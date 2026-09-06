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
 * 促销适用商品表
 * </p>
 *
 * @since 2026-09-06
 */
@Getter
@Setter
@ToString
@TableName("mkt_promotion_products")
public class PromotionProducts implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 促销ID
     */
    @TableField("promotion_id")
    private Long promotionId;

    /**
     * 所属商家ID
     */
    @TableField("merchant_id")
    private Long merchantId;

    /**
     * 1-全站 2-指定分类 3-指定SPU 4-指定SKU
     */
    @TableField("product_type")
    private Byte productType;

    /**
     * 目标ID（SPU_ID或SKU_ID或Category_ID）
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 删除时间
     */
    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
