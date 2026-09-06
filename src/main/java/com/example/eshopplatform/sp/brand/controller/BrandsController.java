package com.example.eshopplatform.sp.controller;

import com.example.eshopplatform.common.ApiResponse;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.sp.brand.dto.BrandsReq;
import com.example.eshopplatform.sp.brand.dto.BrandsVO;
import com.example.eshopplatform.sp.brand.service.BrandsService;
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


/**
 * <p>
 * 品牌表基础 CRUD 接口（模板生成）
 * </p>
 *
 * <p>默认路径按“表名去前缀 + 短横线”生成（如 sp_brands → /api/v1/brands）。
 * 接入真实接口前请调整：如遇子资源/嵌套接口改更精确的路径、Req/VO 按接口用例
 * 裁剪校验、接口按端分组补 springdoc @Tag，并把真实路径补入 application.yml
 * 的 whitelist/admin-paths。</p>
 *
 * @since 2026-09-03
 */
@Tag(name = "BrandsCrud", description = "品牌表基础 CRUD")
@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandsController {

    private final BrandsService brandsService;

    @Operation(operationId = "listBrands", summary = "分页查询品牌表")
    @GetMapping
    public ApiResponse<PageResult<BrandsVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String first_letter,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(brandsService.page(page, size, name, first_letter, status));
    }

    @Operation(operationId = "getBrands", summary = "查询品牌表详情")
    @GetMapping("/{id}")
    public ApiResponse<BrandsVO> get(@PathVariable Long id) {
        return ApiResponse.ok(brandsService.getById(id));
    }

    @Operation(operationId = "createBrands", summary = "新增品牌表")
    @PostMapping
    public ApiResponse<BrandsVO> create(@RequestBody BrandsReq req) {
        return ApiResponse.ok(brandsService.create(req));
    }

    @Operation(operationId = "updateBrands", summary = "更新品牌表")
    @PutMapping("/{id}")
    public ApiResponse<BrandsVO> update(@PathVariable Long id,
                                                     @RequestBody BrandsReq req) {
        return ApiResponse.ok(brandsService.update(id, req));
    }

    @Operation(operationId = "deleteBrands", summary = "删除品牌表")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        brandsService.delete(id);
        return ApiResponse.ok(null);
    }
}
