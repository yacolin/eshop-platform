package com.example.eshopplatform.sp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.sp.dto.CategoriesReq;
import com.example.eshopplatform.sp.dto.CategoriesTreeVO;
import com.example.eshopplatform.sp.dto.CategoriesVO;
import com.example.eshopplatform.sp.dto.CategoryBrandUpdateReq;
import com.example.eshopplatform.sp.dto.CategoryBrandVO;
import com.example.eshopplatform.sp.entity.Brands;
import com.example.eshopplatform.sp.entity.Categories;
import com.example.eshopplatform.sp.entity.CategoryBrands;
import com.example.eshopplatform.sp.mapper.BrandsMapper;
import com.example.eshopplatform.sp.mapper.CategoriesMapper;
import com.example.eshopplatform.sp.mapper.CategoryBrandsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 类目表（树状结构）服务
 * </p>
 *
 * <p>工程约定：Service 为具体类（不生成接口与 *ServiceImpl），直接注入 Mapper；
 * 入参/出参走 DTO（Req/VO）。查询覆盖平铺分页/全部/根/非根/子级/层级/树形；
 * 写操作仅落业务字段（created_at/updated_at/deleted_at 由数据库维护）。</p>
 *
 * @since 2026-09-03
 */
@Service
@RequiredArgsConstructor
public class CategoriesService {

    /** 数据访问层 */
    private final CategoriesMapper categoriesMapper;
    private final CategoryBrandsMapper categoryBrandsMapper;
    private final BrandsMapper brandsMapper;

    /**
     * 分页查询类目（平铺列表，第 page 页，每页 size 条）。
     * 可选筛选：parentId>0 按父级、status 按状态、name 按名称模糊、level>0 按层级。
     */
    public PageResult<CategoriesVO> page(int page, int size,
                                         Long parentId, Byte status, String name, Byte level) {
        Page<Categories> p = new Page<>(Math.max(page, 1), Math.min(Math.max(size, 1), 1000));
        LambdaQueryWrapper<Categories> wrapper = new LambdaQueryWrapper<>();
        if (parentId != null && parentId > 0) {
            wrapper.eq(Categories::getParentId, parentId);
        }
        if (status != null) {
            wrapper.eq(Categories::getStatus, status);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like(Categories::getName, name);
        }
        if (level != null && level > 0) {
            wrapper.eq(Categories::getLevel, level);
        }
        wrapper.orderByAsc(Categories::getSortOrder).orderByDesc(Categories::getId);
        categoriesMapper.selectPage(p, wrapper);
        return PageResult.of(p.getTotal(), p.getRecords().stream().map(this::toVO).toList());
    }

    /** 全部类目（平铺） */
    public List<CategoriesVO> listAll() {
        return categoriesMapper.selectList(new LambdaQueryWrapper<Categories>()
                .orderByAsc(Categories::getSortOrder)
                .orderByDesc(Categories::getId)).stream().map(this::toVO).toList();
    }

    /** 根类目列表（parent_id=0） */
    public List<CategoriesVO> listRoot() {
        return categoriesMapper.selectList(new LambdaQueryWrapper<Categories>()
                .eq(Categories::getParentId, 0L)
                .orderByAsc(Categories::getSortOrder)
                .orderByDesc(Categories::getId)).stream().map(this::toVO).toList();
    }

    /** 非根类目列表（parent_id>0，用于品牌/属性绑定联动） */
    public List<CategoriesVO> listNonRoot() {
        return categoriesMapper.selectList(new LambdaQueryWrapper<Categories>()
                .gt(Categories::getParentId, 0L)
                .orderByAsc(Categories::getSortOrder)
                .orderByDesc(Categories::getId)).stream().map(this::toVO).toList();
    }

    /** 子类目列表（parent_id=id） */
    public List<CategoriesVO> listChildren(Long id) {
        return categoriesMapper.selectList(new LambdaQueryWrapper<Categories>()
                .eq(Categories::getParentId, id)
                .orderByAsc(Categories::getSortOrder)
                .orderByDesc(Categories::getId)).stream().map(this::toVO).toList();
    }

