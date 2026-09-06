package com.example.eshopplatform.sp.categoryBrand.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.sp.brand.entity.Brands;
import com.example.eshopplatform.sp.brand.mapper.BrandsMapper;
import com.example.eshopplatform.sp.category.entity.Categories;
import com.example.eshopplatform.sp.category.mapper.CategoriesMapper;
import com.example.eshopplatform.sp.categoryBrand.dto.CategoryBrandUpdateReq;
import com.example.eshopplatform.sp.categoryBrand.dto.CategoryBrandVO;
import com.example.eshopplatform.sp.categoryBrand.entity.CategoryBrands;
import com.example.eshopplatform.sp.categoryBrand.mapper.CategoryBrandsMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 类目-品牌关联服务单元测试：列表组装品牌详情 / 空关联 / 替换前置校验与先删后插。
 */
@ExtendWith(MockitoExtension.class)
class CategoryBrandsServiceTest {

    @Mock
    private CategoryBrandsMapper categoryBrandsMapper;
    @Mock
    private BrandsMapper brandsMapper;
    @Mock
    private CategoriesMapper categoriesMapper;

    private CategoryBrandsService service;

    /** 纯单测无 MyBatis 运行时：预初始化实体元数据，使 Lambda 条件可解析列名 */
    @BeforeAll
    static void initMybatisTableInfo() {
        for (Class<?> clazz : new Class<?>[]{Categories.class, CategoryBrands.class, Brands.class}) {
            MybatisConfiguration configuration = new MybatisConfiguration();
            MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
            assistant.setCurrentNamespace(clazz.getName());
            TableInfoHelper.initTableInfo(assistant, clazz);
        }
    }

    @BeforeEach
    void setUp() {
        service = new CategoryBrandsService(categoryBrandsMapper, brandsMapper, categoriesMapper);
    }

    private Categories category(Long id, Long parentId, String name, Byte level) {
        Categories c = new Categories();
        c.setId(id);
        c.setParentId(parentId);
        c.setName(name);
        c.setLevel(level);
        c.setPath(parentId == 0 ? "" : id + "/");
        c.setSortOrder(1);
        c.setStatus((byte) 1);
        return c;
    }

    @Test
    void listByCategory_shouldAssembleBrandDetails() {
        CategoryBrands rel1 = new CategoryBrands();
        rel1.setId(1L);
        rel1.setCategoryId(1L);
        rel1.setBrandId(10L);
        rel1.setSortOrder(1);
        CategoryBrands rel2 = new CategoryBrands();
        rel2.setId(2L);
        rel2.setCategoryId(1L);
        rel2.setBrandId(20L);
        rel2.setSortOrder(2);
        when(categoryBrandsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(rel1, rel2));

        Brands apple = new Brands();
        apple.setId(10L);
        apple.setName("苹果");
        apple.setEnglishName("Apple");
        apple.setLogoUrl("http://x/1.png");
        apple.setFirstLetter("A");
        Brands xiaomi = new Brands();
        xiaomi.setId(20L);
        xiaomi.setName("小米");
        when(brandsMapper.selectBatchIds(any())).thenReturn(List.of(apple, xiaomi));

        List<CategoryBrandVO> list = service.listByCategory(1L);

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getBrandName()).isEqualTo("苹果");
        assertThat(list.get(0).getBrandId()).isEqualTo(10L);
        assertThat(list.get(0).getEnglishName()).isEqualTo("Apple");
        assertThat(list.get(1).getBrandName()).isEqualTo("小米");
    }

    @Test
    void listByCategory_noRelations_shouldReturnEmpty() {
        when(categoryBrandsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        assertThat(service.listByCategory(1L)).isEmpty();
    }

    @Test
    void replaceByCategory_missingCategory_shouldThrow404() {
        when(categoriesMapper.selectById(1L)).thenReturn(null);

        CategoryBrandUpdateReq r = new CategoryBrandUpdateReq();
        r.setBrandIds(List.of(10L));

        assertThatThrownBy(() -> service.replaceByCategory(1L, r))
                .isInstanceOf(BizException.class)
                .hasMessage("类目不存在");
    }

    @Test
    void replaceByCategory_emptyBrandIds_shouldThrow400() {
        when(categoriesMapper.selectById(1L)).thenReturn(category(1L, 0L, "电子产品", (byte) 1));

        assertThatThrownBy(() -> service.replaceByCategory(1L, new CategoryBrandUpdateReq()))
                .isInstanceOf(BizException.class)
                .hasMessage("品牌列表不能为空");
    }

    @Test
    void replaceByCategory_invalidBrand_shouldThrow400() {
        when(categoriesMapper.selectById(1L)).thenReturn(category(1L, 0L, "电子产品", (byte) 1));
        when(brandsMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        CategoryBrandUpdateReq r = new CategoryBrandUpdateReq();
        r.setBrandIds(List.of(10L, 20L)); // 只有 1 个存在

        assertThatThrownBy(() -> service.replaceByCategory(1L, r))
                .isInstanceOf(BizException.class)
                .hasMessage("存在无效的品牌ID");
        verify(categoryBrandsMapper, never()).delete(any(LambdaQueryWrapper.class));
    }

    @Test
    void replaceByCategory_valid_shouldDeleteOldThenInsertNew() {
        when(categoriesMapper.selectById(1L)).thenReturn(category(1L, 0L, "电子产品", (byte) 1));
        when(brandsMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        CategoryBrandUpdateReq r = new CategoryBrandUpdateReq();
        r.setBrandIds(List.of(10L, 20L));
        r.setSortOrder(5);

        service.replaceByCategory(1L, r);

        verify(categoryBrandsMapper).delete(any(LambdaQueryWrapper.class));
        ArgumentCaptor<CategoryBrands> captor = ArgumentCaptor.forClass(CategoryBrands.class);
        verify(categoryBrandsMapper, times(2)).insert(captor.capture());
        List<CategoryBrands> inserted = captor.getAllValues();
        assertThat(inserted.get(0).getCategoryId()).isEqualTo(1L);
        assertThat(inserted.get(0).getBrandId()).isEqualTo(10L);
        assertThat(inserted.get(0).getSortOrder()).isEqualTo(5);
        assertThat(inserted.get(1).getBrandId()).isEqualTo(20L);
    }
}
