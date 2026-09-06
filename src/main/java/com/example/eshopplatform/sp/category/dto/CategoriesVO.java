package com.example.eshopplatform.sp.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 类目表（树状结构）视图对象（VO）
 * </p>
 *
 * <p>deleted_at 为服务端软删字段，不对业务端暴露。</p>
 *
 * @since 2026-09-03
 */
@Data
@Schema(description = "类目表（树状结构）VO")
public class CategoriesVO {
    private Long id;

    /** 类目名称（如：手机） */
    @Schema(description = "类目名称（如：手机）")
    private String name;

    /** 父级ID（0表示根节点） */
    @Schema(description = "父级ID（0表示根节点）")
    private Long parentId;

    /** 层级（1-3级） */
    @Schema(description = "层级（1-3级）")
    private Byte level;

    /** 路径（如：1/23/45/） */
    @Schema(description = "路径（如：1/23/45/）")
    private String path;

    /** 类目图标 */
    @Schema(description = "类目图标")
    private String iconUrl;

    /** 排序 */
    @Schema(description = "排序")
    private Integer sortOrder;

    /** 1-启用 0-禁用 */
    @Schema(description = "1-启用 0-禁用")
    private Byte status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
