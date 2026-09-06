package com.example.eshopplatform.sys.controller;

import com.example.eshopplatform.common.ApiResponse;
import com.example.eshopplatform.sys.dto.StaffLoginReq;
import com.example.eshopplatform.sys.dto.StaffLoginVO;
import com.example.eshopplatform.sys.dto.StaffPermissionsVO;
import com.example.eshopplatform.sys.dto.StaffProfileVO;
import com.example.eshopplatform.sys.dto.StaffRefreshTokenReq;
import com.example.eshopplatform.sys.dto.StaffTokenVO;
import com.example.eshopplatform.sys.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * B端员工认证与当前员工接口（对齐 gf-eshop staff：login/refresh/logout/profile/permissions）
 * </p>
 *
 * <p>路径：/api/v1/staff/**。login/refresh 属白名单（application.yml eshop.security.whitelist），
 * 其余接口需携带 B端员工 access token（ROLE_STAFF）。</p>
 *
 * @since 2026-09-06
 */
@Tag(name = "Staff", description = "B端员工认证与当前员工")
@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @Operation(operationId = "staffLogin", summary = "B端员工登录")
    @PostMapping("/login")
    public ApiResponse<StaffLoginVO> login(@Valid @RequestBody StaffLoginReq req,
                                           HttpServletRequest request) {
        return ApiResponse.ok(staffService.login(req.getUsername(), req.getPassword(),
                clientIp(request), userAgent(request)));
    }

    @Operation(operationId = "staffRefresh", summary = "刷新B端令牌")
    @PostMapping("/refresh")
    public ApiResponse<StaffTokenVO> refresh(@Valid @RequestBody StaffRefreshTokenReq req) {
        return ApiResponse.ok(staffService.refresh(req.getRefreshToken()));
    }

    @Operation(operationId = "staffLogout", summary = "B端员工登出（作废全部刷新令牌）")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        staffService.logout();
        return ApiResponse.ok(null);
    }

    @Operation(operationId = "staffProfile", summary = "获取当前员工信息")
    @GetMapping("/profile")
    public ApiResponse<StaffProfileVO> profile() {
        return ApiResponse.ok(staffService.profile());
    }

    @Operation(operationId = "staffPermissions", summary = "获取当前员工角色与权限")
    @GetMapping("/permissions")
    public ApiResponse<StaffPermissionsVO> permissions() {
        return ApiResponse.ok(staffService.permissions());
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static String userAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua == null) {
            return "";
        }
        return ua.length() > 100 ? ua.substring(0, 100) : ua;
    }
}
