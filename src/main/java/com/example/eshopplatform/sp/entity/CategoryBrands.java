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
 * 类目-品牌关联表（多对多）
 * </p>
 *
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@TableName("sp_category_brands")
public class CategoryBrands implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联 categories.id
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 关联 brands.id
     */
    @TableField("brand_id")
    private Long brandId;

    /**
     * 排序权重（越小越靠前，控制该类目下品牌的展示顺序）
     */
    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
