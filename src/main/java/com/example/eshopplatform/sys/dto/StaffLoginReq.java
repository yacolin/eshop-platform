package com.example.eshopplatform.sys.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * B端员工登录请求（POST /api/v1/staff/login）
 */
@Data
@Schema(description = "B端员工登录请求")
public class StaffLoginReq {

    @Schema(description = "登录用户名")
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过{max}")
    private String username;

    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    @Size(max = 100, message = "密码长度不能超过{max}")
    private String password;
}
