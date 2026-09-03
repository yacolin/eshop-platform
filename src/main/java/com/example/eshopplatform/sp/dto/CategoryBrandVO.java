package com.example.eshopplatform.sp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 类目下品牌（关联记录 + 品牌详情）
 * </p>
 *
 * @since 2026-09-03
 */
@Data
@Schema(description = "类目下品牌项")
public class CategoryBrandVO {
    /** 关联记录ID */
    private Long id;

    /** 类目ID */
    private Long categoryId;

    /** 品牌ID */
    private Long brandId;

    /** 关联排序权重 */
    private Integer sortOrder;

    /** 品牌名称 */
    private String brandName;

    /** 品牌英文名 */
    private String englishName;

    /** 品牌Logo */
    private String logoUrl;

    /** 品牌首字母 */
    private String firstLetter;
}
