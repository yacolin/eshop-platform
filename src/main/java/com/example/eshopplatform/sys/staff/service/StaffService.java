package com.example.eshopplatform.sys.staff.service;

import com.example.eshopplatform.sys.common.SysAdminGuard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.common.TimeUtil;
import com.example.eshopplatform.security.JwtTokenProvider;
import com.example.eshopplatform.security.UserContext;
import com.example.eshopplatform.sys.staff.dto.StaffAssignRolesReq;
import com.example.eshopplatform.sys.staff.dto.StaffCreateReq;
import com.example.eshopplatform.sys.staff.dto.StaffListItemVO;
import com.example.eshopplatform.sys.staff.dto.StaffLoginVO;
import com.example.eshopplatform.sys.staff.dto.StaffPermissionsVO;
import com.example.eshopplatform.sys.staff.dto.StaffProfileVO;
import com.example.eshopplatform.sys.staff.dto.StaffTokenVO;
import com.example.eshopplatform.sys.staff.dto.StaffUpdateReq;
import com.example.eshopplatform.sys.role.entity.Roles;
import com.example.eshopplatform.sys.staff.entity.Staff;
import com.example.eshopplatform.sys.permission.mapper.PermissionsMapper;
import com.example.eshopplatform.sys.role.mapper.RolesMapper;
import com.example.eshopplatform.sys.staff.mapper.StaffMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * B端员工业务（sys_staff）——认证（对齐 gf-eshop staff）+ 员工管理：
 * 列表（含角色/部门归属）、新增、更新、删除、分配角色（PUT /staff/{id}/roles）。
 * </p>
 *
 * <p>权限：认证接口（login/refresh）白名单；profile/permissions/列表任意 B端员工；
 * 员工新增/更新/删除/分配角色为管理操作（{@link SysAdminGuard}）。
 * 关联表（sys_staff_roles / sys_staff_departments）与角色-权限一致，采用
 * "物理清空 + 批量写入"的替换式维护（唯一键下软删行会挡路）。</p>
 *
 * @since 2026-09-06
 */
@Service
@RequiredArgsConstructor
public class StaffService {

    /** Redis 刷新令牌白名单键前缀：staff:refresh:{staffId}:{tokenId} */
    static final String REFRESH_KEY_PREFIX = "staff:refresh:";

    private final StaffMapper staffMapper;
    private final RolesMapper rolesMapper;
    private final PermissionsMapper permissionsMapper;
    private final SysAdminGuard sysAdminGuard;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redis;

    // ==================== 认证 ====================

    /** 员工登录 */
    public StaffLoginVO login(String username, String password, String ip, String device) {
        Staff staff = staffMapper.selectOne(new LambdaQueryWrapper<Staff>()
                .eq(Staff::getUsername, username));
        if (staff == null || !passwordEncoder.matches(password, staff.getPasswordHash())) {
            if (staff != null) {
                staffMapper.insertLoginHistory(staff.getId(), ip, device, "", "password", 0, "用户名或密码错误");
            }
            throw BizException.unauthorized("用户名或密码错误");
        }
        if (!Boolean.TRUE.equals(staff.getStatus())) {
            staffMapper.insertLoginHistory(staff.getId(), ip, device, "", "password", 0, "账号已被禁用");
            throw BizException.forbidden("账号已被禁用");
        }
        return issuePair(staff, ip, device);
    }

    /** 刷新令牌（轮换：校验白名单 → 旧 token 作废 → 签发新对） */
    public StaffTokenVO refresh(String refreshToken) {
        Claims claims = parseStaffRefreshClaims(refreshToken);
        Long staffId = Long.valueOf(claims.getSubject());
        String oldTokenId = claims.getId();
        String key = refreshKey(staffId, oldTokenId);
        if (Boolean.FALSE.equals(redis.hasKey(key))) {
            throw BizException.unauthorized("刷新令牌已失效，请重新登录");
        }
        redis.delete(key);

        Staff staff = staffMapper.selectById(staffId);
        if (staff == null || !Boolean.TRUE.equals(staff.getStatus())) {
            throw BizException.unauthorized("账号不存在或已被禁用");
        }
        return toTokenVO(staff);
    }

