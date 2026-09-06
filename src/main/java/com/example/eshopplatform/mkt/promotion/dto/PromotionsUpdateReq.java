package com.example.eshopplatform.mkt.promotion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 更新促销请求（对齐 gf PromotionUpdateReq：头信息部分字段 + 规则；promoType/商家/商品列表不可改）。
 * 规则仅当 benefitType>0 时更新/创建（新增则回写 header.rule_id）。
 */
@Data
@Schema(description = "更新促销请求（null 字段保留原值）")
public class PromotionsUpdateReq {

    @Schema(description = "活动名称")
    @Size(max = 100, message = "活动名称长度不能超过{max}")
    private String promoName;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "发行总量（0=不限）")
    private Integer totalQuantity;

    @Schema(description = "每人限领/限购数量（0=不限）")
    private Integer perUserLimit;

    /** 1-草稿 2-待生效 3-生效中 4-已暂停 5-已结束 6-已作废 */
    @Schema(description = "状态：1-草稿 2-待生效 3-生效中 4-已暂停 5-已结束 6-已作废")
    private Byte status;

    @Schema(description = "优先级（数字越大越优先）")
    private Integer priority;

    // ---------- 规则（benefitType>0 才生效） ----------

    @Schema(description = "规则名称")
    @Size(max = 100, message = "规则名称长度不能超过{max}")
    private String ruleName;

    @Schema(description = "触发条件：1-无门槛 2-满金额 3-满件数 4-指定用户等级")
    private Byte conditionType;

    @Schema(description = "门槛值（分）")
    private Long conditionValue;

    @Schema(description = "优惠类型：1-减固定金额 2-打折扣 3-赠品 4-免运费 5-送积分")
    private Integer benefitType;

    @Schema(description = "优惠值（分/千分比）")
    private Long benefitValue;

    @Schema(description = "是否可与其他促销叠加 0-否 1-是")
    private Byte isStackable;

    @Schema(description = "叠加组ID")
    private Integer stackPriority;
}
