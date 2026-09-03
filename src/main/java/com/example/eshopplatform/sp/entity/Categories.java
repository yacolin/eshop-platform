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
 * 类目表（树状结构）
 * </p>
 *
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@TableName("sp_categories")
public class Categories implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 类目名称（如：手机）
     */
    @TableField("name")
    private String name;

    /**
     * 父级ID（0表示根节点）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 层级（1-3级）
     */
    @TableField("level")
    private Byte level;

    /**
     * 路径（如：1/23/45/）
     */
    @TableField("path")
    private String path;

    /**
     * 类目图标
     */
    @TableField("icon_url")
    private String iconUrl;

    /**
     * 排序
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

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
