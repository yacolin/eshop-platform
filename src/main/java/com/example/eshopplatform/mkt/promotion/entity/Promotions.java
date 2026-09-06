package com.example.eshopplatform.mkt.promotion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import com.baomidou.mybatisplus.annotation.TableLogic;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 统一促销活动表
 * </p>
 *
 * @since 2026-09-06
 */
@Getter
@Setter
@ToString
@TableName("mkt_promotions")
public class Promotions implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 促销ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 促销业务编号
     */
    @TableField("promotion_no")
    private String promotionNo;

    /**
     * 所属商家ID（0表示平台级活动）
     */
    @TableField("merchant_id")
    private Long merchantId;

    /**
     * 活动名称
     */
    @TableField("promo_name")
    private String promoName;

    /**
     * 1-满减券 2-折扣券 3-秒杀 4-满额减 5-满件折 6-会员价
     */
    @TableField("promo_type")
    private Byte promoType;

    /**
     * 优惠码（优惠券专用）
     */
    @TableField("promo_code")
    private String promoCode;

    /**
     * 优惠码唯一键辅助列（空串视为NULL，非券类活动不占唯一键）
     */
    @TableField("promo_code_uq")
    private String promoCodeUq;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 发行总量（0表示不限）
     */
    @TableField("total_quantity")
    private Integer totalQuantity;

    /**
     * 每人限领/限购数量
     */
    @TableField("per_user_limit")
    private Integer perUserLimit;

    /**
     * 已使用/已售数量（异步统计，非实时）
     */
    @TableField("used_quantity")
    private Integer usedQuantity;

    /**
     * 关联规则表ID
     */
    @TableField("rule_id")
    private Long ruleId;

    /**
     * 1-草稿 2-待生效 3-生效中 4-已暂停 5-已结束 6-已作废
     */
    @TableField("status")
    private Byte status;

    /**
     * 优先级（数字越大越优先，同类型互斥）
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 创建人
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 更新人
     */
    @TableField("updated_by")
    private Long updatedBy;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
