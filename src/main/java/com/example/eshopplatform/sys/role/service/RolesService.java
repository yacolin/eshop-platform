package com.example.eshopplatform.sys.role.service;

import com.example.eshopplatform.sys.common.SysAdminGuard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eshopplatform.sys.role.mapper.RolesMapper;
import com.example.eshopplatform.sys.role.entity.Roles;
import com.example.eshopplatform.sys.role.dto.RolesCreateReq;
import com.example.eshopplatform.sys.role.dto.RolesUpdateReq;
import com.example.eshopplatform.sys.role.dto.RolesVO;
import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.common.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * B端角色定义表服务
 * </p>
 *
 * <p>工程约定：Service 为具体类（不生成接口与 *ServiceImpl），直接注入 Mapper；
 * 入参/出参走 DTO（CreateReq/UpdateReq/VO），entity↔dto 的 toVO/apply 由生成器自动补齐。
 * 时间列在 VO 中以 Long（epoch 毫秒）返回；写操作只落业务字段，
 * created_at/updated_at/deleted_at 等自动列一律不进 apply。
 * 语义约定：update 为 DTO 覆盖（null 字段保留原值，非全量重置）；delete 默认逻辑删除
 * （表含 deleted_at 时实体自动标 @TableLogic，无该列的表才是物理删除）。
 * 角色管理整体要求 B端管理员（当前员工持 builtin 角色，见 {@link SysAdminGuard}），
 * 对齐 gf-eshop：Roles 控制器整体挂在 RequireAdmin 中间件下。
 * 以下为基础 CRUD，接入真实业务时按需加查询条件、校验与权限逻辑。</p>
 *
 * @since 2026-09-06
 */
@Service
@RequiredArgsConstructor
public class RolesService {

    /** 数据访问层 */
    private final RolesMapper rolesMapper;

    /** B端管理员守卫（角色管理整体需管理员） */
    private final SysAdminGuard sysAdminGuard;

    /** roleType 取值域（DB 字符串枚举，见 sys_roles.role_type 注释与种子：builtin-系统内置 custom-自定义） */
    private static final String ROLE_TYPE_BUILTIN = "builtin";
    private static final String ROLE_TYPE_CUSTOM = "custom";

    /** 分页查询（第 page 页，每页 size 条；需管理员） */
    public PageResult<RolesVO> page(int page, int size, String roleType, String name, String status) {
        sysAdminGuard.requireAdmin();
        Page<Roles> p = new Page<>(normalizePage(page), normalizeSize(size));
        LambdaQueryWrapper<Roles> wrapper = new LambdaQueryWrapper<Roles>()
                .eq(roleType != null, Roles::getRoleType, roleType)
                .like(name != null && !name.isEmpty(), Roles::getName, name)
                .eq(status != null, Roles::getStatus, status)
                .orderByDesc(Roles::getId);
        rolesMapper.selectPage(p, wrapper);
        return PageResult.of(p.getTotal(), p.getRecords().stream().map(this::toVO).toList());
    }

    /*
     * 带筛选分页写法约定（本模板只生成无筛选版本；接入业务筛选时给 page 补筛选参数，
     * 推荐 MyBatis-Plus condition 链式写法：一行一个筛选条件，判定写在 condition 内，
     * false 自动跳过、无需 if 包裹，排序与条件同处一个查询描述）：
     *
     *     public PageResult<XxxVO> page(int page, int size, Integer status, String name) {
     *         Page<Xxx> p = new Page<>(normalizePage(page), normalizeSize(size));
     *         LambdaQueryWrapper<Xxx> wrapper = new LambdaQueryWrapper<Xxx>()
     *                 .eq(status != null, Xxx::getStatus, status)
     *                 .like(StringUtils.hasText(name), Xxx::getName, name)
     *                 .orderByDesc(Xxx::getId);
     *         xxxMapper.selectPage(p, wrapper);
     *         return PageResult.of(p.getTotal(), p.getRecords().stream().map(this::toVO).toList());
     *     }
     *
     * 带业务语义的判定（如 parentId > 0 表示只筛子级、level > 0）也写在 condition 里；
     * 判定一长就读不动时，抽成带名字的局部 boolean 或单独 if，不要硬塞链式。
     */

    /** 按主键查询（需管理员） */
    public RolesVO getById(Long id) {
        sysAdminGuard.requireAdmin();
        return toVO(require(id));
    }

    /**
     * 新增（需管理员）。
     * 业务规则：
     * <ul>
     *   <li>roleType 只允许 DB 字符串枚举 builtin/custom；builtin（系统内置）角色仅能由种子/代码定义，
     *       接口创建一律拒绝（HTTP 403）；</li>
     *   <li>name 受 sys_roles.uk_name 唯一索引约束，重复创建返回 409（含已逻辑删除行，
     *       软删仍占索引）；</li>
     *   <li>status（Boolean）对应 DB tinyint(1) 的 1/0，未传默认启用（true）；</li>
     *   <li>roleType 未传时显式落 DB 默认值 custom、status 未传默认 true，
     *       避免 INSERT 省略列后返回 VO 与库中实际值不一致。</li>
     * </ul>
     */
    public RolesVO create(RolesCreateReq req) {
        sysAdminGuard.requireAdmin();
        checkRoleType(req.getRoleType());
        if (ROLE_TYPE_BUILTIN.equals(req.getRoleType())) {
            throw BizException.forbidden("系统内置角色（builtin）不允许通过接口创建");
        }
        checkNameUnique(req.getName(), null);
        Roles entity = new Roles();
        apply(entity, req);
        if (entity.getRoleType() == null) {
            entity.setRoleType(ROLE_TYPE_CUSTOM);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(Boolean.TRUE);
        }
        rolesMapper.insert(entity);
        return toVO(entity);
    }

