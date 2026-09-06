package com.example.eshopplatform.sys.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * B端权限表视图对象（VO）
 * </p>
 *
 * <p>由代码生成器生成的基础 DTO：deleted_at 为服务端软删字段，不对业务端暴露；
 * 时间列（LocalDateTime）统一以 Long（epoch 毫秒）返回，避免各端解析字符串时间；
 * 接入真实接口时按业务裁剪。</p>
 *
 * @since 2026-09-06
 */
@Data
@Schema(description = "B端权限表VO")
public class PermissionsVO {
    /** 主键 */
    @Schema(description = "主键")
    private Long id;

    /** 权限标识（唯一，如 order:create） */
    @Schema(description = "权限标识（唯一，如 order:create）")
    private String name;

    /** 权限显示名称 */
    @Schema(description = "权限显示名称")
    private String displayName;

    /** 权限描述 */
    @Schema(description = "权限描述")
    private String description;

    /** 资源（如 order/product/user） */
    @Schema(description = "资源（如 order/product/user）")
    private String resource;

    /** 操作（如 create/read/update/delete） */
    @Schema(description = "操作（如 create/read/update/delete）")
    private String action;

    /** 父级ID（0=根节点，支持菜单/按钮树形层级） */
    @Schema(description = "父级ID（0=根节点，支持菜单/按钮树形层级）")
    private Long parentId;

    /** 分类（如 business/system/admin） */
    @Schema(description = "分类（如 business/system/admin）")
    private String category;

    /** 排序值 */
    @Schema(description = "排序值")
    private Integer sortOrder;

    /** 1-启用 0-禁用 */
    @Schema(description = "1-启用 0-禁用")
    private Boolean status;

    /** 创建时间（epoch 毫秒） */
    @Schema(description = "创建时间（epoch 毫秒）")
    private Long createdAt;

    /** 更新时间（epoch 毫秒） */
    @Schema(description = "更新时间（epoch 毫秒）")
    private Long updatedAt;
}
