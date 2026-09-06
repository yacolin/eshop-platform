package com.example.eshopplatform.sp.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 类目表（树状结构）创建/更新请求（Req）
 * </p>
 *
 * <p>仅含业务字段；created_at/updated_at/deleted_at 等自动列由服务端/数据库
 * 维护，不接收客户端输入（level/path 随父级类目由调用方按树规则填写）。</p>
 *
 * @since 2026-09-03
 */
@Data
@Schema(description = "类目表（树状结构）创建/更新请求")
public class CategoriesReq {
    /** 类目名称（如：手机） */
    @Schema(description = "类目名称（如：手机）")
    private String name;

    /** 父级ID（0表示根节点） */
    @Schema(description = "父级ID（0表示根节点）")
    private Long parentId;

    /** 层级（1-3级） */
    @Schema(description = "层级（1-3级）")
    private Byte level;

    /** 路径（如：1/23/45/） */
    @Schema(description = "路径（如：1/23/45/）")
    private String path;

    /** 类目图标 */
    @Schema(description = "类目图标")
    private String iconUrl;

    /** 排序 */
    @Schema(description = "排序")
    private Integer sortOrder;

    /** 1-启用 0-禁用 */
    @Schema(description = "1-启用 0-禁用")
    private Byte status;
}
