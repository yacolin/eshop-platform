package com.example.eshopplatform.sp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 类目-属性关联（推荐模板行）
 * </p>
 *
 * @since 2026-09-03
 */
@Data
@Schema(description = "类目推荐属性项")
public class CategoryAttributeVO {
    /** 关联记录ID */
    private Long id;

    /** 类目ID */
    private Long categoryId;

    /** 属性ID */
    private Long attributeId;

    /** 该类目下是否必填（0/1，仅提示） */
    private Byte required;

    /** 是否作为前台默认筛选项（0/1） */
    private Byte isDefaultFilter;

    /** 排序 */
    private Integer sortOrder;

    private LocalDateTime createdAt;
}
