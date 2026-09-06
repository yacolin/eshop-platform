package com.example.eshopplatform.sp.attribute.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 属性字典表视图对象（VO）
 * </p>
 *
 * <p>由代码生成器生成的基础 DTO：deleted_at 为服务端软删字段，不对业务端暴露；
 * 时间列（LocalDateTime）统一以 Long（epoch 毫秒）返回，避免各端解析字符串时间；
 * 接入真实接口时按业务裁剪。</p>
 *
 * @since 2026-09-06
 */
@Data
@Schema(description = "属性字典表VO")
public class AttributesVO {
    private Long id;

    /** 属性名称（如：处理器、屏幕尺寸） */
    @Schema(description = "属性名称（如：处理器、屏幕尺寸）")
    private String name;

    /** 推荐归属类目（非强约束，仅用于后台管理视图） */
    @Schema(description = "推荐归属类目（非强约束，仅用于后台管理视图）")
    private Long categoryId;

    /** 1-文本 2-数值 3-颜色 */
    @Schema(description = "1-文本 2-数值 3-颜色")
    private Byte valueType;

    /** 是否参与前台筛选 */
    @Schema(description = "是否参与前台筛选")
    private Byte filterable;

    /** 单位（如：英寸、GB） */
    @Schema(description = "单位（如：英寸、GB）")
    private String unit;

    /** 1-必填（该属性在该类目下创建商品时必须填写） */
    @Schema(description = "1-必填（该属性在该类目下创建商品时必须填写）")
    private Byte required;

    /** 1-作为前台筛选条件（列表页筛选项来源） */
    @Schema(description = "1-作为前台筛选条件（列表页筛选项来源）")
    private Byte searchable;

    /** 1-是SKU规格（如颜色、内存） 0-仅SPU属性（如上市时间） */
    @Schema(description = "1-是SKU规格（如颜色、内存） 0-仅SPU属性（如上市时间）")
    private Byte isSkuSpec;

    private Integer sortOrder;

    /** 1-启用 0-禁用 */
    @Schema(description = "1-启用 0-禁用")
    private Byte status;

    /** 创建时间（epoch 毫秒） */
    @Schema(description = "创建时间（epoch 毫秒）")
    private Long createdAt;

    /** 更新时间（epoch 毫秒） */
    @Schema(description = "更新时间（epoch 毫秒）")
    private Long updatedAt;
}
