package com.example.eshopplatform.sp.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 类目关联品牌请求（全量替换该类目下的品牌关联）
 * </p>
 *
 * <p>brandIds 为替换后的完整品牌集合（不能为空，替换语义：先删旧关联再按序插入）；
 * sortOrder 应用于本次插入的全部关联。</p>
 *
 * @since 2026-09-03
 */
@Data
@Schema(description = "类目关联品牌请求")
public class CategoryBrandUpdateReq {

    /** 品牌ID列表（替换后的完整集合） */
    @Schema(description = "品牌ID列表（不能为空）")
    private List<Long> brandIds;

    /** 排序权重 */
    @Schema(description = "排序权重")
    private Integer sortOrder;
}
