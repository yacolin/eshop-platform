package com.example.eshopplatform.sp.sku.controller;

import com.example.eshopplatform.common.ApiResponse;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.sp.sku.dto.SkusCreateReq;
import com.example.eshopplatform.sp.sku.dto.SkusUpdateReq;
import com.example.eshopplatform.sp.sku.dto.SkusVO;
import com.example.eshopplatform.sp.sku.service.SkusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * SKU规格表（具体可售单元）基础 CRUD 接口（模板生成）
 * </p>
 *
 * <p>默认路径按“表名去前缀 + 短横线”生成（如 sp_brands → /api/v1/brands）。
 * 新增/更新请求体分别用 SkusCreateReq / SkusUpdateReq，
 * 写接口带 {@code @Valid} 触发 DTO 内校验注解。接入真实接口前请调整：如遇子资源/
 * 嵌套接口改更精确的路径、Req/VO 按接口用例裁剪校验、接口按端分组补 springdoc
 * @Tag，并把真实路径补入 application.yml 的 whitelist/admin-paths。</p>
 *
 * @since 2026-09-06
 */
@Tag(name = "SkusCrud", description = "SKU规格表（具体可售单元）基础 CRUD")
@RestController
@RequestMapping("/api/v1/skus")
@RequiredArgsConstructor
public class SkusController {

    private final SkusService skusService;

    @Operation(operationId = "listSkus", summary = "分页查询SKU规格表（具体可售单元）")
    @GetMapping
    public ApiResponse<PageResult<SkusVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long productId) {
        return ApiResponse.ok(skusService.page(page, size, productId));
    }

    @Operation(operationId = "getSkus", summary = "查询SKU规格表（具体可售单元）详情")
    @GetMapping("/{id}")
    public ApiResponse<SkusVO> get(@PathVariable Long id) {
        return ApiResponse.ok(skusService.getById(id));
    }

    @Operation(operationId = "getSkusByCode", summary = "查询SKU规格表（具体可售单元）详情")
    @GetMapping("/code/{skuCode}")
    public ApiResponse<SkusVO> getByCode(@PathVariable String skuCode) {
        return ApiResponse.ok(skusService.getBySkuCode(skuCode));
    }

    @Operation(operationId = "createSkus", summary = "新增SKU规格表（具体可售单元）")
    @PostMapping
    public ApiResponse<SkusVO> create(@Valid @RequestBody SkusCreateReq req) {
        return ApiResponse.ok(skusService.create(req));
    }

    @Operation(operationId = "updateSkus", summary = "更新SKU规格表（具体可售单元）")
    @PutMapping("/{id}")
    public ApiResponse<SkusVO> update(@PathVariable Long id,
                                                     @Valid @RequestBody SkusUpdateReq req) {
        return ApiResponse.ok(skusService.update(id, req));
    }

    @Operation(operationId = "deleteSkus", summary = "删除SKU规格表（具体可售单元）")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        skusService.delete(id);
        return ApiResponse.ok(null);
    }
}