    /** 登出：作废该员工全部刷新令牌 */
    public void logout() {
        Long staffId = UserContext.getStaffId();
        Set<String> keys = redis.keys(REFRESH_KEY_PREFIX + staffId + ":*");
        if (keys != null && !keys.isEmpty()) {
            redis.delete(keys);
        }
    }

    /** 当前员工资料（含所属部门） */
    public StaffProfileVO profile() {
        Long staffId = UserContext.getStaffId();
        Staff staff = require(staffId);
        StaffProfileVO vo = new StaffProfileVO();
        vo.setId(staff.getId());
        vo.setUsername(staff.getUsername());
        vo.setRealName(staff.getRealName());
        vo.setEmail(staff.getEmail());
        vo.setPhone(staff.getPhone());
        vo.setAvatar(staff.getAvatar());
        vo.setStatus(staff.getStatus());
        vo.setLastLoginIp(staff.getLastLoginIp());
        vo.setDepartmentIds(staffMapper.selectDepartmentIdsByStaffId(staffId));
        vo.setDepartmentNames(staffMapper.selectDepartmentNamesByStaffId(staffId));
        return vo;
    }

    /** 当前员工的角色名与权限标识（供前端菜单/按钮鉴权） */
    public StaffPermissionsVO permissions() {
        Long staffId = UserContext.getStaffId();
        StaffPermissionsVO vo = new StaffPermissionsVO();
        vo.setRoles(rolesMapper.selectRoleNamesByStaffId(staffId));
        vo.setPermissions(permissionsMapper.selectPermissionNamesByStaffId(staffId));
        return vo;
    }

    // ==================== 员工管理（写操作需管理员） ====================

