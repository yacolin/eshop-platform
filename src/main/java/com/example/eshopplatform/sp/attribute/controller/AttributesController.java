package com.example.eshopplatform.sp.attribute.controller;

import com.example.eshopplatform.common.ApiResponse;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.sp.attribute.dto.AttributesCreateReq;
import com.example.eshopplatform.sp.attribute.dto.AttributesUpdateReq;
import com.example.eshopplatform.sp.attribute.dto.AttributesVO;
import com.example.eshopplatform.sp.attribute.service.AttributesService;
import com.example.eshopplatform.sp.attributeValue.dto.AttributeValuesVO;
import com.example.eshopplatform.sp.attributeValue.service.AttributeValuesService;
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

import java.util.List;


/**
 * <p>
 * 属性字典表基础 CRUD 接口（模板生成）
 * </p>
 *
 * <p>默认路径按“表名去前缀 + 短横线”生成（如 sp_brands → /api/v1/brands）。
 * 新增/更新请求体分别用 AttributesCreateReq / AttributesUpdateReq，
 * 写接口带 {@code @Valid} 触发 DTO 内校验注解。接入真实接口前请调整：如遇子资源/
 * 嵌套接口改更精确的路径、Req/VO 按接口用例裁剪校验、接口按端分组补 springdoc
 * @Tag，并把真实路径补入 application.yml 的 whitelist/admin-paths。</p>
 *
 * @since 2026-09-06
 */
@Tag(name = "AttributesCrud", description = "属性字典表基础 CRUD")
@RestController
@RequestMapping("/api/v1/attributes")
@RequiredArgsConstructor
public class AttributesController {

    private final AttributesService attributesService;
    private final AttributeValuesService attributeValuesService;

    @Operation(operationId = "listSearchableAttributes", summary = "可搜索属性列表（searchable=1 且启用）")
    @GetMapping("/searchable")
    public ApiResponse<List<AttributesVO>> searchable(@RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(attributesService.listSearchable(categoryId));
    }

    @Operation(operationId = "listSkuSpecAttributes", summary = "SKU规格属性列表（is_sku_spec=1 且启用）")
    @GetMapping("/sku-spec")
    public ApiResponse<List<AttributesVO>> skuSpec(@RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(attributesService.listSkuSpec(categoryId));
    }

    @Operation(operationId = "listAttributeValuesByAttributeId", summary = "按属性ID获取属性值列表（启用，search_weight 降序）")
    @GetMapping("/{id}/values")
    public ApiResponse<List<AttributeValuesVO>> values(@PathVariable Long id) {
        return ApiResponse.ok(attributeValuesService.listByAttribute(id));
    }

    @Operation(operationId = "listAttributes", summary = "分页查询属性字典表")
    @GetMapping
    public ApiResponse<PageResult<AttributesVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer searchable,
            @RequestParam(required = false) Integer isSkuSpec) {
        return ApiResponse.ok(attributesService.page(page, size, categoryId, searchable, isSkuSpec));
    }

    @Operation(operationId = "getAttributes", summary = "查询属性字典表详情")
    @GetMapping("/{id}")
    public ApiResponse<AttributesVO> get(@PathVariable Long id) {
        return ApiResponse.ok(attributesService.getById(id));
    }

    @Operation(operationId = "createAttributes", summary = "新增属性字典表")
    @PostMapping
    public ApiResponse<AttributesVO> create(@Valid @RequestBody AttributesCreateReq req) {
        return ApiResponse.ok(attributesService.create(req));
    }

    @Operation(operationId = "updateAttributes", summary = "更新属性字典表")
    @PutMapping("/{id}")
    public ApiResponse<AttributesVO> update(@PathVariable Long id,
                                                     @Valid @RequestBody AttributesUpdateReq req) {
        return ApiResponse.ok(attributesService.update(id, req));
    }

    @Operation(operationId = "deleteAttributes", summary = "删除属性字典表")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        attributesService.delete(id);
        return ApiResponse.ok(null);
    }
}
