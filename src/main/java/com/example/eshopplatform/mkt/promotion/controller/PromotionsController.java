package com.example.eshopplatform.mkt.promotion.controller;

import com.example.eshopplatform.common.ApiResponse;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.mkt.promotion.dto.PromotionsCreateReq;
import com.example.eshopplatform.mkt.promotion.dto.PromotionsDetailVO;
import com.example.eshopplatform.mkt.promotion.dto.PromotionsUpdateReq;
import com.example.eshopplatform.mkt.promotion.dto.PromotionsVO;
import com.example.eshopplatform.mkt.promotion.service.PromotionsService;
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
 * 统一促销活动表基础 CRUD 接口（模板生成）
 * </p>
 *
 * <p>默认路径按“表名去前缀 + 短横线”生成（如 sp_brands → /api/v1/brands）。
 * 新增/更新请求体分别用 PromotionsCreateReq / PromotionsUpdateReq，
 * 写接口带 {@code @Valid} 触发 DTO 内校验注解。接入真实接口前请调整：如遇子资源/
 * 嵌套接口改更精确的路径、Req/VO 按接口用例裁剪校验、接口按端分组补 springdoc
 * @Tag，并把真实路径补入 application.yml 的 whitelist/admin-paths。</p>
 *
 * @since 2026-09-06
 */
@Tag(name = "PromotionsCrud", description = "统一促销活动表基础 CRUD")
@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionsController {

    private final PromotionsService promotionsService;

    @Operation(operationId = "listPromotions", summary = "分页查询统一促销活动表")
    @GetMapping
    public ApiResponse<PageResult<PromotionsVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer promoType) {
        return ApiResponse.ok(promotionsService.page(page, size, status, promoType));
    }

    @Operation(operationId = "getPromotions", summary = "查询统一促销活动表详情")
    @GetMapping("/{id}")
    public ApiResponse<PromotionsVO> get(@PathVariable Long id) {
        return ApiResponse.ok(promotionsService.getById(id));
    }

    @Operation(operationId = "getPromotionDetail", summary = "促销详细信息（含规则和商品）")
    @GetMapping("/{id}/detail")
    public ApiResponse<PromotionsDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(promotionsService.getDetail(id));
    }

    @Operation(operationId = "createPromotions", summary = "新增统一促销活动表")
    @PostMapping
    public ApiResponse<PromotionsVO> create(@Valid @RequestBody PromotionsCreateReq req) {
        return ApiResponse.ok(promotionsService.create(req));
    }

    @Operation(operationId = "updatePromotions", summary = "更新统一促销活动表")
    @PutMapping("/{id}")
    public ApiResponse<PromotionsVO> update(@PathVariable Long id,
                                                     @Valid @RequestBody PromotionsUpdateReq req) {
        return ApiResponse.ok(promotionsService.update(id, req));
    }

    @Operation(operationId = "deletePromotions", summary = "删除统一促销活动表")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        promotionsService.delete(id);
        return ApiResponse.ok(null);
    }
}
