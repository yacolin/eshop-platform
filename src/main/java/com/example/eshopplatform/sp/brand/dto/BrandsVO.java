package com.example.eshopplatform.sp.brand.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 品牌表视图对象（VO）
 * </p>
 *
 * <p>由代码生成器生成的基础 DTO：字段与表结构一致；接入真实接口时按业务裁剪
 * （隐藏内部字段、时间转 epoch 毫秒等）。deleted_at 为服务端软删字段，
 * 不对业务端暴露。</p>
 *
 * @since 2026-09-03
 */
@Data
@Schema(description = "品牌表VO")
public class BrandsVO {
    private Long id;

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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
