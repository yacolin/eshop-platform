package com.example.eshopplatform.sp.attribute.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import com.baomidou.mybatisplus.annotation.TableLogic;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 属性字典表
 * </p>
 *
 * @since 2026-09-06
 */
@Getter
@Setter
@ToString
@TableName("sp_attributes")
public class Attributes implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 属性名称（如：处理器、屏幕尺寸）
     */
    @TableField("name")
    private String name;

    /**
     * 推荐归属类目（非强约束，仅用于后台管理视图）
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 1-文本 2-数值 3-颜色
     */
    @TableField("value_type")
    private Byte valueType;

    /**
     * 是否参与前台筛选
     */
    @TableField("filterable")
    private Byte filterable;

    /**
     * 单位（如：英寸、GB）
     */
    @TableField("unit")
    private String unit;

    /**
     * 1-必填（该属性在该类目下创建商品时必须填写）
     */
    @TableField("required")
    private Byte required;

    /**
     * 1-作为前台筛选条件（列表页筛选项来源）
     */
    @TableField("searchable")
    private Byte searchable;

    /**
     * 1-是SKU规格（如颜色、内存） 0-仅SPU属性（如上市时间）
     */
    @TableField("is_sku_spec")
    private Byte isSkuSpec;

    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 1-启用 0-禁用
     */
    @TableField("status")
    private Byte status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
