package com.example.eshopplatform.mkt.promotion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 促销规则 VO（benefit_config JSON 解出 benefitType/benefitValue 便捷字段）。
 */
@Data
@Schema(description = "促销规则")
public class PromoRuleVO {

    @Schema(description = "规则ID")
    private Long id;

    @Schema(description = "所属促销ID")
    private Long promotionId;

    @Schema(description = "规则名称")
    private String ruleName;

    /** 1-无门槛 2-满金额 3-满件数 4-指定用户等级 */
    @Schema(description = "触发条件：1-无门槛 2-满金额 3-满件数 4-指定用户等级")
    private Byte conditionType;

    @Schema(description = "门槛值（分）")
    private Long conditionValue;

    /** 简单优惠类型（取自 benefit_config.type） */
    @Schema(description = "优惠类型：1-减固定金额 2-打折扣 3-赠品 4-免运费 5-送积分（简单型）")
    private Integer benefitType;

    /** 简单优惠值（取自 benefit_config.value，分/千分比） */
    @Schema(description = "优惠值（分/千分比，简单型）")
    private Long benefitValue;

    @Schema(description = "是否可与其他促销叠加 0-否 1-是")
    private Byte isStackable;

    @Schema(description = "叠加组ID")
    private Integer stackGroup;
}
