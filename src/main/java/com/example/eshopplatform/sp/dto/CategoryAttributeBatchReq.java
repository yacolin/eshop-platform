package com.example.eshopplatform.sp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 批量新增类目属性关联请求
 * </p>
 *
 * @since 2026-09-03
 */
@Data
@Schema(description = "批量新增类目属性关联请求")
public class CategoryAttributeBatchReq {

    /** 类目ID */
    @Schema(description = "类目ID")
    private Long categoryId;

    /** 属性列表 */
    @Schema(description = "属性列表")
    private List<Item> items;

    /** 批量关联项 */
    @Data
    @Schema(description = "批量关联项")
    public static class Item {

        /** 属性ID */
        @Schema(description = "属性ID")
        private Long attributeId;

        /** 该类目下是否必填（0/1，仅提示） */
        @Schema(description = "该类目下是否必填（0/1，仅提示）")
        private Byte required;

        /** 是否作为前台默认筛选项（0/1） */
        @Schema(description = "是否作为前台默认筛选项（0/1）")
        private Byte isDefaultFilter;

        /** 排序 */
        @Schema(description = "排序")
        private Integer sortOrder;
    }
}
