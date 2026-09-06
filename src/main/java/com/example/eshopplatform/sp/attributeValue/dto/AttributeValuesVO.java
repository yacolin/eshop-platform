package com.example.eshopplatform.sp.attributeValue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

/**
 * <p>
 * 属性值字典表视图对象（VO）
 * </p>
 *
 * <p>由代码生成器生成的基础 DTO：deleted_at 为服务端软删字段，不对业务端暴露；
 * 时间列（LocalDateTime）统一以 Long（epoch 毫秒）返回，避免各端解析字符串时间；
 * 接入真实接口时按业务裁剪。</p>
 *
 * @since 2026-09-06
 */
@Data
@Schema(description = "属性值字典表VO")
public class AttributeValuesVO {
    private Long id;

    /** 关联属性ID */
    @Schema(description = "关联属性ID")
    private Long attributeId;

    /** 属性值（如：256G、红色） */
    @Schema(description = "属性值（如：256G、红色）")
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
    private String colorHex;

    /** 排序权重 */
    @Schema(description = "排序权重")
    private Integer sortOrder;

    /** 1-启用 0-禁用 */
    @Schema(description = "1-启用 0-禁用")
    private Byte status;

    /** 创建时间（epoch 毫秒） */
    @Schema(description = "创建时间（epoch 毫秒）")
    private Long createdAt;
}
