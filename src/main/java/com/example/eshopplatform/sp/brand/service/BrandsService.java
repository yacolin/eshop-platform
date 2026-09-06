package com.example.eshopplatform.sp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eshopplatform.sp.brand.mapper.BrandsMapper;
import com.example.eshopplatform.sp.brand.entity.Brands;
import com.example.eshopplatform.sp.brand.dto.BrandsReq;
import com.example.eshopplatform.sp.brand.dto.BrandsVO;
import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.common.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 品牌表服务
 * </p>
 *
 * <p>工程约定：Service 为具体类（不生成接口与 *ServiceImpl），直接注入 Mapper；
 * 入参/出参走 DTO（Req/VO），entity↔dto 的 toVO/apply 由生成器自动补齐。
 * 以下为基础 CRUD，接入真实业务时按需加查询条件、校验与权限逻辑。</p>
 *
 * @since 2026-09-03
 */
@Service
@RequiredArgsConstructor
public class BrandsService {

    /** 数据访问层 */
    private final BrandsMapper brandsMapper;

    /** 分页查询（第 page 页，每页 size 条） */
    public PageResult<BrandsVO> page(int page, int size, String name, String first_letter, String status) {
        Page<Brands> p = new Page<>(Math.max(page, 1), Math.min(Math.max(size, 1), 1000));
        LambdaQueryWrapper<Brands> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Brands::getName, name);
        }
        if (first_letter != null && !first_letter.isEmpty()) {
            wrapper.like(Brands::getFirstLetter, first_letter);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Brands::getStatus, status);
        }
        wrapper.orderByDesc(Brands::getId);
        brandsMapper.selectPage(p, wrapper);
        return PageResult.of(p.getTotal(), p.getRecords().stream().map(this::toVO).toList());
    }

    /** 按主键查询 */
    public BrandsVO getById(Long id) {
        return toVO(require(id));
    }

    /** 新增 */
    public BrandsVO create(BrandsReq req) {
        Brands entity = new Brands();
        apply(entity, req);
        brandsMapper.insert(entity);
        return toVO(entity);
    }

    /** 按主键更新 */
    public BrandsVO update(Long id, BrandsReq req) {
        Brands entity = require(id);
        apply(entity, req);
        brandsMapper.updateById(entity);
        return toVO(entity);
    }

    /** 按主键删除 */
    public void delete(Long id) {
        require(id);
        brandsMapper.deleteById(id);
    }

    /** 按主键查询，不存在抛 404 */
    private Brands require(Long id) {
        Brands entity = brandsMapper.selectById(id);
        if (entity == null) {
            throw BizException.notFound("品牌不存在");
        }
        return entity;
    }

    /** entity -> VO（deleted_at 为服务端软删字段，不对业务端暴露） */
    private BrandsVO toVO(Brands entity) {
        BrandsVO vo = new BrandsVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setEnglishName(entity.getEnglishName());
        vo.setLogoUrl(entity.getLogoUrl());
        vo.setFirstLetter(entity.getFirstLetter());
        vo.setSortOrder(entity.getSortOrder());
        vo.setStatus(entity.getStatus());
        vo.setDescription(entity.getDescription());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    /**
     * Req -> entity（仅落业务字段，与 gf-eshop 写接口对齐）；
     * created_at/updated_at/deleted_at 由数据库自动维护，不接受客户端输入。
     */
    private void apply(Brands entity, BrandsReq req) {
        entity.setName(req.getName());
        entity.setEnglishName(req.getEnglishName());
        entity.setLogoUrl(req.getLogoUrl());
        entity.setFirstLetter(req.getFirstLetter());
        entity.setSortOrder(req.getSortOrder());
        entity.setStatus(req.getStatus());
        entity.setDescription(req.getDescription());
    }
}
