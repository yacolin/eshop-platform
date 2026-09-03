package com.example.eshopplatform.sp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 类目树节点（含子级列表，供类目/商品级联选择）
 * </p>
 *
 * <p>字段与 {@link CategoriesVO} 一致并附带 children；deleted_at 为服务端
 * 软删字段，不对业务端暴露。</p>
 *
 * @since 2026-09-03
 */
@Data
@Schema(description = "类目树节点")
public class CategoriesTreeVO {
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

    /** 子级类目 */
    @Schema(description = "子级类目")
    private List<CategoriesTreeVO> children = new ArrayList<>();
}
