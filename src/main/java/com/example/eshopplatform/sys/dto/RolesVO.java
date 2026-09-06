package com.example.eshopplatform.sys.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * B端角色定义表视图对象（VO）
 * </p>
 *
 * <p>由代码生成器生成的基础 DTO：deleted_at 为服务端软删字段，不对业务端暴露；
 * 时间列（LocalDateTime）统一以 Long（epoch 毫秒）返回，避免各端解析字符串时间；
 * 接入真实接口时按业务裁剪。</p>
 *
 * @since 2026-09-06
 */
@Data
@Schema(description = "B端角色定义表VO")
public class RolesVO {
    /** 主键 */
    @Schema(description = "主键")
    private Long id;

    /** 角色名称（唯一，如 admin/editor/vip） */
    @Schema(description = "角色名称（唯一，如 admin/editor/vip）")
    private String name;

    /** 角色显示名称 */
    @Schema(description = "角色显示名称")
    private String displayName;

    /** 角色描述 */
    @Schema(description = "角色描述")
    private String description;

    /** builtin-系统内置 custom-自定义 */
    @Schema(description = "builtin-系统内置 custom-自定义")
    private String roleType;

    /** 排序值 */
    @Schema(description = "排序值")
    private Integer sortOrder;

    /** true-启用 false-禁用（DB tinyint(1) 1/0） */
    @Schema(description = "true-启用 false-禁用（DB tinyint(1) 1/0）")
    private Boolean status;

    /** 创建时间（epoch 毫秒） */
    @Schema(description = "创建时间（epoch 毫秒）")
    private Long createdAt;

    /** 更新时间（epoch 毫秒） */
    @Schema(description = "更新时间（epoch 毫秒）")
    private Long updatedAt;
}
