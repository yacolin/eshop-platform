package com.example.eshopplatform.sys.role.controller;

import com.example.eshopplatform.common.ApiResponse;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.sys.role.dto.RolesCreateReq;
import com.example.eshopplatform.sys.role.dto.RolesUpdateReq;
import com.example.eshopplatform.sys.role.dto.RolesVO;
import com.example.eshopplatform.sys.role.service.RolesService;
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
 * B端角色定义表基础 CRUD 接口（模板生成）
 * </p>
 *
 * <p>默认路径按“表名去前缀 + 短横线”生成（如 sp_brands → /api/v1/brands）。
 * 新增/更新请求体分别用 RolesCreateReq / RolesUpdateReq，
 * 写接口带 {@code @Valid} 触发 DTO 内校验注解。接入真实接口前请调整：如遇子资源/
 * 嵌套接口改更精确的路径、Req/VO 按接口用例裁剪校验、接口按端分组补 springdoc
 * @Tag，并把真实路径补入 application.yml 的 whitelist/admin-paths。</p>
 *
 * @since 2026-09-06
 */
@Tag(name = "RolesCrud", description = "B端角色定义表基础 CRUD")
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RolesController {

    private final RolesService rolesService;

    @Operation(operationId = "listRoles", summary = "分页查询B端角色定义表")
    @GetMapping
    public ApiResponse<PageResult<RolesVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String roleType,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(rolesService.page(page, size, roleType, name, status));
    }

    @Operation(operationId = "getRoles", summary = "查询B端角色定义表详情")
    @GetMapping("/{id}")
    public ApiResponse<RolesVO> get(@PathVariable Long id) {
        return ApiResponse.ok(rolesService.getById(id));
    }

    @Operation(operationId = "createRoles", summary = "新增B端角色定义表")
    @PostMapping
    public ApiResponse<RolesVO> create(@Valid @RequestBody RolesCreateReq req) {
        return ApiResponse.ok(rolesService.create(req));
    }

    @Operation(operationId = "updateRoles", summary = "更新B端角色定义表")
    @PutMapping("/{id}")
    public ApiResponse<RolesVO> update(@PathVariable Long id,
                                                     @Valid @RequestBody RolesUpdateReq req) {
        return ApiResponse.ok(rolesService.update(id, req));
    }

    @Operation(operationId = "deleteRoles", summary = "删除B端角色定义表")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        rolesService.delete(id);
        return ApiResponse.ok(null);
    }
}
