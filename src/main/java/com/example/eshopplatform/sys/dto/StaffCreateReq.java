package com.example.eshopplatform.sys.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新增 B端员工请求（POST /api/v1/staff）
 */
@Data
@Schema(description = "新增B端员工请求")
public class StaffCreateReq {

    @Schema(description = "登录用户名（唯一）")
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过{max}")
    private String username;

    @Schema(description = "密码（bcrypt 存储）")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度需在{min}~{max}之间")
    private String password;

    @Schema(description = "真实姓名")
    @Size(max = 50, message = "真实姓名长度不能超过{max}")
    private String realName;

    @Schema(description = "邮箱")
    @Size(max = 100, message = "邮箱长度不能超过{max}")
    private String email;

    @Schema(description = "手机号")
    @Size(max = 20, message = "手机号长度不能超过{max}")
    private String phone;

    @Schema(description = "头像URL")
    @Size(max = 512, message = "头像URL长度不能超过{max}")
    private String avatar;

    /** 缺省启用 */
    @Schema(description = "true-正常 false-禁用（缺省启用）")
    private Boolean status;

    @Schema(description = "所属部门ID列表（缺省不分配）")
    private List<Long> departmentIds;
}
