package com.example.eshopplatform.sys.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 权限校验请求（POST /api/v1/permissions/check）——对齐 gf-eshop：单权限标识校验。
 */
@Data
@Schema(description = "权限校验请求")
public class PermissionCheckReq {

    @Schema(description = "权限标识，如 order:create")
    @NotBlank(message = "permission 不能为空")
    @Size(max = 100, message = "permission 长度不能超过{max}")
    private String permission;
}
