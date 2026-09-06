package com.example.eshopplatform.sys.role.entity;

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
 * B端角色定义表
 * </p>
 *
 * @since 2026-09-06
 */
@Getter
@Setter
@ToString
@TableName("sys_roles")
public class Roles implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色名称（唯一，如 admin/editor/vip）
     */
    @TableField("name")
    private String name;

    /**
     * 角色显示名称
     */
    @TableField("display_name")
    private String displayName;

    /**
     * 角色描述
     */
    @TableField("description")
    private String description;

    /**
     * builtin-系统内置 custom-自定义
     */
    @TableField("role_type")
    private String roleType;

    /**
     * 排序值
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 1-启用 0-禁用（MySQL tinyint(1) 即布尔列，Java 侧 Boolean 读写 1/0）
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
