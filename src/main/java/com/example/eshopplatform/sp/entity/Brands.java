package com.example.eshopplatform.sp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 品牌表
 * </p>
 *
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@TableName("sp_brands")
public class Brands implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 品牌名称（如：苹果）
     */
    @TableField("name")
    private String name;

    /**
     * 英文名
     */
    @TableField("english_name")
    private String englishName;

    /**
     * 品牌Logo（CDN）
     */
    @TableField("logo_url")
    private String logoUrl;

    /**
     * 首字母（A-Z，用于前台索引筛选）
     */
    @TableField("first_letter")
    private String firstLetter;

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

    /**
     * 品牌故事
     */
    @TableField("description")
    private String description;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
