package com.example.eshopplatform.sys.staff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 刷新令牌响应（仅令牌对）
 */
@Data
@Schema(description = "刷新令牌响应")
public class StaffTokenVO {

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "access_token 过期时间（秒）")
    private Long expireIn;

    @Schema(description = "刷新令牌")
    private String refreshToken;

    @Schema(description = "refresh_token 过期时间（秒）")
    private Long refreshIn;
}
