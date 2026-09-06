package com.example.eshopplatform.mkt.promotion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.common.TimeUtil;
import com.example.eshopplatform.mkt.promotion.dto.PromoRuleVO;
import com.example.eshopplatform.mkt.promotion.dto.PromotionsCreateReq;
import com.example.eshopplatform.mkt.promotion.dto.PromotionsDetailVO;
import com.example.eshopplatform.mkt.promotion.dto.PromotionsUpdateReq;
import com.example.eshopplatform.mkt.promotion.dto.PromotionsVO;
import com.example.eshopplatform.mkt.promotion.entity.PromotionProducts;
import com.example.eshopplatform.mkt.promotion.entity.PromotionRules;
import com.example.eshopplatform.mkt.promotion.entity.Promotions;
import com.example.eshopplatform.mkt.promotion.mapper.PromotionProductsMapper;
import com.example.eshopplatform.mkt.promotion.mapper.PromotionRulesMapper;
import com.example.eshopplatform.mkt.promotion.mapper.PromotionsMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>
 * 统一促销活动服务（mkt_promotions + mkt_promotion_rules + mkt_promotion_products），
 * 行为对齐 gf-eshop marketing/promotion：
 * 新增 = 头信息 + 规则(benefitType>0) + 适用商品(product_ids) 一个事务落库；
 * 更新 = 头信息部分字段 + 规则 upsert（benefitType>0 时）；
 * 详细信息 = 头 + 规则 + 适用商品（SPU 基础信息 + SKU 价格区间富化）。
 * </p>
 *
 * @since 2026-09-06
 */
@Service
@RequiredArgsConstructor
public class PromotionsService {

    private final PromotionsMapper promotionsMapper;
    private final PromotionRulesMapper promotionRulesMapper;
    private final PromotionProductsMapper promotionProductsMapper;

    private final ObjectMapper objectMapper;

    /** 分页查询（第 page 页，每页 size 条；status/promoType 可选筛选） */
    public PageResult<PromotionsVO> page(int page, int size, Integer status, Integer promoType) {
        Page<Promotions> p = new Page<>(normalizePage(page), normalizeSize(size));
        LambdaQueryWrapper<Promotions> wrapper = new LambdaQueryWrapper<Promotions>()
                .eq(status != null, Promotions::getStatus, status)
                .eq(promoType != null, Promotions::getPromoType, promoType)
                .orderByDesc(Promotions::getId);
        promotionsMapper.selectPage(p, wrapper);
        return PageResult.of(p.getTotal(), p.getRecords().stream().map(this::toVO).toList());
    }

    /** 按主键查询 */
    public PromotionsVO getById(Long id) {
        return toVO(require(id));
    }

    /**
     * 促销详细信息（含规则和适用商品，SPU 富化）——GET /promotions/{id}/detail。
     */
    public PromotionsDetailVO getDetail(Long id) {
        Promotions promo = require(id);
        PromotionsDetailVO vo = new PromotionsDetailVO();
        vo.setPromotion(toVO(promo));
        vo.setRule(promo.getRuleId() != null && promo.getRuleId() > 0
                ? toRuleVO(promotionRulesMapper.selectById(promo.getRuleId())) : null);
        vo.setProducts(promotionProductsMapper.selectFullByPromotionId(id));
        return vo;
    }

    /** 新增：头信息 + 规则(benefitType>0) + 适用商品，事务内完成 */
    @Transactional(rollbackFor = Exception.class)
    public PromotionsVO create(PromotionsCreateReq req) {
        Promotions entity = new Promotions();
        entity.setPromotionNo(generatePromotionNo());
        entity.setMerchantId(req.getMerchantId() == null ? 0L : req.getMerchantId());
        entity.setPromoName(req.getPromoName());
        entity.setPromoType(req.getPromoType());
        entity.setPromoCode(req.getPromoCode() == null ? "" : req.getPromoCode());
        entity.setStartTime(req.getStartTime());
        entity.setEndTime(req.getEndTime());
        entity.setTotalQuantity(req.getTotalQuantity() == null ? 0 : req.getTotalQuantity());
        entity.setPerUserLimit(req.getPerUserLimit() == null ? 1 : req.getPerUserLimit());
        entity.setPriority(req.getPriority() == null ? 0 : req.getPriority());
        entity.setStatus((byte) 1); // 草稿
        entity.setRuleId(0L);
        entity.setCreatedBy(0L);
        entity.setUpdatedBy(0L);
        promotionsMapper.insert(entity);

        if (req.getBenefitType() != null && req.getBenefitType() > 0) {
            PromotionRules rule = buildRule(entity.getId(), entity.getMerchantId(), req);
            promotionRulesMapper.insert(rule);
            entity.setRuleId(rule.getId());
            promotionsMapper.updateById(entity);
        }
        insertProducts(entity.getId(), entity.getMerchantId(), req.getProductIds());
        return toVO(entity);
    }

