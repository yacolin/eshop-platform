package com.example.eshopplatform.sys.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * <p>
 * B端角色定义表创建请求（CreateReq）
 * </p>
 *
 * <p>由代码生成器生成的基础 DTO：仅含业务字段，不含主键 id 与
 * created_at/updated_at/deleted_at 等服务端自动维护列。DB 必填列
 * （NOT NULL 且无默认值）已自动加最基础的空校验（String 用
 * {@code @NotBlank}、其余用 {@code @NotNull}）；String 列另按 DB 长度
 * 上限自动补 {@code @Size}（防超长入库存 500）；其余字段按业务裁剪补充。</p>
 *
 * @since 2026-09-06
 */
@Data
@Schema(description = "B端角色定义表创建请求")
public class RolesCreateReq {
    /** 角色名称（唯一，如 admin/editor/vip） */
    @Schema(description = "角色名称（唯一，如 admin/editor/vip）")
    @NotBlank(message = "角色名称（唯一，如 admin/editor/vip）不能为空")
    @Size(max = 50, message = "角色名称（唯一，如 admin/editor/vip）长度不能超过{max}")
    private String name;

    /** 角色显示名称 */
    @Schema(description = "角色显示名称")
    @NotBlank (message = "角色显示名称不能为空")
    @Size(max = 100, message = "角色显示名称长度不能超过{max}")
    private String displayName;

    /** 角色描述 */
    @Schema(description = "角色描述")
    @Size(max = 255, message = "角色描述长度不能超过{max}")
    private String description;

    /** builtin-系统内置 custom-自定义 */
    @Schema(description = "builtin-系统内置 custom-自定义")
    @Size(max = 20, message = "builtin-系统内置 custom-自定义长度不能超过{max}")
    private String roleType;

    /** 排序值 */
    @Schema(description = "排序值")
    private Integer sortOrder;

    /** true-启用 false-禁用（对应 DB tinyint(1) 的 1/0；不传默认启用） */
    @Schema(description = "true-启用 false-禁用（对应 DB tinyint(1) 的 1/0；不传默认启用）")
    private Boolean status;
}
