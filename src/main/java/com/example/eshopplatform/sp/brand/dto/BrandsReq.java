package com.example.eshopplatform.sp.brand.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 品牌表创建/更新请求（Req）
 * </p>
 *
 * <p>仅含业务字段（与 gf-eshop 品牌写接口对齐）；created_at/updated_at/deleted_at
 * 等自动列由服务端/数据库维护，不接收客户端输入。</p>
 *
 * @since 2026-09-03
 */
@Data
@Schema(description = "品牌表创建/更新请求")
public class BrandsReq {
    /** 品牌名称（如：苹果） */
    @Schema(description = "品牌名称（如：苹果）")
    private String name;

    /** 英文名 */
    @Schema(description = "英文名")
    private String englishName;

    /** 品牌Logo（CDN） */
    @Schema(description = "品牌Logo（CDN）")
    private String logoUrl;

    /** 首字母（A-Z，用于前台索引筛选） */
    @Schema(description = "首字母（A-Z，用于前台索引筛选）")
    private String firstLetter;

    /** 排序权重 */
    @Schema(description = "排序权重")
    private Integer sortOrder;

    /** 1-启用 0-禁用 */
    @Schema(description = "1-启用 0-禁用")
    private Byte status;

    /** 品牌故事 */
    @Schema(description = "品牌故事")
    private String description;
}
