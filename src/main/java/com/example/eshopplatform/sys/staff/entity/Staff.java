package com.example.eshopplatform.sys.staff.entity;

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
 * B端员工表（后台管理员/运营/财务等）
 * </p>
 *
 * @since 2026-09-06
 */
@Getter
@Setter
@ToString
@TableName("sys_staff")
public class Staff implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 登录用户名（唯一）
     */
    @TableField("username")
    private String username;

    /**
     * bcrypt 密码哈希
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 1-正常 0-禁用（Boolean 对应 DB tinyint(1)）
     */
    @TableField("status")
    private Boolean status;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 最后登录时间
     */
    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 创建人ID（0=系统）
     */
    @TableField("created_by")
    private Long createdBy;

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
