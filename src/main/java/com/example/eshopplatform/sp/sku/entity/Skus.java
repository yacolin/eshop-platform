package com.example.eshopplatform.sp.sku.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import com.baomidou.mybatisplus.annotation.TableLogic;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * SKU规格表（具体可售单元）
 * </p>
 *
 * @since 2026-09-06
 */
@Getter
@Setter
@ToString
@TableName("sp_skus")
public class Skus implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联 products.id
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 所属商家ID
     */
    @TableField("merchant_id")
    private Long merchantId;

    /**
     * 商家编码（唯一，用于ERP/WMS对接）
     */
    @TableField("sku_code")
    private String skuCode;

    /**
     * 条码/EAN/UPC（仓库扫描用，NULL表示无条码）
     */
    @TableField("barcode")
    private String barcode;

    /**
     * 规格文本快照（如：红色 / 256G）
     */
    @TableField("spec_summary")
    private String specSummary;

    /**
     * 销售价（分）
     */
    @TableField("price")
    private Long price;

    /**
     * 划线价/市场价（分）
     */
    @TableField("market_price")
    private Long marketPrice;

    /**
     * 成本价（分，仅后台可见）
     */
    @TableField("cost_price")
    private Long costPrice;

    /**
     * 重量（克）
     */
    @TableField("weight")
    private BigDecimal weight;

    /**
     * 体积（立方厘米）
     */
    @TableField("volume")
    private BigDecimal volume;

    /**
     * 长（厘米）
     */
    @TableField("length")
    private BigDecimal length;

    /**
     * 宽（厘米）
     */
    @TableField("width")
    private BigDecimal width;

    /**
     * 高（厘米）
     */
    @TableField("height")
    private BigDecimal height;

    /**
     * 最少购买数量
     */
    @TableField("min_purchase_qty")
    private Integer minPurchaseQty;

    /**
     * 最大购买数量（0=不限）
     */
    @TableField("max_purchase_qty")
    private Integer maxPurchaseQty;

    /**
     * SKU专属图（如不同颜色展示不同图片）
     */
    @TableField("image")
    private String image;

    /**
     * 1-正常 0-禁用（如某规格暂时缺货下架）
     */
    @TableField("status")
    private Byte status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
