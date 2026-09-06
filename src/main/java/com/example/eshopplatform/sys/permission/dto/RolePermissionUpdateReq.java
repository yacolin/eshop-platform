package com.example.eshopplatform.sys.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 替换角色权限请求（PUT /api/v1/permissions/roles/{roleId}）——对齐 gf-eshop：
 * body 传 permissionIds（权限 id 列表，空数组=清空该角色全部授权）。
 */
@Data
@Schema(description = "替换角色权限请求")
public class RolePermissionUpdateReq {

    @Schema(description = "权限ID列表（空数组=清空全部授权）")
    @NotNull(message = "permissionIds 不能为 null（可为空数组）")
    private List<Long> permissionIds;
}
