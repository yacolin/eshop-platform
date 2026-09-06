package com.example.eshopplatform.sp.attribute.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * <p>
 * 属性字典表更新请求（UpdateReq）
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
@Schema(description = "属性字典表更新请求")
public class AttributesUpdateReq {
    /** 属性名称（如：处理器、屏幕尺寸） */
    @Schema(description = "属性名称（如：处理器、屏幕尺寸）")
    @NotBlank(message = "属性名称（如：处理器、屏幕尺寸）不能为空")
    @Size(max = 100, message = "属性名称（如：处理器、屏幕尺寸）长度不能超过{max}")
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
    @Size(max = 20, message = "单位（如：英寸、GB）长度不能超过{max}")
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
}
