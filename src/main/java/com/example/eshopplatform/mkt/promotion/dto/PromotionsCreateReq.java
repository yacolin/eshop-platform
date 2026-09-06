package com.example.eshopplatform.mkt.promotion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 新增促销请求（对齐 gf PromotionCreateReq：头信息 + 规则 + 适用商品）。
 * 服务端自动处理：promotion_no 生成、merchant_id 默认 0（平台）、rule 落 mkt_promotion_rules
 * 并回写 rule_id、product_ids 落 mkt_promotion_products（product_type=3）。
 */
@Data
@Schema(description = "新增促销请求")
public class PromotionsCreateReq {

    @Schema(description = "所属商家ID（0=平台级活动，缺省0）")
    private Long merchantId;

    @Schema(description = "活动名称")
    @NotNull(message = "活动名称不能为空")
    @Size(max = 100, message = "活动名称长度不能超过{max}")
    private String promoName;

    /** 1-满减券 2-折扣券 3-秒杀 4-满额减 5-满件折 6-会员价 */
    @Schema(description = "促销类型：1-满减券 2-折扣券 3-秒杀 4-满额减 5-满件折 6-会员价")
    @NotNull(message = "促销类型不能为空")
    private Byte promoType;

    @Schema(description = "优惠码（优惠券专用，非券类留空）")
    @Size(max = 50, message = "优惠码长度不能超过{max}")
    private String promoCode;

    @Schema(description = "开始时间")
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "发行总量（0=不限）")
    private Integer totalQuantity;

    @Schema(description = "每人限领/限购数量（0=不限）")
    private Integer perUserLimit;

    @Schema(description = "优先级（数字越大越优先）")
    private Integer priority;

    // ---------- 规则 ----------

    @Schema(description = "规则名称")
    @Size(max = 100, message = "规则名称长度不能超过{max}")
    private String ruleName;

    /** 1-无门槛 2-满金额 3-满件数 4-指定用户等级 */
    @Schema(description = "触发条件：1-无门槛 2-满金额 3-满件数 4-指定用户等级")
    private Byte conditionType;

    @Schema(description = "门槛值（分）")
    private Long conditionValue;

    /** 1-减固定金额 2-打折扣 3-赠品 4-免运费 5-送积分 */
    @Schema(description = "优惠类型：1-减固定金额 2-打折扣 3-赠品 4-免运费 5-送积分（>0 才创建规则）")
    private Integer benefitType;

    @Schema(description = "优惠值（分/千分比）")
    private Long benefitValue;

    @Schema(description = "是否可与其他促销叠加 0-否 1-是")
    private Byte isStackable;

    @Schema(description = "叠加组ID")
    private Integer stackPriority;

    // ---------- 适用商品 ----------

    @Schema(description = "适用 SPU ID 列表（product_type=3）")
    private List<Long> productIds;
}
