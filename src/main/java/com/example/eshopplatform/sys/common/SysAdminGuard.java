package com.example.eshopplatform.sys.common;

import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.security.UserContext;
import com.example.eshopplatform.sys.role.mapper.RolesMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * B端管理员守卫：管理员 = 当前员工（ROLE_STAFF 令牌）且持有 builtin（系统内置）角色，
 * 对齐 gf-eshop RequireAdmin/IsAdmin（动态查库，不信任令牌声明）。Roles/Permissions/
 * Staff 三类管理接口共用。
 */
@Service
@RequiredArgsConstructor
public class SysAdminGuard {

    private final RolesMapper rolesMapper;

    /** 非管理员直接抛 403（非 staff 令牌抛 403，见 UserContext） */
    public void requireAdmin() {
        Long staffId = UserContext.getStaffId();
        if (rolesMapper.countBuiltinRolesOfStaff(staffId) == 0) {
            throw BizException.forbidden("无权限，需要管理员角色");
        }
    }
}
