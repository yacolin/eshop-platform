package com.example.eshopplatform.sp.categoryAttribute.controller;

import com.example.eshopplatform.common.ApiResponse;
import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.sp.categoryAttribute.dto.CategoryAttributeBatchReq;
import com.example.eshopplatform.sp.categoryAttribute.dto.CategoryAttributeCreateReq;
import com.example.eshopplatform.sp.categoryAttribute.dto.CategoryAttributeVO;
import com.example.eshopplatform.sp.categoryAttribute.service.CategoryAttributesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 类目-属性关联接口（公开）
 * </p>
 *
 * <p>维护类目下的推荐属性模板：按类目列表 / 单条与批量新增 / 删除；
 * /categories/{id}/attributes（类目推荐属性）与本模块共用同一服务。</p>
 *
 * @since 2026-09-03
 */
@Tag(name = "CategoryAttributes", description = "类目属性关联")
@RestController
@RequestMapping("/api/v1/category-attributes")
@RequiredArgsConstructor
public class CategoryAttributesController {

    private final CategoryAttributesService categoryAttributesService;

    @Operation(operationId = "listCategoryAttributes", summary = "类目属性关联列表")
    @GetMapping
    public ApiResponse<List<CategoryAttributeVO>> list(
            @RequestParam(required = false) Long categoryId) {
        if (categoryId == null) {
            throw BizException.badRequest("categoryId 不能为空");
        }
        return ApiResponse.ok(categoryAttributesService.listByCategory(categoryId));
    }

    @Operation(operationId = "createCategoryAttribute", summary = "新增类目属性关联")
    @PostMapping
    public ApiResponse<CategoryAttributeVO> create(@RequestBody CategoryAttributeCreateReq req) {
        return ApiResponse.ok(categoryAttributesService.create(req));
    }

    @Operation(operationId = "batchCreateCategoryAttributes", summary = "批量新增类目属性关联")
    @PostMapping("/batch")
    public ApiResponse<Void> batchCreate(@RequestBody CategoryAttributeBatchReq req) {
        categoryAttributesService.batchCreate(req);
        return ApiResponse.ok(null);
    }

    @Operation(operationId = "deleteCategoryAttribute", summary = "删除类目属性关联")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryAttributesService.delete(id);
        return ApiResponse.ok(null);
    }
}
