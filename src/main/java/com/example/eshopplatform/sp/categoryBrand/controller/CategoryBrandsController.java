package com.example.eshopplatform.sp.categoryBrand.controller;

import com.example.eshopplatform.common.ApiResponse;
import com.example.eshopplatform.sp.categoryBrand.dto.CategoryBrandUpdateReq;
import com.example.eshopplatform.sp.categoryBrand.dto.CategoryBrandVO;
import com.example.eshopplatform.sp.categoryBrand.service.CategoryBrandsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 类目-品牌关联接口（独立业务包 categoryBrand）。
 * </p>
 *
 * <p>为保持公开 API 不变，映射沿用类目子资源路径：
 * GET / PUT /api/v1/categories/{categoryId}/brands
 * （该路径已入 application.yml 公开 whitelist / public 文档分组）。</p>
 *
 * @since 2026-09-03
 */
@Tag(name = "CategoryBrandsCrud", description = "类目-品牌关联")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryBrandsController {

    private final CategoryBrandsService categoryBrandsService;

    @Operation(summary = "类目下品牌列表（关联 + 品牌详情）")
    @GetMapping("/{categoryId}/brands")
    public ApiResponse<List<CategoryBrandVO>> listByCategory(@PathVariable Long categoryId) {
        return ApiResponse.ok(categoryBrandsService.listByCategory(categoryId));
    }

    @Operation(summary = "类目关联品牌（全量替换）")
    @PutMapping("/{categoryId}/brands")
    public ApiResponse<Void> replaceByCategory(@PathVariable Long categoryId,
                                               @RequestBody CategoryBrandUpdateReq req) {
        categoryBrandsService.replaceByCategory(categoryId, req);
        return ApiResponse.ok(null);
    }
}
