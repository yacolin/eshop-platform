package com.example.eshopplatform.sys.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.common.TimeUtil;
import com.example.eshopplatform.security.UserContext;
import com.example.eshopplatform.sys.dto.PermissionsCreateReq;
import com.example.eshopplatform.sys.dto.PermissionsUpdateReq;
import com.example.eshopplatform.sys.dto.PermissionsVO;
import com.example.eshopplatform.sys.entity.Permissions;
import com.example.eshopplatform.sys.mapper.PermissionsMapper;
import com.example.eshopplatform.sys.mapper.RolesMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * B端权限表服务
 * </p>
 *
 * <p>工程约定：Service 为具体类（不生成接口与 *ServiceImpl），直接注入 Mapper；
 * 入参/出参走 DTO（CreateReq/UpdateReq/VO），entity↔dto 的 toVO/apply 由生成器自动补齐。
 * 时间列在 VO 中以 Long（epoch 毫秒）返回；写操作只落业务字段，
 * created_at/updated_at/deleted_at 等自动列一律不进 apply。
 * 语义约定：update 为 DTO 覆盖（null 字段保留原值，非全量重置）；delete 默认逻辑删除，
 * 但角色-权限关联（sys_role_permissions）为替换式物理删除（唯一键下软删行会挡路）。</p>
 *
 * <p>权限编排三个业务接口（见 PermissionsController）：
 * <ul>
 *   <li>GET /permissions/roles/{roleId}：查角色已授予的权限；</li>
 *   <li>PUT /permissions/roles/{roleId}：整表替换角色的授权（先物理清空再批量写入）；</li>
 *   <li>POST /permissions/check：校验当前登录用户是否拥有全部给定权限。</li>
 * </ul>
 * 当前用户由 JWT principal 提供（sys_staff.id）；userType=1 视为超级管理员直接放行。
 * 注意：B端员工登录/发牌尚未实现，届时签发 access token 的 subject 需落 sys_staff.id
 * 本校验才会真实生效；在此之前 /check 只能被已有 C端/管理员令牌触发。</p>
 *
 * @since 2026-09-06
 */
@Service
@RequiredArgsConstructor
public class PermissionsService {

    /** 数据访问层 */
    private final PermissionsMapper permissionsMapper;

    /** 角色（校验角色存在用） */
    private final RolesMapper rolesMapper;

    /** 分页查询（第 page 页，每页 size 条） */
    public PageResult<PermissionsVO> page(int page, int size, String resource, String action, String category, String status) {
        Page<Permissions> p = new Page<>(normalizePage(page), normalizeSize(size));
        LambdaQueryWrapper<Permissions> wrapper = new LambdaQueryWrapper<Permissions>()
                .eq(resource != null && !resource.isEmpty(), Permissions::getResource, resource)
                .eq(action != null && !action.isEmpty(), Permissions::getAction, action)
                .eq(category != null && !category.isEmpty(), Permissions::getCategory, category)
                .eq(status != null && !status.isEmpty(), Permissions::getStatus, status)
                .orderByDesc(Permissions::getId);
        permissionsMapper.selectPage(p, wrapper);
        return PageResult.of(p.getTotal(), p.getRecords().stream().map(this::toVO).toList());
    }

    /** 按主键查询 */
    public PermissionsVO getById(Long id) {
        return toVO(require(id));
    }

    /**
     * 新增。
     * 业务规则：
     * <ul>
     *   <li>name（权限标识，如 order:create）受 sys_permissions.uk_name 唯一约束，重复创建返回 409；</li>
     *   <li>status 为 Boolean，对应 DB tinyint(1) 的 1/0；未传默认启用（true）。</li>
     * </ul>
     */
    public PermissionsVO create(PermissionsCreateReq req) {
        checkNameUnique(req.getName(), null);
        Permissions entity = new Permissions();
        apply(entity, req);
        if (entity.getStatus() == null) {
            entity.setStatus(Boolean.TRUE);
        }
        permissionsMapper.insert(entity);
        return toVO(entity);
    }

