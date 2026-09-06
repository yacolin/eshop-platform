package com.example.eshopplatform.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 当前登录主体（SecurityContext 的 principal）。
 * 字段来自 JWT 声明，由 {@link JwtAuthenticationFilter} 写入；业务代码通过
 * {@link UserContext} 取用。
 * <ul>
 *   <li>B端员工（staff）：kind=staff，id=sys_staff.id，携带 username/realName，
 *       权限 {@code ROLE_STAFF}；是否管理员（持有 builtin 角色）由服务层查库判定，
 *       不写入令牌（对齐 gf-eshop：管理员 = 员工挂 builtin 角色）。</li>
 *   <li>C端用户（user）：kind=user，id=userId + userType（usr_users.user_type，
 *       =1 视为超级管理员，额外授 ROLE_ADMIN）。</li>
 * </ul>
 */
@Getter
public class LoginUser {

    /** 主体 id：staff=sys_staff.id / user=usr_users.id */
    private final Long id;
    /** C端用户类型（仅 user 令牌有值；staff 令牌为 null） */
    private final Integer userType;
    /** 主体类别：staff / user */
    private final String kind;
    /** B端员工用户名（仅 staff 令牌） */
    private final String username;
    /** B端员工真实姓名（仅 staff 令牌） */
    private final String realName;
    private final Collection<? extends GrantedAuthority> authorities;

    private LoginUser(Long id, Integer userType, String kind, String username, String realName,
                      Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.userType = userType;
        this.kind = kind;
        this.username = username;
        this.realName = realName;
        this.authorities = authorities;
    }

    /** C端用户主体（userType=1 追加 ROLE_ADMIN） */
    public static LoginUser of(Long userId, Integer userType) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (userType != null && userType == 1) {
            // 超级管理员（usr_users.user_type=1）
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return new LoginUser(userId, userType, JwtTokenProvider.KIND_USER, null, null, authorities);
    }

    /** B端员工主体（是否管理员由服务层查 builtin 角色决定，见 RolesService/PermissionsService） */
    public static LoginUser staff(Long staffId, String username, String realName) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
        return new LoginUser(staffId, null, JwtTokenProvider.KIND_STAFF, username, realName, authorities);
    }

    /** C端超级管理员（user 令牌且 user_type=1） */
    public boolean isAdmin() {
        return userType != null && userType == 1;
    }

    /** 是否 B端员工主体 */
    public boolean isStaff() {
        return JwtTokenProvider.KIND_STAFF.equals(kind);
    }
}
