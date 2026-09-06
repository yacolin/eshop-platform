package com.example.eshopplatform.sys.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 更新 B端员工请求（PUT /api/v1/staff/{id}）。
 * 语义：null 字段保留原值；username 不允许修改；password 空/缺省 = 不重置，
 * 非空则重置为新密码；departmentIds 缺省 = 不变，传入（可为空数组）= 替换部门归属。
 */
@Data
@Schema(description = "更新B端员工请求")
public class StaffUpdateReq {

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

    @Schema(description = "true-正常 false-禁用")
    private Boolean status;

    @Schema(description = "重置密码：非空则修改，空字符串/缺省表示不修改")
    @Size(min = 6, max = 100, message = "密码长度需在{min}~{max}之间")
    private String password;

    @Schema(description = "所属部门ID列表（缺省不变；传[]表示清空）")
    private List<Long> departmentIds;
}
