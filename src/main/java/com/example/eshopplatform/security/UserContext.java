package com.example.eshopplatform.security;

import com.example.eshopplatform.common.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户工具：从 SecurityContext 取 principal。
 * 未登录（无 token / 令牌无效）时抛 401 {@link BizException}。
 */
public final class UserContext {

    private UserContext() {
    }

    /** 当前登录用户主体，未登录抛 401 */
    public static LoginUser getLoginUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        throw BizException.unauthorized("未登录或登录已过期");
    }

    /** 当前登录用户 ID（user=C端用户 id；staff=员工 id） */
    public static Long getUserId() {
        return getLoginUser().getId();
    }

    /** 当前登录用户类型（usr_users.user_type；staff 令牌返回 null） */
    public static Integer getUserType() {
        return getLoginUser().getUserType();
    }

    /**
     * 当前 B端员工主体，非 staff 令牌抛 403。
     * B端接口（roles/permissions/staff）都要求 staff 令牌。
     */
    public static LoginUser getStaff() {
        LoginUser user = getLoginUser();
        if (!user.isStaff()) {
            throw BizException.forbidden("仅B端员工可访问");
        }
        return user;
    }

    /** 当前 B端员工 ID（sys_staff.id），非 staff 令牌抛 403 */
    public static Long getStaffId() {
        return getStaff().getId();
    }
}
