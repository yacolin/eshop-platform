package com.example.eshopplatform.mkt.promotion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 统一促销活动表视图对象（VO）
 * </p>
 *
 * <p>由代码生成器生成的基础 DTO：deleted_at 为服务端软删字段，不对业务端暴露；
 * 时间列（LocalDateTime）统一以 Long（epoch 毫秒）返回，避免各端解析字符串时间；
 * 接入真实接口时按业务裁剪。</p>
 *
 * @since 2026-09-06
 */
@Data
@Schema(description = "统一促销活动表VO")
public class PromotionsVO {
    /** 促销ID */
    @Schema(description = "促销ID")
    private Long id;

    /** 促销业务编号 */
    @Schema(description = "促销业务编号")
    private String promotionNo;

    /** 所属商家ID（0表示平台级活动） */
    @Schema(description = "所属商家ID（0表示平台级活动）")
    private Long merchantId;

    /** 活动名称 */
    @Schema(description = "活动名称")
    private String promoName;

    /** 1-满减券 2-折扣券 3-秒杀 4-满额减 5-满件折 6-会员价 */
    @Schema(description = "1-满减券 2-折扣券 3-秒杀 4-满额减 5-满件折 6-会员价")
    private Byte promoType;

    /** 优惠码（优惠券专用） */
    @Schema(description = "优惠码（优惠券专用）")
    private String promoCode;

    /** 优惠码唯一键辅助列（空串视为NULL，非券类活动不占唯一键） */
    @Schema(description = "优惠码唯一键辅助列（空串视为NULL，非券类活动不占唯一键）")
    private String promoCodeUq;

    /** 开始时间（epoch 毫秒） */
    @Schema(description = "开始时间（epoch 毫秒）")
    private Long startTime;

    /** 结束时间（epoch 毫秒） */
    @Schema(description = "结束时间（epoch 毫秒）")
    private Long endTime;

    /** 发行总量（0表示不限） */
    @Schema(description = "发行总量（0表示不限）")
    private Integer totalQuantity;

    /** 每人限领/限购数量 */
    @Schema(description = "每人限领/限购数量")
    private Integer perUserLimit;

    /** 已使用/已售数量（异步统计，非实时） */
    @Schema(description = "已使用/已售数量（异步统计，非实时）")
    private Integer usedQuantity;

    /** 关联规则表ID */
    @Schema(description = "关联规则表ID")
    private Long ruleId;

    /** 1-草稿 2-待生效 3-生效中 4-已暂停 5-已结束 6-已作废 */
    @Schema(description = "1-草稿 2-待生效 3-生效中 4-已暂停 5-已结束 6-已作废")
    private Byte status;

    /** 优先级（数字越大越优先，同类型互斥） */
    @Schema(description = "优先级（数字越大越优先，同类型互斥）")
    private Integer priority;

    /** 创建人 */
    @Schema(description = "创建人")
    private Long createdBy;

    /** 更新人 */
    @Schema(description = "更新人")
    private Long updatedBy;

    /** 创建时间（epoch 毫秒） */
    @Schema(description = "创建时间（epoch 毫秒）")
    private Long createdAt;

    /** 更新时间（epoch 毫秒） */
    @Schema(description = "更新时间（epoch 毫秒）")
    private Long updatedAt;
}
