package com.example.eshopplatform.sp.sku.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

/**
 * <p>
 * SKU规格表（具体可售单元）视图对象（VO）
 * </p>
 *
 * <p>由代码生成器生成的基础 DTO：deleted_at 为服务端软删字段，不对业务端暴露；
 * 时间列（LocalDateTime）统一以 Long（epoch 毫秒）返回，避免各端解析字符串时间；
 * 接入真实接口时按业务裁剪。</p>
 *
 * @since 2026-09-06
 */
@Data
@Schema(description = "SKU规格表（具体可售单元）VO")
public class SkusVO {
    /** SKU ID */
    @Schema(description = "SKU ID")
    private Long id;

    /** 关联 products.id */
    @Schema(description = "关联 products.id")
    private Long productId;

    /** 所属商家ID */
    @Schema(description = "所属商家ID")
    private Long merchantId;

    /** 商家编码（唯一，用于ERP/WMS对接） */
    @Schema(description = "商家编码（唯一，用于ERP/WMS对接）")
    private String skuCode;

    /** 条码/EAN/UPC（仓库扫描用，NULL表示无条码） */
    @Schema(description = "条码/EAN/UPC（仓库扫描用，NULL表示无条码）")
    private String barcode;

    /** 规格文本快照（如：红色 / 256G） */
    @Schema(description = "规格文本快照（如：红色 / 256G）")
    private String specSummary;

    /** 销售价（分） */
    @Schema(description = "销售价（分）")
    private Long price;

    /** 划线价/市场价（分） */
    @Schema(description = "划线价/市场价（分）")
    private Long marketPrice;

    /** 成本价（分，仅后台可见） */
    @Schema(description = "成本价（分，仅后台可见）")
    private Long costPrice;

    /** 重量（克） */
    @Schema(description = "重量（克）")
    private BigDecimal weight;

    /** 体积（立方厘米） */
    @Schema(description = "体积（立方厘米）")
    private BigDecimal volume;

    /** 长（厘米） */
    @Schema(description = "长（厘米）")
    private BigDecimal length;

    /** 宽（厘米） */
    @Schema(description = "宽（厘米）")
    private BigDecimal width;

    /** 高（厘米） */
    @Schema(description = "高（厘米）")
    private BigDecimal height;

    /** 最少购买数量 */
    @Schema(description = "最少购买数量")
    private Integer minPurchaseQty;

    /** 最大购买数量（0=不限） */
    @Schema(description = "最大购买数量（0=不限）")
    private Integer maxPurchaseQty;

    /** SKU专属图（如不同颜色展示不同图片） */
    @Schema(description = "SKU专属图（如不同颜色展示不同图片）")
    private String image;

    /** 1-正常 0-禁用（如某规格暂时缺货下架） */
    @Schema(description = "1-正常 0-禁用（如某规格暂时缺货下架）")
    private Byte status;

    /** 创建时间（epoch 毫秒） */
    @Schema(description = "创建时间（epoch 毫秒）")
    private Long createdAt;

    /** 更新时间（epoch 毫秒） */
    @Schema(description = "更新时间（epoch 毫秒）")
    private Long updatedAt;
}
