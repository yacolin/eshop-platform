package com.example.eshopplatform.sp.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.eshopplatform.sp.category.entity.Categories;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 类目表（树状结构） Mapper 接口
 * </p>
 *
 * @since 2026-09-03
 */
@Mapper
public interface CategoriesMapper extends BaseMapper<Categories> {

}
