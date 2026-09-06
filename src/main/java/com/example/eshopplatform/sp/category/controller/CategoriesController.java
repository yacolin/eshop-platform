package com.example.eshopplatform.sp.category.controller;

import com.example.eshopplatform.common.ApiResponse;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.sp.category.dto.CategoriesReq;
import com.example.eshopplatform.sp.category.dto.CategoriesTreeVO;
import com.example.eshopplatform.sp.category.dto.CategoriesVO;
import com.example.eshopplatform.sp.categoryAttribute.dto.CategoryAttributeVO;
import com.example.eshopplatform.sp.category.service.CategoriesService;
import com.example.eshopplatform.sp.categoryAttribute.service.CategoryAttributesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * <p>
 * 类目表（树状结构）接口（公开）
 * </p>
 *
 * <p>查询覆盖：平铺分页（parent_id/status/name/level 筛选）、全部/根/非根、
 * 子级/层级/树形、详情；子资源：类目推荐属性（类目-品牌关联见 categoryBrand 业务）。
 * 路径已加入 application.yml 的公开 whitelist 与 springdoc public 分组。</p>
 *
 * @since 2026-09-03
 */
@Tag(name = "CategoriesCrud", description = "类目表（树状结构）基础 CRUD")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoriesController {

    private final CategoriesService categoriesService;
    private final CategoryAttributesService categoryAttributesService;

    @Operation(summary = "分页查询类目表（树状结构）")
    @GetMapping
    public ApiResponse<PageResult<CategoriesVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "parent_id", required = false) Long parentId,
            @RequestParam(required = false) Byte status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Byte level) {
        return ApiResponse.ok(categoriesService.page(page, size, parentId, status, name, level));
    }

    @Operation(summary = "全部类目（平铺）")
    @GetMapping("/all")
    public ApiResponse<List<CategoriesVO>> all() {
        return ApiResponse.ok(categoriesService.listAll());
    }

    @Operation(summary = "根类目列表")
    @GetMapping("/root")
    public ApiResponse<List<CategoriesVO>> root() {
        return ApiResponse.ok(categoriesService.listRoot());
    }

    @Operation(summary = "非根类目列表")
    @GetMapping("/nonroot")
    public ApiResponse<List<CategoriesVO>> nonroot() {
        return ApiResponse.ok(categoriesService.listNonRoot());
    }

    @Operation(summary = "子类目列表")
    @GetMapping("/{id}/children")
    public ApiResponse<List<CategoriesVO>> children(@PathVariable Long id) {
        return ApiResponse.ok(categoriesService.listChildren(id));
    }

    @Operation(summary = "指定层级类目列表")
    @GetMapping("/level/{level}")
    public ApiResponse<List<CategoriesVO>> level(@PathVariable Byte level) {
        return ApiResponse.ok(categoriesService.listByLevel(level));
    }

    @Operation(summary = "类目树形结构")
    @GetMapping("/tree")
    public ApiResponse<List<CategoriesTreeVO>> tree(@RequestParam(required = false) Byte status) {
        return ApiResponse.ok(categoriesService.tree(status));
    }

    @Operation(summary = "类目推荐属性列表")
    @GetMapping("/{id}/attributes")
    public ApiResponse<List<CategoryAttributeVO>> attributes(@PathVariable Long id) {
        return ApiResponse.ok(categoryAttributesService.listByCategory(id));
    }

    @Operation(summary = "查询类目表（树状结构）详情")
    @GetMapping("/{id}")
    public ApiResponse<CategoriesVO> get(@PathVariable Long id) {
        return ApiResponse.ok(categoriesService.getById(id));
    }

    @Operation(summary = "新增类目表（树状结构）")
    @PostMapping
    public ApiResponse<CategoriesVO> create(@RequestBody CategoriesReq req) {
        return ApiResponse.ok(categoriesService.create(req));
    }

    @Operation(summary = "更新类目表（树状结构）")
    @PutMapping("/{id}")
    public ApiResponse<CategoriesVO> update(@PathVariable Long id,
                                                     @RequestBody CategoriesReq req) {
        return ApiResponse.ok(categoriesService.update(id, req));
    }

    @Operation(summary = "删除类目表（树状结构）")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoriesService.delete(id);
        return ApiResponse.ok(null);
    }
}
