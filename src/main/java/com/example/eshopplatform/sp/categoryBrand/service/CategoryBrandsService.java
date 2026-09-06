package com.example.eshopplatform.sp.categoryBrand.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.sp.brand.entity.Brands;
import com.example.eshopplatform.sp.brand.mapper.BrandsMapper;
import com.example.eshopplatform.sp.category.mapper.CategoriesMapper;
import com.example.eshopplatform.sp.categoryBrand.dto.CategoryBrandUpdateReq;
import com.example.eshopplatform.sp.categoryBrand.dto.CategoryBrandVO;
import com.example.eshopplatform.sp.categoryBrand.entity.CategoryBrands;
import com.example.eshopplatform.sp.categoryBrand.mapper.CategoryBrandsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 类目-品牌关联业务（sp_category_brands）——独立业务包，接口仍挂在
 * 类目资源路径下（GET/PUT /api/v1/categories/{categoryId}/brands）。
 * </p>
 *
 * <p>替换语义：全量替换（先删旧关联、再按序插入），类目需存在、品牌列表非空且全部存在。</p>
 *
 * @since 2026-09-03
 */
@Service
@RequiredArgsConstructor
public class CategoryBrandsService {

    /** 关联表数据访问层 */
    private final CategoryBrandsMapper categoryBrandsMapper;

    /** 品牌（列表组装详情 / 存在性校验） */
    private final BrandsMapper brandsMapper;

    /** 类目（校验宿主类目存在） */
    private final CategoriesMapper categoriesMapper;

    /** 类目下品牌列表（关联记录 + 品牌详情，按关联排序权重升序） */
    public List<CategoryBrandVO> listByCategory(Long categoryId) {
        List<CategoryBrands> rels = categoryBrandsMapper.selectList(new LambdaQueryWrapper<CategoryBrands>()
                .eq(CategoryBrands::getCategoryId, categoryId)
                .orderByAsc(CategoryBrands::getSortOrder)
                .orderByDesc(CategoryBrands::getId));
        if (rels.isEmpty()) {
            return List.of();
        }
        Map<Long, Brands> brandMap = brandsMapper.selectBatchIds(
                        rels.stream().map(CategoryBrands::getBrandId).distinct().toList())
                .stream().collect(Collectors.toMap(Brands::getId, b -> b));
        List<CategoryBrandVO> list = new ArrayList<>(rels.size());
        for (CategoryBrands rel : rels) {
            CategoryBrandVO vo = new CategoryBrandVO();
            vo.setId(rel.getId());
            vo.setCategoryId(rel.getCategoryId());
            vo.setBrandId(rel.getBrandId());
            vo.setSortOrder(rel.getSortOrder());
            Brands brand = brandMap.get(rel.getBrandId());
            if (brand != null) {
                vo.setBrandName(brand.getName());
                vo.setEnglishName(brand.getEnglishName());
                vo.setLogoUrl(brand.getLogoUrl());
                vo.setFirstLetter(brand.getFirstLetter());
            }
            list.add(vo);
        }
        return list;
    }

    /**
     * 全量替换类目下品牌关联（先删旧关联、再按序插入，事务内完成）。
     * 类目需存在；品牌列表非空且全部存在。
     */
    @Transactional(rollbackFor = Exception.class)
    public void replaceByCategory(Long categoryId, CategoryBrandUpdateReq req) {
        requireCategory(categoryId);
        List<Long> brandIds = req.getBrandIds();
        if (brandIds == null || brandIds.isEmpty()) {
            throw BizException.badRequest("品牌列表不能为空");
        }
        long exist = brandsMapper.selectCount(new LambdaQueryWrapper<Brands>()
                .in(Brands::getId, brandIds));
        if (exist != brandIds.size()) {
            throw BizException.badRequest("存在无效的品牌ID");
        }
        categoryBrandsMapper.delete(new LambdaQueryWrapper<CategoryBrands>()
                .eq(CategoryBrands::getCategoryId, categoryId));
        for (Long brandId : brandIds) {
            CategoryBrands rel = new CategoryBrands();
            rel.setCategoryId(categoryId);
            rel.setBrandId(brandId);
            rel.setSortOrder(req.getSortOrder());
            categoryBrandsMapper.insert(rel);
        }
    }

    /** 宿主类目需存在（自动排除软删），不存在抛 404 */
    private void requireCategory(Long categoryId) {
        if (categoriesMapper.selectById(categoryId) == null) {
            throw BizException.notFound("类目不存在");
        }
    }
}
