package com.example.eshopplatform.sp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 类目-属性关联表（推荐模板）
 * </p>
 *
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@TableName("sp_category_attributes")
public class CategoryAttributes implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 类目ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 属性ID
     */
    @TableField("attribute_id")
    private Long attributeId;

    /**
     * 该类目下是否必填（仅提示，非强校验）
     */
    @TableField("required")
    private Byte required;

    /**
     * 是否作为前台默认筛选项
     */
    @TableField("is_default_filter")
    private Byte isDefaultFilter;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
