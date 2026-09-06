package com.example.eshopplatform.mkt.promotion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 促销适用商品项（含 SPU 基础信息与 SKU 价格区间，对齐 gf PromotionProductItem）。
 */
@Data
@Schema(description = "促销适用商品项")
public class PromoProductVO {

    @Schema(description = "促销商品关联ID")
    private Long id;

    /** 1-全站 2-指定分类 3-指定SPU 4-指定SKU */
    @Schema(description = "适用类型：1-全站 2-指定分类 3-指定SPU 4-指定SKU")
    private Integer productType;

    @Schema(description = "目标ID（SPU/SKU/类目ID）")
    private Long productId;

    @Schema(description = "SPU名称")
    private String spuName;

    @Schema(description = "SPU副标题")
    private String subtitle;

    @Schema(description = "主图URL")
    private String mainImage;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "SKU最低价（分）")
    private Long minPrice;

    @Schema(description = "SKU最高价（分）")
    private Long maxPrice;

    @Schema(description = "总销量")
    private Integer salesCount;

    @Schema(description = "SPU状态（0-草稿 1-待审 2-已上架 3-已下架 4-违规封禁）")
    private Integer spuStatus;
}
