package com.example.eshopplatform.sys.staff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * B端登录成功响应：令牌对 + 员工基本信息
 */
@Data
@Schema(description = "B端登录响应（令牌对 + 员工信息）")
public class StaffLoginVO {

    @Schema(description = "访问令牌")
    private String accessToken;

    /** access token 过期时间（秒） */
    @Schema(description = "access_token 过期时间（秒）")
    private Long expireIn;

    @Schema(description = "刷新令牌")
    private String refreshToken;

    /** refresh token 过期时间（秒） */
    @Schema(description = "refresh_token 过期时间（秒）")
    private Long refreshIn;

    /** 员工ID（sys_staff.id） */
    @Schema(description = "员工ID")
    private Long staffId;

    @Schema(description = "登录用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;
}
