package com.example.eshopplatform.sys.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.security.JwtTokenProvider;
import com.example.eshopplatform.security.UserContext;
import com.example.eshopplatform.sys.dto.StaffLoginVO;
import com.example.eshopplatform.sys.dto.StaffPermissionsVO;
import com.example.eshopplatform.sys.dto.StaffProfileVO;
import com.example.eshopplatform.sys.dto.StaffTokenVO;
import com.example.eshopplatform.sys.entity.Staff;
import com.example.eshopplatform.sys.mapper.PermissionsMapper;
import com.example.eshopplatform.sys.mapper.RolesMapper;
import com.example.eshopplatform.sys.mapper.StaffMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * <p>
 * B端员工认证服务（sys_staff）——对齐 gf-eshop staff 业务：
 * 登录（bcrypt 校验 + 记登录历史 + 刷新令牌 Redis 白名单）、刷新（轮换）、登出、
 * 当前员工资料与角色/权限。
 * </p>
 *
 * <p>说明：access token 只携带声明（不查库），员工状态（禁用）在登录/刷新时落库校验；
 * “是否管理员”不写进令牌，由业务层按“是否持 builtin 角色”动态判定（见 RolesService/
 * PermissionsService 的 requireAdmin）。</p>
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
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redis;

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

    /** 当前员工资料（部门域业务落地前，部门列表返回空） */
    public StaffProfileVO profile() {
        Long staffId = UserContext.getStaffId();
        Staff staff = staffMapper.selectById(staffId);
        if (staff == null) {
            throw BizException.notFound("员工不存在");
        }
        StaffProfileVO vo = new StaffProfileVO();
        vo.setId(staff.getId());
        vo.setUsername(staff.getUsername());
        vo.setRealName(staff.getRealName());
        vo.setEmail(staff.getEmail());
        vo.setPhone(staff.getPhone());
        vo.setAvatar(staff.getAvatar());
        vo.setStatus(staff.getStatus());
        vo.setLastLoginIp(staff.getLastLoginIp());
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
