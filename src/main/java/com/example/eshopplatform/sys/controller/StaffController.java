package com.example.eshopplatform.sys.controller;

import com.example.eshopplatform.common.ApiResponse;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.sys.dto.StaffAssignRolesReq;
import com.example.eshopplatform.sys.dto.StaffCreateReq;
import com.example.eshopplatform.sys.dto.StaffListItemVO;
import com.example.eshopplatform.sys.dto.StaffLoginReq;
import com.example.eshopplatform.sys.dto.StaffLoginVO;
import com.example.eshopplatform.sys.dto.StaffPermissionsVO;
import com.example.eshopplatform.sys.dto.StaffProfileVO;
import com.example.eshopplatform.sys.dto.StaffRefreshTokenReq;
import com.example.eshopplatform.sys.dto.StaffTokenVO;
import com.example.eshopplatform.sys.dto.StaffUpdateReq;
import com.example.eshopplatform.sys.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    // ==================== 员工管理（写操作需管理员） ====================

    @Operation(operationId = "listStaff", summary = "员工分页列表（含角色/部门归属）")
    @GetMapping
    public ApiResponse<PageResult<StaffListItemVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(staffService.page(page, size, keyword, status));
    }

    @Operation(operationId = "createStaff", summary = "新增员工")
    @PostMapping
    public ApiResponse<StaffListItemVO> create(@Valid @RequestBody StaffCreateReq req) {
        return ApiResponse.ok(staffService.create(req));
    }

    @Operation(operationId = "updateStaff", summary = "更新员工")
    @PutMapping("/{id}")
    public ApiResponse<StaffListItemVO> update(@PathVariable Long id,
                                               @Valid @RequestBody StaffUpdateReq req) {
        return ApiResponse.ok(staffService.update(id, req));
    }

    @Operation(operationId = "deleteStaff", summary = "删除员工（逻辑删除）")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        staffService.delete(id);
        return ApiResponse.ok(null);
    }

    @Operation(operationId = "assignStaffRoles", summary = "分配员工角色（整表替换）")
    @PutMapping("/{id}/roles")
    public ApiResponse<Void> assignRoles(@PathVariable Long id,
                                         @Valid @RequestBody StaffAssignRolesReq req) {
        staffService.assignRoles(id, req);
        return ApiResponse.ok(null);
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
