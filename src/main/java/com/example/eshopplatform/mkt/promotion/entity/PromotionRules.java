package com.example.eshopplatform.mkt.promotion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 促销规则表（配置化）
 * </p>
 *
 * @since 2026-09-06
 */
@Getter
@Setter
@ToString
@TableName("mkt_promotion_rules")
public class PromotionRules implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属促销ID
     */
    @TableField("promotion_id")
    private Long promotionId;

    /**
     * 所属商家ID
     */
    @TableField("merchant_id")
    private Long merchantId;

    /**
     * 规则名称
     */
    @TableField("rule_name")
    private String ruleName;

    /**
     * 1-无门槛 2-满金额 3-满件数 4-指定用户等级
     */
    @TableField("condition_type")
    private Byte conditionType;

    /**
     * 门槛值（分）
     */
    @TableField("condition_value")
    private Long conditionValue;

    /**
     * 优惠配置JSON：{"type":1,"value":3000} 或阶梯 {"type":2,"steps":[...]}
     */
    @TableField("benefit_config")
    private String benefitConfig;

    /**
     * 是否可与其他促销叠加 0-否 1-是
     */
    @TableField("is_stackable")
    private Byte isStackable;

    /**
     * 叠加组ID（同组内互斥，不同组可叠加）
     */
    @TableField("stack_group")
    private Integer stackGroup;

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

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 删除时间
     */
    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