    /**
     * 按主键更新（DTO 覆盖语义）。
     * 注意：MyBatis-Plus 默认 NOT_NULL 策略——req 中为 null 的字段不会生成 SET，
     * 即"没传的字段保留原值"；若业务要求"传 null = 重置为默认值"，请在 apply 内
     * 对该字段显式兜底（如 entity.setStatus(req.getStatus() == null
     * ? Boolean.TRUE : req.getStatus())）。
     */
    public PermissionsVO update(Long id, PermissionsUpdateReq req) {
        Permissions entity = require(id);
        checkNameUnique(req.getName(), id);
        apply(entity, req);
        permissionsMapper.updateById(entity);
        return toVO(entity);
    }

    /**
     * 按主键删除（默认逻辑删除：置 deleted_at，普通查询自动过滤）。
     * 引用保护：该权限仍被任意角色授权（sys_role_permissions 未软删行）时拒绝删除，
     * 需先经 PUT /permissions/roles/{roleId} 从角色授权列表移除，或直接把权限置为禁用
     * （status=false，不删除、不丢授权关系）。
     */
    public void delete(Long id) {
        require(id);
        if (permissionsMapper.countRoleRefs(id) > 0) {
            throw BizException.conflict("该权限仍被角色引用，请先从对应角色的权限列表中移除（PUT /permissions/roles/{roleId}）");
        }
        permissionsMapper.deleteById(id);
    }

    // ==================== 角色授权编排 ====================

    /**
     * 查询角色已授予的权限列表（平台范围），按 sort_order,id 升序。
     * 角色不存在返回 404。
     */
    public PermissionsVO[] getPermissionsByRoleId(Long roleId) {
        requireRole(roleId);
        return permissionsMapper.selectByRoleId(roleId).stream()
                .map(this::toVO)
                .toArray(PermissionsVO[]::new);
    }

