package com.example.eshopplatform.mkt.promotion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.eshopplatform.mkt.promotion.dto.PromoProductVO;
import com.example.eshopplatform.mkt.promotion.entity.PromotionProducts;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 促销适用商品表 Mapper 接口
 * </p>
 *
 * <p>含商品详情富化联查（sp_products + sp_skus 价格区间）与 SPU 存在性校验的注解 SQL。</p>
 *
 * @since 2026-09-06
 */
@Mapper
public interface PromotionProductsMapper extends BaseMapper<PromotionProducts> {

    /**
     * 促销适用商品 + SPU 基础信息 + SKU 价格区间（按 product_type,id 升序，对齐 gf 详情富化）。
     */
    @Select("SELECT pp.id AS id, pp.product_type AS product_type, pp.target_id AS product_id, "
            + "p.name AS spu_name, p.subtitle AS subtitle, p.main_image AS main_image, "
            + "p.unit AS unit, p.sales_count AS sales_count, p.status AS spu_status, "
            + "pr.min_price AS min_price, pr.max_price AS max_price "
            + "FROM mkt_promotion_products pp "
            + "LEFT JOIN sp_products p ON p.id = pp.target_id AND pp.product_type = 3 AND p.deleted_at IS NULL "
            + "LEFT JOIN (SELECT product_id, MIN(price) AS min_price, MAX(price) AS max_price "
            + "FROM sp_skus WHERE status = 1 AND deleted_at IS NULL GROUP BY product_id) pr "
            + "ON pr.product_id = pp.target_id "
            + "WHERE pp.promotion_id = #{promotionId} AND pp.deleted_at IS NULL "
            + "ORDER BY pp.product_type ASC, pp.id ASC")
    List<PromoProductVO> selectFullByPromotionId(@Param("promotionId") Long promotionId);

    /**
     * 校验 SPU ID 均存在且未删除（返回匹配条数，与入参数量比对）。
     */
    @Select("<script>"
            + "SELECT COUNT(*) FROM sp_products WHERE deleted_at IS NULL AND id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    long countExistingProducts(@Param("ids") Collection<Long> ids);
}