    /** 指定层级类目列表 */
    public List<CategoriesVO> listByLevel(Byte level) {
        return categoriesMapper.selectList(new LambdaQueryWrapper<Categories>()
                .eq(Categories::getLevel, level)
                .orderByAsc(Categories::getSortOrder)
                .orderByDesc(Categories::getId)).stream().map(this::toVO).toList();
    }

    /** 类目树形结构（可选按状态过滤，从根节点向下组装） */
    public List<CategoriesTreeVO> tree(Byte status) {
        LambdaQueryWrapper<Categories> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Categories::getStatus, status);
        }
        wrapper.orderByAsc(Categories::getSortOrder).orderByAsc(Categories::getId);
        return buildTree(categoriesMapper.selectList(wrapper), 0L);
    }

    /** 类目下品牌列表（关联记录 + 品牌详情，按关联排序权重升序） */
    public List<CategoryBrandVO> listCategoryBrands(Long categoryId) {
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
    public void replaceCategoryBrands(Long categoryId, CategoryBrandUpdateReq req) {
        require(categoryId);
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

    /** 按主键查询 */
    public CategoriesVO getById(Long id) {
        return toVO(require(id));
    }

    /** 新增 */
    public CategoriesVO create(CategoriesReq req) {
        Categories entity = new Categories();
        apply(entity, req);
        categoriesMapper.insert(entity);
        return toVO(entity);
    }

    /** 按主键更新 */
    public CategoriesVO update(Long id, CategoriesReq req) {
        Categories entity = require(id);
        apply(entity, req);
        categoriesMapper.updateById(entity);
        return toVO(entity);
    }

    /** 按主键删除 */
    public void delete(Long id) {
        require(id);
        categoriesMapper.deleteById(id);
    }

    /** 按主键查询，不存在抛 404 */
    private Categories require(Long id) {
        Categories entity = categoriesMapper.selectById(id);
        if (entity == null) {
            throw BizException.notFound("类目不存在");
        }
        return entity;
    }

    /** entity -> VO（deleted_at 为服务端软删字段，不对业务端暴露） */
    private CategoriesVO toVO(Categories entity) {
        CategoriesVO vo = new CategoriesVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setParentId(entity.getParentId());
        vo.setLevel(entity.getLevel());
        vo.setPath(entity.getPath());
        vo.setIconUrl(entity.getIconUrl());
        vo.setSortOrder(entity.getSortOrder());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    /** 递归组装树：按父级把节点挂到对应父节点下（根为 parent_id=0） */
    private List<CategoriesTreeVO> buildTree(List<Categories> nodes, Long parentId) {
        List<CategoriesTreeVO> tree = new ArrayList<>();
        for (Categories node : nodes) {
            if (node.getParentId() != null && node.getParentId().equals(parentId)) {
                CategoriesTreeVO item = toTreeVO(node);
                item.setChildren(buildTree(nodes, node.getId()));
                tree.add(item);
            }
        }
        return tree;
    }

    /** entity -> 树节点 VO（字段与 toVO 一致，另挂子级列表） */
    private CategoriesTreeVO toTreeVO(Categories entity) {
        CategoriesTreeVO vo = new CategoriesTreeVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setParentId(entity.getParentId());
        vo.setLevel(entity.getLevel());
        vo.setPath(entity.getPath());
        vo.setIconUrl(entity.getIconUrl());
        vo.setSortOrder(entity.getSortOrder());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    /**
     * Req -> entity（仅落业务字段）；created_at/updated_at/deleted_at 由数据库
     * 自动维护，不接受客户端输入（level/path 随父级类目由调用方按树规则填写）。
     */
    private void apply(Categories entity, CategoriesReq req) {
        entity.setName(req.getName());
        entity.setParentId(req.getParentId());
        entity.setLevel(req.getLevel());
        entity.setPath(req.getPath());
        entity.setIconUrl(req.getIconUrl());
        entity.setSortOrder(req.getSortOrder());
        entity.setStatus(req.getStatus());
    }
}
