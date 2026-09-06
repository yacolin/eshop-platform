package com.example.eshopplatform.sys.staff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 当前员工信息（GET /api/v1/staff/profile）
 */
@Data
@Schema(description = "B端当前员工信息")
public class StaffProfileVO {

    @Schema(description = "员工ID")
    private Long id;

    @Schema(description = "登录用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像URL")
    private String avatar;

    /** true-正常 false-禁用 */
    @Schema(description = "true-正常 false-禁用")
    private Boolean status;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    /** 所属部门ID列表（部门域业务落地后填充，当前恒为空） */
    @Schema(description = "所属部门ID列表")
    private List<Long> departmentIds = new ArrayList<>();

    /** 所属部门名称列表 */
    @Schema(description = "所属部门名称列表")
    private List<String> departmentNames = new ArrayList<>();
}