    /**
     * 更新：头信息部分字段（promoType/商家/商品列表不可改）+ 规则 upsert（benefitType>0 时）。
     */
    @Transactional(rollbackFor = Exception.class)
    public PromotionsVO update(Long id, PromotionsUpdateReq req) {
        Promotions entity = require(id);
        if (req.getPromoName() != null) {
            entity.setPromoName(req.getPromoName());
        }
        if (req.getStartTime() != null) {
            entity.setStartTime(req.getStartTime());
        }
        if (req.getEndTime() != null) {
            entity.setEndTime(req.getEndTime());
        }
        if (req.getTotalQuantity() != null) {
            entity.setTotalQuantity(req.getTotalQuantity());
        }
        if (req.getPerUserLimit() != null) {
            entity.setPerUserLimit(req.getPerUserLimit());
        }
        if (req.getStatus() != null) {
            entity.setStatus(req.getStatus());
        }
        if (req.getPriority() != null) {
            entity.setPriority(req.getPriority());
        }
        if (req.getBenefitType() != null && req.getBenefitType() > 0) {
            upsertRule(entity, req);
        }
        entity.setUpdatedBy(0L);
        promotionsMapper.updateById(entity);
        return toVO(entity);
    }

    /** 按主键删除（逻辑删除，头信息标 deleted_at；规则/商品行保留） */
    public void delete(Long id) {
        require(id);
        promotionsMapper.deleteById(id);
    }

    // ==================== 私有工具 ====================

    /** 规则 upsert：已存在则改字段，否则新建并回写 header.rule_id */
    private void upsertRule(Promotions promo, PromotionsUpdateReq req) {
        PromotionRules rule = promotionRulesMapper.selectOne(new LambdaQueryWrapper<PromotionRules>()
                .eq(PromotionRules::getPromotionId, promo.getId()));
        boolean created = false;
        if (rule == null) {
            rule = new PromotionRules();
            rule.setPromotionId(promo.getId());
            rule.setMerchantId(promo.getMerchantId());
            created = true;
        }
        if (req.getRuleName() != null) {
            rule.setRuleName(req.getRuleName());
        }
        if (req.getConditionType() != null) {
            rule.setConditionType(req.getConditionType());
        }
        if (req.getConditionValue() != null) {
            rule.setConditionValue(req.getConditionValue());
        }
        if (req.getBenefitValue() != null) {
            rule.setBenefitConfig(benefitConfigJson(req.getBenefitType(), req.getBenefitValue()));
        } else if (req.getBenefitType() != null) {
            rule.setBenefitConfig(benefitConfigJson(req.getBenefitType(), 0L));
        }
        if (req.getIsStackable() != null) {
            rule.setIsStackable(req.getIsStackable());
        }
        if (req.getStackPriority() != null) {
            rule.setStackGroup(req.getStackPriority());
        }
        if (created) {
            rule.setConditionValue(rule.getConditionValue() == null ? 0L : rule.getConditionValue());
            promotionRulesMapper.insert(rule);
            promo.setRuleId(rule.getId());
        } else {
            rule.setUpdatedBy(0L);
            promotionRulesMapper.updateById(rule);
        }
    }

    /** 新建规则实体（默认值兜底：condition_type=1 无门槛、benefit_config 必填） */
    private PromotionRules buildRule(Long promotionId, Long merchantId, PromotionsCreateReq req) {
        PromotionRules rule = new PromotionRules();
        rule.setPromotionId(promotionId);
        rule.setMerchantId(merchantId == null ? 0L : merchantId);
        rule.setRuleName(req.getRuleName());
        rule.setConditionType(req.getConditionType() == null ? (byte) 1 : req.getConditionType());
        rule.setConditionValue(req.getConditionValue() == null ? 0L : req.getConditionValue());
        rule.setBenefitConfig(benefitConfigJson(req.getBenefitType(),
                req.getBenefitValue() == null ? 0L : req.getBenefitValue()));
        rule.setIsStackable(req.getIsStackable() == null ? (byte) 0 : req.getIsStackable());
        rule.setStackGroup(req.getStackPriority() == null ? 0 : req.getStackPriority());
        rule.setCreatedBy(0L);
        rule.setUpdatedBy(0L);
        return rule;
    }

