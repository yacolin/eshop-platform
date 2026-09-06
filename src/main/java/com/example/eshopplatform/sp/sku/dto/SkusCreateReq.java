package com.example.eshopplatform.sp.sku.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * <p>
 * SKU规格表（具体可售单元）创建请求（CreateReq）
 * </p>
 *
 * <p>由代码生成器生成的基础 DTO：仅含业务字段，不含主键 id 与
 * created_at/updated_at/deleted_at 等服务端自动维护列。DB 必填列
 * （NOT NULL 且无默认值）已自动加最基础的空校验（String 用
 * {@code @NotBlank}、其余用 {@code @NotNull}）；String 列另按 DB 长度
 * 上限自动补 {@code @Size}（防超长入库存 500）；其余字段按业务裁剪补充。</p>
 *
 * @since 2026-09-06
 */
@Data
@Schema(description = "SKU规格表（具体可售单元）创建请求")
public class SkusCreateReq {
    /** 关联 products.id */
    @Schema(description = "关联 products.id")
    @NotNull(message = "关联 products.id不能为空")
    private Long productId;

    /** 所属商家ID */
    @Schema(description = "所属商家ID")
    private Long merchantId;

    /** 商家编码（唯一，用于ERP/WMS对接） */
    @Schema(description = "商家编码（唯一，用于ERP/WMS对接）")
    @NotBlank(message = "商家编码（唯一，用于ERP/WMS对接）不能为空")
    @Size(max = 100, message = "商家编码（唯一，用于ERP/WMS对接）长度不能超过{max}")
    private String skuCode;

    /** 条码/EAN/UPC（仓库扫描用，NULL表示无条码） */
    @Schema(description = "条码/EAN/UPC（仓库扫描用，NULL表示无条码）")
    @Size(max = 50, message = "条码/EAN/UPC（仓库扫描用，NULL表示无条码）长度不能超过{max}")
    private String barcode;

    /** 规格文本快照（如：红色 / 256G） */
    @Schema(description = "规格文本快照（如：红色 / 256G）")
    @Size(max = 500, message = "规格文本快照（如：红色 / 256G）长度不能超过{max}")
    private String specSummary;

    /** 销售价（分） */
    @Schema(description = "销售价（分）")
    @NotNull(message = "销售价（分）不能为空")
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
    @Size(max = 512, message = "SKU专属图（如不同颜色展示不同图片）长度不能超过{max}")
    private String image;

    /** 1-正常 0-禁用（如某规格暂时缺货下架） */
    @Schema(description = "1-正常 0-禁用（如某规格暂时缺货下架）")
    private Byte status;
}
