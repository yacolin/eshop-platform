package com.example.eshopplatform.sys.permission.controller;

import com.example.eshopplatform.common.ApiResponse;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.sys.permission.dto.PermissionCheckReq;
import com.example.eshopplatform.sys.permission.dto.PermissionsCreateReq;
import com.example.eshopplatform.sys.permission.dto.PermissionsUpdateReq;
import com.example.eshopplatform.sys.permission.dto.PermissionsVO;
import com.example.eshopplatform.sys.permission.dto.RolePermissionUpdateReq;
import com.example.eshopplatform.sys.permission.service.PermissionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * B端权限表基础 CRUD 接口（模板生成）
 * </p>
 *
 * <p>默认路径按“表名去前缀 + 短横线”生成（如 sp_brands → /api/v1/brands）。
 * 新增/更新请求体分别用 PermissionsCreateReq / PermissionsUpdateReq，
 * 写接口带 {@code @Valid} 触发 DTO 内校验注解。接入真实接口前请调整：如遇子资源/
 * 嵌套接口改更精确的路径、Req/VO 按接口用例裁剪校验、接口按端分组补 springdoc
 * @Tag，并把真实路径补入 application.yml 的 whitelist/admin-paths。</p>
 *
 * @since 2026-09-06
 */
@Tag(name = "PermissionsCrud", description = "B端权限表基础 CRUD")
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionsController {

    private final PermissionsService permissionsService;

    @Operation(operationId = "listPermissions", summary = "分页查询B端权限表")
    @GetMapping
    public ApiResponse<PageResult<PermissionsVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(permissionsService.page(page, size, resource, action, category, status));
    }

    @Operation(operationId = "getPermission", summary = "查询B端权限表详情")
    @GetMapping("/{id}")
    public ApiResponse<PermissionsVO> get(@PathVariable Long id) {
        return ApiResponse.ok(permissionsService.getById(id));
    }

    @Operation(operationId = "createPermission", summary = "新增B端权限表")
    @PostMapping
    public ApiResponse<PermissionsVO> create(@Valid @RequestBody PermissionsCreateReq req) {
        return ApiResponse.ok(permissionsService.create(req));
    }

    @Operation(operationId = "updatePermission", summary = "更新B端权限表")
    @PutMapping("/{id}")
    public ApiResponse<PermissionsVO> update(@PathVariable Long id,
                                                     @Valid @RequestBody PermissionsUpdateReq req) {
        return ApiResponse.ok(permissionsService.update(id, req));
    }

    @Operation(operationId = "deletePermission", summary = "删除B端权限表")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        permissionsService.delete(id);
        return ApiResponse.ok(null);
    }

    @Operation(operationId = "getPermissionsByRoleId", summary = "根据角色ID查询权限列表")
    @GetMapping("/roles/{roleId}")
    public ApiResponse<PermissionsVO[]> getPermissionsByRoleId(@PathVariable Long roleId) {
        return ApiResponse.ok(permissionsService.getPermissionsByRoleId(roleId));
    }

    @Operation(operationId = "putPermissionsByRoleId", summary = "根据角色ID替换权限列表")
    @PutMapping("/roles/{roleId}")
    public ApiResponse<Void> putPermissionsByRoleId(@PathVariable Long roleId,
                                                    @Valid @RequestBody RolePermissionUpdateReq req) {
        permissionsService.putPermissionsByRoleId(roleId, req.getPermissionIds());
        return ApiResponse.ok(null);
    }

    @Operation(operationId = "checkUserPermissions", summary = "校验当前员工是否拥有指定权限")
    @PostMapping("/check")
    public ApiResponse<Boolean> checkUserPermissions(@Valid @RequestBody PermissionCheckReq req) {
        return ApiResponse.ok(permissionsService.checkUserPermissions(req.getPermission()));
    }
}
