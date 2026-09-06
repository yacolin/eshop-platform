package com.example.eshopplatform.sys.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 当前员工角色与权限标识（GET /api/v1/staff/permissions）
 */
@Data
@Schema(description = "B端当前员工角色与权限")
public class StaffPermissionsVO {

    @Schema(description = "角色名称列表")
    private List<String> roles = new ArrayList<>();

    @Schema(description = "权限标识列表（如 order:create）")
    private List<String> permissions = new ArrayList<>();
}
