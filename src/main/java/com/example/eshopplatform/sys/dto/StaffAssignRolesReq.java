package com.example.eshopplatform.sys.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 分配员工角色请求（PUT /api/v1/staff/{id}/roles）——整表替换，空数组=清空角色。
 */
@Data
@Schema(description = "分配员工角色请求")
public class StaffAssignRolesReq {

    @Schema(description = "角色ID列表（空数组=清空全部角色）")
    @NotNull(message = "roleIds 不能为 null（可为空数组）")
    private List<Long> roleIds;
}