    /** 写入适用商品（product_type=3 指定SPU；入参 SPU 需存在且未删） */
    private void insertProducts(Long promotionId, Long merchantId, List<Long> productIds) {
        if (productIds == null) {
            return;
        }
        List<Long> ids = productIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return;
        }
        if (promotionProductsMapper.countExistingProducts(ids) != ids.size()) {
            throw BizException.badRequest("包含不存在或已删除的SPU");
        }
        for (Long targetId : ids) {
            PromotionProducts pp = new PromotionProducts();
            pp.setPromotionId(promotionId);
            pp.setMerchantId(merchantId == null ? 0L : merchantId);
            pp.setProductType((byte) 3);
            pp.setTargetId(targetId);
            promotionProductsMapper.insert(pp);
        }
    }

    /** benefit_config JSON：{"type":t,"value":v} */
    private String benefitConfigJson(Integer benefitType, Long benefitValue) {
        return String.format("{\"type\":%d,\"value\":%d}", benefitType, benefitValue);
    }

    /** 促销业务编号：P + 时间戳 + 随机段（<=32） */
    private String generatePromotionNo() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return "P" + ts + ThreadLocalRandom.current().nextInt(100, 999);
    }

    private PromoRuleVO toRuleVO(PromotionRules rule) {
        if (rule == null) {
            return null;
        }
        PromoRuleVO vo = new PromoRuleVO();
        vo.setId(rule.getId());
        vo.setPromotionId(rule.getPromotionId());
        vo.setRuleName(rule.getRuleName());
        vo.setConditionType(rule.getConditionType());
        vo.setConditionValue(rule.getConditionValue());
        vo.setIsStackable(rule.getIsStackable());
        vo.setStackGroup(rule.getStackGroup());
        parseBenefit(rule.getBenefitConfig(), vo);
        return vo;
    }

    /** 解析简单型 benefit_config（{"type","value"}）到 VO；阶梯/异常时保持 null */
    private void parseBenefit(String benefitConfig, PromoRuleVO vo) {
        if (benefitConfig == null || benefitConfig.isBlank()) {
            return;
        }
        try {
            JsonNode node = objectMapper.readTree(benefitConfig);
            if (node.has("type") && node.get("type").canConvertToInt()) {
                vo.setBenefitType(node.get("type").asInt());
            }
            if (node.has("value") && node.get("value").canConvertToLong()) {
                vo.setBenefitValue(node.get("value").asLong());
            }
        } catch (Exception ignored) {
            // 阶梯/异常配置不在 VO 便捷字段体现
        }
    }

    /** 按主键查询，不存在抛 404 */
    private Promotions require(Long id) {
        Promotions entity = promotionsMapper.selectById(id);
        if (entity == null) {
            throw BizException.notFound("统一促销活动表不存在");
        }
        return entity;
    }

    /** entity -> VO（时间列 Long epoch 毫秒） */
    private PromotionsVO toVO(Promotions entity) {
        PromotionsVO vo = new PromotionsVO();
        vo.setId(entity.getId());
        vo.setPromotionNo(entity.getPromotionNo());
        vo.setMerchantId(entity.getMerchantId());
        vo.setPromoName(entity.getPromoName());
        vo.setPromoType(entity.getPromoType());
        vo.setPromoCode(entity.getPromoCode());
        vo.setPromoCodeUq(entity.getPromoCodeUq());
        vo.setStartTime(TimeUtil.toEpochMillis(entity.getStartTime()));
        vo.setEndTime(TimeUtil.toEpochMillis(entity.getEndTime()));
        vo.setTotalQuantity(entity.getTotalQuantity());
        vo.setPerUserLimit(entity.getPerUserLimit());
        vo.setUsedQuantity(entity.getUsedQuantity());
        vo.setRuleId(entity.getRuleId());
        vo.setStatus(entity.getStatus());
        vo.setPriority(entity.getPriority());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setUpdatedBy(entity.getUpdatedBy());
        vo.setCreatedAt(TimeUtil.toEpochMillis(entity.getCreatedAt()));
        vo.setUpdatedAt(TimeUtil.toEpochMillis(entity.getUpdatedAt()));
        return vo;
    }

    /** 页号归一化：至少从第 1 页开始 */
    static int normalizePage(int page) {
        return Math.max(page, 1);
    }

    /** 每页条数归一化：1~1000 */
    static int normalizeSize(int size) {
        return Math.min(Math.max(size, 1), 1000);
    }
}
