package com.example.eshopplatform.sp.attributeValue.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 属性值字典表
 * </p>
 *
 * @since 2026-09-06
 */
@Getter
@Setter
@ToString
@TableName("sp_attribute_values")
public class AttributeValues implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联属性ID
     */
    @TableField("attribute_id")
    private Long attributeId;

    /**
     * 属性值（如：256G、红色）
     */
    @TableField("value")
    private String value;

    /**
     * 别名列表，如["深空灰","黑灰"]，用于搜索纠错、模糊匹配
     */
    @TableField("alias")
    private String alias;

    /**
     * 搜索权重（值越大匹配优先级越高）
     */
    @TableField("search_weight")
    private Integer searchWeight;

    /**
     * 数值型值（用于区间筛选）
     */
    @TableField("numeric_value")
    private BigDecimal numericValue;

    /**
     * 颜色色值（#FF0000）
     */
    @TableField("color_hex")
    private String colorHex;

    /**
     * 排序权重
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 1-启用 0-禁用
     */
    @TableField("status")
    private Byte status;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
