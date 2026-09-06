package com.example.eshopplatform.sp.attributeValue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * <p>
 * 属性值字典表更新请求（UpdateReq）
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
@Schema(description = "属性值字典表更新请求")
public class AttributeValuesUpdateReq {
    /** 关联属性ID */
    @Schema(description = "关联属性ID")
    @NotNull(message = "关联属性ID不能为空")
    private Long attributeId;

    /** 属性值（如：256G、红色） */
    @Schema(description = "属性值（如：256G、红色）")
    @NotBlank(message = "属性值（如：256G、红色）不能为空")
    @Size(max = 200, message = "属性值（如：256G、红色）长度不能超过{max}")
    private String value;

    /** 别名列表，如["深空灰","黑灰"]，用于搜索纠错、模糊匹配 */
    @Schema(description = "别名列表，如[\"深空灰\",\"黑灰\"]，用于搜索纠错、模糊匹配")
    private String alias;

    /** 搜索权重（值越大匹配优先级越高） */
    @Schema(description = "搜索权重（值越大匹配优先级越高）")
    private Integer searchWeight;

    /** 数值型值（用于区间筛选） */
    @Schema(description = "数值型值（用于区间筛选）")
    private BigDecimal numericValue;

    /** 颜色色值（#FF0000） */
    @Schema(description = "颜色色值（#FF0000）")
    @Size(max = 10, message = "颜色色值（#FF0000）长度不能超过{max}")
    private String colorHex;

    /** 排序权重 */
    @Schema(description = "排序权重")
    private Integer sortOrder;

    /** 1-启用 0-禁用 */
    @Schema(description = "1-启用 0-禁用")
    private Byte status;
}
