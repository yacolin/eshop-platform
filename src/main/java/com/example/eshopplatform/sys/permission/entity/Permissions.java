package com.example.eshopplatform.sys.permission.entity;

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
 * B端权限表
 * </p>
 *
 * @since 2026-09-06
 */
@Getter
@Setter
@ToString
@TableName("sys_permissions")
public class Permissions implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 权限标识（唯一，如 order:create）
     */
    @TableField("name")
    private String name;

    /**
     * 权限显示名称
     */
    @TableField("display_name")
    private String displayName;

    /**
     * 权限描述
     */
    @TableField("description")
    private String description;

    /**
     * 资源（如 order/product/user）
     */
    @TableField("resource")
    private String resource;

    /**
     * 操作（如 create/read/update/delete）
     */
    @TableField("action")
    private String action;

    /**
     * 父级ID（0=根节点，支持菜单/按钮树形层级）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 分类（如 business/system/admin）
     */
    @TableField("category")
    private String category;

    /**
     * 排序值
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 1-启用 0-禁用
     */
    @TableField("status")
    private Boolean status;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 删除时间
     */
    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
