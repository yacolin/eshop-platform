package com.example.eshopplatform.sp.attributeValue.controller;

import com.example.eshopplatform.common.ApiResponse;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.sp.attributeValue.dto.AttributeValuesCreateReq;
import com.example.eshopplatform.sp.attributeValue.dto.AttributeValuesUpdateReq;
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


/**
 * <p>
 * 属性值字典表基础 CRUD 接口（模板生成）
 * </p>
 *
 * <p>默认路径按“表名去前缀 + 短横线”生成（如 sp_brands → /api/v1/brands）。
 * 新增/更新请求体分别用 AttributeValuesCreateReq / AttributeValuesUpdateReq，
 * 写接口带 {@code @Valid} 触发 DTO 内校验注解。接入真实接口前请调整：如遇子资源/
 * 嵌套接口改更精确的路径、Req/VO 按接口用例裁剪校验、接口按端分组补 springdoc
 * @Tag，并把真实路径补入 application.yml 的 whitelist/admin-paths。</p>
 *
 * @since 2026-09-06
 */
@Tag(name = "AttributeValuesCrud", description = "属性值字典表基础 CRUD")
@RestController
@RequestMapping("/api/v1/attribute-values")
@RequiredArgsConstructor
public class AttributeValuesController {

    private final AttributeValuesService attributeValuesService;

    @Operation(operationId = "listAttributeValues", summary = "分页查询属性值字典表")
    @GetMapping
    public ApiResponse<PageResult<AttributeValuesVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long attributeId) {
        return ApiResponse.ok(attributeValuesService.page(page, size, attributeId));
    }

    @Operation(operationId = "getAttributeValues", summary = "查询属性值字典表详情")
    @GetMapping("/{id}")
    public ApiResponse<AttributeValuesVO> get(@PathVariable Long id) {
        return ApiResponse.ok(attributeValuesService.getById(id));
    }

    @Operation(operationId = "createAttributeValues", summary = "新增属性值字典表")
    @PostMapping
    public ApiResponse<AttributeValuesVO> create(@Valid @RequestBody AttributeValuesCreateReq req) {
        return ApiResponse.ok(attributeValuesService.create(req));
    }

    @Operation(operationId = "updateAttributeValues", summary = "更新属性值字典表")
    @PutMapping("/{id}")
    public ApiResponse<AttributeValuesVO> update(@PathVariable Long id,
                                                     @Valid @RequestBody AttributeValuesUpdateReq req) {
        return ApiResponse.ok(attributeValuesService.update(id, req));
    }

    @Operation(operationId = "deleteAttributeValues", summary = "删除属性值字典表")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        attributeValuesService.delete(id);
        return ApiResponse.ok(null);
    }
}
