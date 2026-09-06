package com.example.eshopplatform.sp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.sp.categoryAttribute.dto.CategoryAttributeBatchReq;
import com.example.eshopplatform.sp.categoryAttribute.dto.CategoryAttributeCreateReq;
import com.example.eshopplatform.sp.categoryAttribute.dto.CategoryAttributeVO;
import com.example.eshopplatform.sp.categoryAttribute.entity.CategoryAttributes;
import com.example.eshopplatform.sp.categoryAttribute.mapper.CategoryAttributesMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 类目-属性关联服务（sp_category_attributes，类目推荐属性模板）
 * </p>
 *
 * <p>查询按类目取关联行；写操作仅落业务字段（created_at 由数据库维护），
 * 重复关联由唯一键 (category_id, attribute_id) 兜底。</p>
 *
 * @since 2026-09-03
 */
@Service
@RequiredArgsConstructor
public class CategoryAttributesService {

    private final CategoryAttributesMapper categoryAttributesMapper;

    /** 某类目下的属性关联列表（按排序权重升序，供类目/商品配置推荐属性） */
    public List<CategoryAttributeVO> listByCategory(Long categoryId) {
        return categoryAttributesMapper.selectList(new LambdaQueryWrapper<CategoryAttributes>()
                        .eq(CategoryAttributes::getCategoryId, categoryId)
                        .orderByAsc(CategoryAttributes::getSortOrder))
                .stream().map(this::toVO).toList();
    }

    /** 新增单条类目属性关联 */
    public CategoryAttributeVO create(CategoryAttributeCreateReq req) {
        if (req.getCategoryId() == null || req.getAttributeId() == null) {
            throw BizException.badRequest("类目ID与属性ID不能为空");
        }
        CategoryAttributes entity = new CategoryAttributes();
        entity.setCategoryId(req.getCategoryId());
        entity.setAttributeId(req.getAttributeId());
        entity.setRequired(req.getRequired());
        entity.setIsDefaultFilter(req.getIsDefaultFilter());
        entity.setSortOrder(req.getSortOrder());
        categoryAttributesMapper.insert(entity);
        return toVO(entity);
    }

    /** 批量新增类目属性关联（循环插入，逐条失败即中断） */
    public void batchCreate(CategoryAttributeBatchReq req) {
        if (req.getCategoryId() == null) {
            throw BizException.badRequest("类目ID不能为空");
        }
        List<CategoryAttributeBatchReq.Item> items = req.getItems();
        if (items == null || items.isEmpty()) {
            throw BizException.badRequest("属性列表不能为空");
        }
        for (CategoryAttributeBatchReq.Item item : items) {
            if (item.getAttributeId() == null) {
                throw BizException.badRequest("存在缺失属性ID的关联项");
            }
            CategoryAttributes entity = new CategoryAttributes();
            entity.setCategoryId(req.getCategoryId());
            entity.setAttributeId(item.getAttributeId());
            entity.setRequired(item.getRequired());
            entity.setIsDefaultFilter(item.getIsDefaultFilter());
            entity.setSortOrder(item.getSortOrder());
            categoryAttributesMapper.insert(entity);
        }
    }

    /** 删除关联记录（幂等：记录不存在同样返回成功） */
    public void delete(Long id) {
        categoryAttributesMapper.deleteById(id);
    }

    /** entity -> VO */
    private CategoryAttributeVO toVO(CategoryAttributes entity) {
        CategoryAttributeVO vo = new CategoryAttributeVO();
        vo.setId(entity.getId());
        vo.setCategoryId(entity.getCategoryId());
        vo.setAttributeId(entity.getAttributeId());
        vo.setRequired(entity.getRequired());
        vo.setIsDefaultFilter(entity.getIsDefaultFilter());
        vo.setSortOrder(entity.getSortOrder());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
