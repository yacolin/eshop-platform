package com.example.eshopplatform.mkt.promotion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 促销详细信息（含规则和商品）——对齐 gf PromotionFullDetailRes。
 */
@Data
@Schema(description = "促销详细信息（含规则和商品）")
public class PromotionsDetailVO {

    @Schema(description = "促销头信息")
    private PromotionsVO promotion;

    @Schema(description = "促销规则（可能为空）")
    private PromoRuleVO rule;

    @Schema(description = "适用商品列表")
    private List<PromoProductVO> products = new ArrayList<>();
}
