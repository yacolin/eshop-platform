package com.example.eshopplatform.sys.staff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * B端刷新令牌请求（POST /api/v1/staff/refresh）
 */
@Data
@Schema(description = "B端刷新令牌请求")
public class StaffRefreshTokenReq {

    @Schema(description = "刷新令牌")
    @NotBlank(message = "refresh_token 不能为空")
    private String refreshToken;
}
