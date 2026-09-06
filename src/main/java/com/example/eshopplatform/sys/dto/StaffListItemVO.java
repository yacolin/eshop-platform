package com.example.eshopplatform.sys.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * B端员工列表项（GET /api/v1/staff）：含角色与部门归属。
 */
@Data
@Schema(description = "B端员工列表项")
public class StaffListItemVO {

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

    /** epoch 毫秒 */
    @Schema(description = "最后登录时间（epoch 毫秒）")
    private Long lastLoginAt;

    /** epoch 毫秒 */
    @Schema(description = "创建时间（epoch 毫秒）")
    private Long createdAt;

    @Schema(description = "角色ID列表")
    private List<Long> roleIds = new ArrayList<>();

    @Schema(description = "角色名称列表")
    private List<String> roleNames = new ArrayList<>();

    @Schema(description = "部门ID列表")
    private List<Long> departmentIds = new ArrayList<>();

    @Schema(description = "部门名称列表")
    private List<String> departmentNames = new ArrayList<>();
}