    /**
     * 整表替换角色的授权：以 body 给定的权限 id 集合为角色的最终权限集。
     * 实现为事务内"物理清空 → 批量写入"，保证幂等与并发安全；
     * 空数组 = 清空该角色全部授权。
     * body 项取 PermissionsVO.id 定位权限；含不存在/已软删权限 id 时 400 并整体回滚。
     */
    @Transactional(rollbackFor = Exception.class)
    public void putPermissionsByRoleId(Long roleId, PermissionsVO[] permissions) {
        requireRole(roleId);
        List<Long> ids = permissions == null ? List.of()
                : Arrays.stream(permissions)
                        .map(PermissionsVO::getId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();
        // 防呆：body 非空却解析不到任何权限 id（如只传了 name），视为请求错误而非"清空授权"
        if (permissions != null && permissions.length > 0 && ids.isEmpty()) {
            throw BizException.badRequest("body 需携带权限 id（PermissionsVO.id），未解析到任何权限ID");
        }
        if (!ids.isEmpty()) {
            Set<Long> existIds = permissionsMapper.selectBatchIds(ids).stream()
                    .map(Permissions::getId)
                    .collect(Collectors.toSet());
            List<Long> missing = ids.stream().filter(id -> !existIds.contains(id)).toList();
            if (!missing.isEmpty()) {
                throw BizException.badRequest("包含不存在或已删除的权限ID：" + missing);
            }
        }
        permissionsMapper.deleteRolePermissions(roleId);
        if (!ids.isEmpty()) {
            permissionsMapper.insertRolePermissions(roleId, ids);
        }
    }

    /**
     * 校验当前登录用户是否拥有 body 中全部权限（任一不满足即 false）。
     * <ul>
     *   <li>userType=1（超级管理员）直接放行返回 true；</li>
     *   <li>否则按 principal.id = sys_staff.id 经 员工→角色→权限 解析启用权限集合；
     *       body 项按 id 匹配，无 id 时退化为按 name（唯一权限标识）匹配；</li>
     *   <li>空数组视为无要求，返回 true。</li>
     * </ul>
     */
    public Boolean checkUserPermissions(PermissionsVO[] permissions) {
        var user = UserContext.getLoginUser();
        if (user.isAdmin()) {
            return true;
        }
        if (permissions == null || permissions.length == 0) {
            return true;
        }
        List<Permissions> granted = permissionsMapper.selectEnabledByStaffId(user.getId());
        Set<Long> grantedIds = granted.stream().map(Permissions::getId).collect(Collectors.toSet());
        Set<String> grantedNames = granted.stream()
                .map(Permissions::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        for (PermissionsVO vo : permissions) {
            boolean hit = vo.getId() != null && grantedIds.contains(vo.getId())
                    || vo.getName() != null && grantedNames.contains(vo.getName());
            if (!hit) {
                return false;
            }
        }
        return true;
    }

    // ==================== 私有工具 ====================

    /** 权限标识唯一性校验（sys_permissions.uk_name），更新时排除自身 */
    private void checkNameUnique(String name, Long excludeId) {
        if (name == null || name.isEmpty()) {
            return;
        }
        LambdaQueryWrapper<Permissions> wrapper = new LambdaQueryWrapper<Permissions>()
                .eq(Permissions::getName, name);
        if (excludeId != null) {
            wrapper.ne(Permissions::getId, excludeId);
        }
        if (permissionsMapper.selectCount(wrapper) > 0) {
            throw BizException.conflict("权限标识已存在");
        }
    }

    /** 按主键查询权限，不存在抛 404 */
    private Permissions require(Long id) {
        Permissions entity = permissionsMapper.selectById(id);
        if (entity == null) {
            throw BizException.notFound("B端权限表不存在");
        }
        return entity;
    }

    /** 校验角色存在（sys_roles，自动排除软删），不存在抛 404 */
    private void requireRole(Long roleId) {
        if (rolesMapper.selectById(roleId) == null) {
            throw BizException.notFound("角色不存在");
        }
    }

    /** entity -> VO（生成器自动映射：deleted_at 不对外暴露；时间列转 Long epoch 毫秒） */
    private PermissionsVO toVO(Permissions entity) {
        PermissionsVO vo = new PermissionsVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setDisplayName(entity.getDisplayName());
        vo.setDescription(entity.getDescription());
        vo.setResource(entity.getResource());
        vo.setAction(entity.getAction());
        vo.setParentId(entity.getParentId());
        vo.setCategory(entity.getCategory());
        vo.setSortOrder(entity.getSortOrder());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(TimeUtil.toEpochMillis(entity.getCreatedAt()));
        vo.setUpdatedAt(TimeUtil.toEpochMillis(entity.getUpdatedAt()));
        return vo;
    }

    /** CreateReq -> entity（生成器自动映射：主键 id、created_at/updated_at/deleted_at 等
     *  自动列绝不在此赋值；业务字段计算在此补充） */
    private void apply(Permissions entity, PermissionsCreateReq req) {
        entity.setName(req.getName());
        entity.setDisplayName(req.getDisplayName());
        entity.setDescription(req.getDescription());
        entity.setResource(req.getResource());
        entity.setAction(req.getAction());
        entity.setParentId(req.getParentId());
        entity.setCategory(req.getCategory());
        entity.setSortOrder(req.getSortOrder());
        entity.setStatus(req.getStatus());
    }

    /** UpdateReq -> entity（同上：全量更新时只落业务字段） */
    private void apply(Permissions entity, PermissionsUpdateReq req) {
        entity.setName(req.getName());
        entity.setDisplayName(req.getDisplayName());
        entity.setDescription(req.getDescription());
        entity.setResource(req.getResource());
        entity.setAction(req.getAction());
        entity.setParentId(req.getParentId());
        entity.setCategory(req.getCategory());
        entity.setSortOrder(req.getSortOrder());
        entity.setStatus(req.getStatus());
    }

    /** 页号归一化：至少从第 1 页开始 */
    static int normalizePage(int page) {
        return Math.max(page, 1);
    }

    /** 每页条数归一化：1~1000 */
    static int normalizeSize(int size) {
        return Math.min(Math.max(size, 1), 1000);
    }
}