    /** 员工分页列表（任意 B端员工可查），keyword 匹配用户名/姓名，含角色与部门归属 */
    public PageResult<StaffListItemVO> page(int page, int size, String keyword, String status) {
        Page<Staff> p = new Page<>(Math.max(page, 1), Math.min(Math.max(size, 1), 1000));
        LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Staff::getUsername, keyword)
                    .or().like(Staff::getRealName, keyword));
        }
        Boolean st = parseStatus(status);
        wrapper.eq(st != null, Staff::getStatus, st);
        wrapper.orderByDesc(Staff::getId);
        staffMapper.selectPage(p, wrapper);
        return PageResult.of(p.getTotal(), p.getRecords().stream().map(this::toListItem).toList());
    }

    /** 新增员工（需管理员），可同时指定部门归属 */
    @Transactional(rollbackFor = Exception.class)
    public StaffListItemVO create(StaffCreateReq req) {
        sysAdminGuard.requireAdmin();
        checkUsernameUnique(req.getUsername(), null);
        Staff staff = new Staff();
        staff.setUsername(req.getUsername().trim());
        staff.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        staff.setRealName(req.getRealName() == null ? "" : req.getRealName());
        staff.setEmail(req.getEmail());
        staff.setPhone(req.getPhone());
        staff.setAvatar(req.getAvatar());
        staff.setStatus(req.getStatus() == null ? Boolean.TRUE : req.getStatus());
        staffMapper.insert(staff);
        replaceDepartments(staff.getId(), req.getDepartmentIds());
        return toListItem(staff);
    }

    /**
     * 更新员工（需管理员）。username 不允许改；password 空/缺省 = 不重置；
     * departmentIds 缺省 = 不变，传入（可为空数组）= 替换部门归属。
     */
    @Transactional(rollbackFor = Exception.class)
    public StaffListItemVO update(Long id, StaffUpdateReq req) {
        sysAdminGuard.requireAdmin();
        Staff staff = require(id);
        if (req.getRealName() != null) {
            staff.setRealName(req.getRealName());
        }
        if (req.getEmail() != null) {
            staff.setEmail(req.getEmail());
        }
        if (req.getPhone() != null) {
            staff.setPhone(req.getPhone());
        }
        if (req.getAvatar() != null) {
            staff.setAvatar(req.getAvatar());
        }
        if (req.getStatus() != null) {
            staff.setStatus(req.getStatus());
        }
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            staff.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        if (req.getDepartmentIds() != null) {
            replaceDepartments(id, req.getDepartmentIds());
        }
        staffMapper.updateById(staff);
        return toListItem(staff);
    }

    /** 删除员工（需管理员；逻辑删除）。不允许删除自己与持 builtin 角色的系统管理员 */
    public void delete(Long id) {
        sysAdminGuard.requireAdmin();
        if (UserContext.getStaffId().equals(id)) {
            throw BizException.forbidden("不能删除当前登录的自己");
        }
        require(id);
        if (rolesMapper.countBuiltinRolesOfStaff(id) > 0) {
            throw BizException.forbidden("系统管理员（持 builtin 角色）不允许删除");
        }
        staffMapper.deleteById(id);
    }

    /** 分配员工角色（需管理员）：整表替换，空数组=清空 */
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long staffId, StaffAssignRolesReq req) {
        sysAdminGuard.requireAdmin();
        require(staffId);
        List<Long> ids = normalizeIds(req.getRoleIds());
        if (!ids.isEmpty()) {
            long n = rolesMapper.selectCount(new LambdaQueryWrapper<Roles>()
                    .in(Roles::getId, ids));
            if (n != ids.size()) {
                throw BizException.badRequest("包含不存在或已删除的角色ID");
            }
        }
        staffMapper.deleteStaffRoles(staffId);
        if (!ids.isEmpty()) {
            staffMapper.insertStaffRoles(staffId, ids);
        }
    }

    // ==================== 私有工具 ====================

    /** 员工详情/行 → 列表项（含角色与部门归属） */
    private StaffListItemVO toListItem(Staff staff) {
        StaffListItemVO vo = new StaffListItemVO();
        vo.setId(staff.getId());
        vo.setUsername(staff.getUsername());
        vo.setRealName(staff.getRealName());
        vo.setEmail(staff.getEmail());
        vo.setPhone(staff.getPhone());
        vo.setAvatar(staff.getAvatar());
        vo.setStatus(staff.getStatus());
        vo.setLastLoginIp(staff.getLastLoginIp());
        vo.setLastLoginAt(TimeUtil.toEpochMillis(staff.getLastLoginAt()));
        vo.setCreatedAt(TimeUtil.toEpochMillis(staff.getCreatedAt()));
        Long staffId = staff.getId();
        vo.setRoleIds(rolesMapper.selectRoleIdsByStaffId(staffId));
        vo.setRoleNames(rolesMapper.selectRoleNamesByStaffId(staffId));
        vo.setDepartmentIds(staffMapper.selectDepartmentIdsByStaffId(staffId));
        vo.setDepartmentNames(staffMapper.selectDepartmentNamesByStaffId(staffId));
        return vo;
    }

    /** 替换员工部门归属（departmentIds 为 null 时不做任何变更） */
    private void replaceDepartments(Long staffId, List<Long> departmentIds) {
        if (departmentIds == null) {
            return;
        }
        List<Long> ids = normalizeIds(departmentIds);
        if (!ids.isEmpty() && staffMapper.countExistingDepartments(ids) != ids.size()) {
            throw BizException.badRequest("包含不存在或已删除的部门ID");
        }
        staffMapper.deleteStaffDepartments(staffId);
        if (!ids.isEmpty()) {
            staffMapper.insertStaffDepartments(staffId, ids);
        }
    }

    /** 用户名唯一性校验（sys_staff.uk_username） */
    private void checkUsernameUnique(String username, Long excludeId) {
        if (username == null || username.isBlank()) {
            return;
        }
        LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<Staff>()
                .eq(Staff::getUsername, username.trim());
        if (excludeId != null) {
            wrapper.ne(Staff::getId, excludeId);
        }
        if (staffMapper.selectCount(wrapper) > 0) {
            throw BizException.conflict("用户名已存在");
        }
    }

    /** 按主键查询员工，不存在抛 404 */
    private Staff require(Long id) {
        Staff staff = staffMapper.selectById(id);
        if (staff == null) {
            throw BizException.notFound("员工不存在");
        }
        return staff;
    }

    private static List<Long> normalizeIds(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    /** 兼容 1/0 与 true/false 的状态筛参；无法解析时返回 null（不筛） */
    private static Boolean parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        if ("1".equals(status) || "true".equalsIgnoreCase(status)) {
            return Boolean.TRUE;
        }
        if ("0".equals(status) || "false".equalsIgnoreCase(status)) {
            return Boolean.FALSE;
        }
        return null;
    }

    /** 签发令牌对、Redis 白名单登记刷新令牌、更新最后登录信息并记登录历史 */
    private StaffLoginVO issuePair(Staff staff, String ip, String device) {
        String accessToken = tokenProvider.createStaffAccessToken(staff.getId(), staff.getUsername(), staff.getRealName());
        String refreshToken = tokenProvider.createStaffRefreshToken(staff.getId(), staff.getUsername(), staff.getRealName());
        saveRefreshToken(staff.getId(), refreshToken);

        staff.setLastLoginIp(ip);
        staff.setLastLoginAt(LocalDateTime.now());
        staffMapper.updateById(staff);
        staffMapper.insertLoginHistory(staff.getId(), ip, device, "", "password", 1, "");

        StaffLoginVO vo = new StaffLoginVO();
        vo.setAccessToken(accessToken);
        vo.setExpireIn(tokenProvider.getAccessTokenExpireSeconds());
        vo.setRefreshToken(refreshToken);
        vo.setRefreshIn(tokenProvider.getRefreshTokenExpireSeconds());
        vo.setStaffId(staff.getId());
        vo.setUsername(staff.getUsername());
        vo.setRealName(staff.getRealName());
        return vo;
    }

    private StaffTokenVO toTokenVO(Staff staff) {
        String accessToken = tokenProvider.createStaffAccessToken(staff.getId(), staff.getUsername(), staff.getRealName());
        String refreshToken = tokenProvider.createStaffRefreshToken(staff.getId(), staff.getUsername(), staff.getRealName());
        saveRefreshToken(staff.getId(), refreshToken);
        StaffTokenVO vo = new StaffTokenVO();
        vo.setAccessToken(accessToken);
        vo.setExpireIn(tokenProvider.getAccessTokenExpireSeconds());
        vo.setRefreshToken(refreshToken);
        vo.setRefreshIn(tokenProvider.getRefreshTokenExpireSeconds());
        return vo;
    }

    /** 校验为 B端员工 refresh 令牌并返回声明，否则 401 */
    private Claims parseStaffRefreshClaims(String refreshToken) {
        try {
            Claims claims = tokenProvider.parse(refreshToken);
            if (!JwtTokenProvider.TYPE_REFRESH.equals(claims.get(JwtTokenProvider.CLAIM_TOKEN_TYPE, String.class))
                    || !JwtTokenProvider.KIND_STAFF.equals(claims.get(JwtTokenProvider.CLAIM_KIND, String.class))) {
                throw BizException.unauthorized("刷新令牌无效");
            }
            return claims;
        } catch (BizException e) {
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            throw BizException.unauthorized("刷新令牌无效或已过期");
        }
    }

    private String refreshKey(Long staffId, String tokenId) {
        return REFRESH_KEY_PREFIX + staffId + ":" + tokenId;
    }

    private void saveRefreshToken(Long staffId, String refreshToken) {
        Claims claims = tokenProvider.parse(refreshToken); // 取 jti（自己刚签发，不会失败）
        redis.opsForValue().set(refreshKey(staffId, claims.getId()), "1",
                Duration.ofSeconds(tokenProvider.getRefreshTokenExpireSeconds()));
    }
}