    /**
     * 按主键更新（需管理员；DTO 覆盖语义）。
     * 注意：MyBatis-Plus 默认 NOT_NULL 策略——req 中为 null 的字段不会生成 SET，
     * 即"没传的字段保留原值"；若业务要求"传 null = 重置为默认值"，请在 apply 内
     * 对该字段显式兜底（如 entity.setStatus(req.getStatus() == null
     * ? Boolean.TRUE : req.getStatus())）。
     * 另：系统内置角色（builtin）的身份字段受保护——roleType 与 name 均不允许修改，
     * 防止绕过"内置角色只能由种子/代码定义"的约束（displayName/description/status/
     * sortOrder 等展示字段仍可编辑）。
     */
    public RolesVO update(Long id, RolesUpdateReq req) {
        sysAdminGuard.requireAdmin();
        Roles entity = require(id);
        checkRoleType(req.getRoleType());
        if (ROLE_TYPE_BUILTIN.equals(entity.getRoleType())
                && req.getRoleType() != null && !ROLE_TYPE_BUILTIN.equals(req.getRoleType())) {
            throw BizException.forbidden("系统内置角色（builtin）不允许修改 roleType");
        }
        if (ROLE_TYPE_BUILTIN.equals(entity.getRoleType())
                && req.getName() != null && !entity.getName().equals(req.getName())) {
            throw BizException.forbidden("系统内置角色（builtin）不允许修改角色名称（name）");
        }
        checkNameUnique(req.getName(), id);
        apply(entity, req);
        rolesMapper.updateById(entity);
        return toVO(entity);
    }

    /**
     * 按主键删除（默认逻辑删除）。
     * 表含 deleted_at 列时，实体该字段已标 {@code @TableLogic(value = "null", delval = "now()")}：
     * deleteById 自动转 {@code UPDATE ... SET deleted_at = now()}，且普通查询
     * （selectById/page/wrapper）自动带 {@code deleted_at IS NULL}，无需手写过滤；
     * 仅当表本身没有 deleted_at 列（流水/记录表）时才是物理删除。
     * 业务上若存在"被引用 / 子级存在时不允许删除"等约束，接入时先查后删并抛
     * {@code BizException.conflict}，例如：
     * <pre>{@code
     * long ref = xxxMapper.selectCount(new LambdaQueryWrapper<Xxx>()
     *         .eq(Xxx::getCategoryId, id));
     * if (ref > 0) {
     *     throw BizException.conflict("该记录已被引用，无法删除");
     * }
     * }</pre>
     */
    public void delete(Long id) {
        sysAdminGuard.requireAdmin();
        require(id);
        rolesMapper.deleteById(id);
    }

    /**
     * 校验 roleType 落在 DB 字符串枚举内（builtin/custom）。
     * 传 null 视为"取默认值"（创建默认 custom），不拦截。
     */
    private static void checkRoleType(String roleType) {
        if (roleType != null
                && !ROLE_TYPE_BUILTIN.equals(roleType) && !ROLE_TYPE_CUSTOM.equals(roleType)) {
            throw BizException.badRequest("roleType 仅支持：builtin(系统内置) / custom(自定义)");
        }
    }

    /**
     * 角色名称唯一性校验（对应 sys_roles.uk_name）。
     * 注意：逻辑删除仅置 deleted_at，行仍占 uk_name 索引，因此已软删同名角色也会撞冲突；
     * 这是表结构决定的现状（索引不含 deleted_at），如需允许软删后复用名称需改表。
     *
     * @param excludeId 更新时传入自身 id 以排除自己；创建传 null
     */
    private void checkNameUnique(String name, Long excludeId) {
        if (name == null || name.isEmpty()) {
            return;
        }
        LambdaQueryWrapper<Roles> wrapper = new LambdaQueryWrapper<Roles>()
                .eq(Roles::getName, name);
        if (excludeId != null) {
            wrapper.ne(Roles::getId, excludeId);
        }
        if (rolesMapper.selectCount(wrapper) > 0) {
            throw BizException.conflict("角色名称已存在");
        }
    }

    /** 按主键查询，不存在抛 404 */
    private Roles require(Long id) {
        Roles entity = rolesMapper.selectById(id);
        if (entity == null) {
            throw BizException.notFound("B端角色定义表不存在");
        }
        return entity;
    }

    /** entity -> VO（生成器自动映射：deleted_at 不对外暴露；时间列转 Long epoch 毫秒） */
    private RolesVO toVO(Roles entity) {
        RolesVO vo = new RolesVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setDisplayName(entity.getDisplayName());
        vo.setDescription(entity.getDescription());
        vo.setRoleType(entity.getRoleType());
        vo.setSortOrder(entity.getSortOrder());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(TimeUtil.toEpochMillis(entity.getCreatedAt()));
        vo.setUpdatedAt(TimeUtil.toEpochMillis(entity.getUpdatedAt()));
        return vo;
    }

    /** CreateReq -> entity（生成器自动映射：主键 id、created_at/updated_at/deleted_at 等
     *  自动列绝不在此赋值；业务字段计算在此补充） */
    private void apply(Roles entity, RolesCreateReq req) {
        entity.setName(req.getName());
        entity.setDisplayName(req.getDisplayName());
        entity.setDescription(req.getDescription());
        entity.setRoleType(req.getRoleType());
        entity.setSortOrder(req.getSortOrder());
        entity.setStatus(req.getStatus());
    }

    /** UpdateReq -> entity（同上：全量更新时只落业务字段） */
    private void apply(Roles entity, RolesUpdateReq req) {
        entity.setName(req.getName());
        entity.setDisplayName(req.getDisplayName());
        entity.setDescription(req.getDescription());
        entity.setRoleType(req.getRoleType());
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
