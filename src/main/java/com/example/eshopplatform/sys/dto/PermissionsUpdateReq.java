package com.example.eshopplatform.sys.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * <p>
 * B端权限表更新请求（UpdateReq）
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
@Schema(description = "B端权限表更新请求")
public class PermissionsUpdateReq {
    /** 权限标识（唯一，如 order:create） */
    @Schema(description = "权限标识（唯一，如 order:create）")
    @NotBlank(message = "权限标识（唯一，如 order:create）不能为空")
    @Size(max = 100, message = "权限标识（唯一，如 order:create）长度不能超过{max}")
    private String name;

    /** 权限显示名称 */
    @Schema(description = "权限显示名称")
    @NotBlank (message = "权限显示名称不能为空")
    @Size(max = 100, message = "权限显示名称长度不能超过{max}")
    private String displayName;

    /** 权限描述 */
    @Schema(description = "权限描述")
    @Size(max = 255, message = "权限描述长度不能超过{max}")
    private String description;

    /** 资源（如 order/product/user） */
    @Schema(description = "资源（如 order/product/user）")
    @NotBlank(message = "资源（如 order/product/user）不能为空")
    @Size(max = 50, message = "资源（如 order/product/user）长度不能超过{max}")
    private String resource;

    /** 操作（如 create/read/update/delete） */
    @Schema(description = "操作（如 create/read/update/delete）")
    @NotBlank(message = "操作（如 create/read/update/delete）不能为空")
    @Size(max = 50, message = "操作（如 create/read/update/delete）长度不能超过{max}")
    private String action;

    /** 父级ID（0=根节点，支持菜单/按钮树形层级） */
    @Schema(description = "父级ID（0=根节点，支持菜单/按钮树形层级）")
    private Long parentId;

    /** 分类（如 business/system/admin） */
    @Schema(description = "分类（如 business/system/admin）")
    @Size(max = 50, message = "分类（如 business/system/admin）长度不能超过{max}")
    private String category;

    /** 排序值 */
    @Schema(description = "排序值")
    private Integer sortOrder;

    /** 1-启用 0-禁用 */
    @Schema(description = "1-启用 0-禁用")
    private Boolean status;
}
