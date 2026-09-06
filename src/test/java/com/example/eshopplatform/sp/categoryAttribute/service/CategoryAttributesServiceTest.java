package com.example.eshopplatform.sp.categoryAttribute.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.sp.categoryAttribute.dto.CategoryAttributeBatchReq;
import com.example.eshopplatform.sp.categoryAttribute.dto.CategoryAttributeCreateReq;
import com.example.eshopplatform.sp.categoryAttribute.dto.CategoryAttributeVO;
import com.example.eshopplatform.sp.categoryAttribute.entity.CategoryAttributes;
import com.example.eshopplatform.sp.categoryAttribute.mapper.CategoryAttributesMapper;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 类目-属性关联服务单元测试：按类目列表、单条/批量新增（业务字段）、删除。
 */
@ExtendWith(MockitoExtension.class)
class CategoryAttributesServiceTest {

    @Mock
    private CategoryAttributesMapper categoryAttributesMapper;

    private CategoryAttributesService service;

    /** 纯单测无 MyBatis 运行时：预初始化实体元数据，使 Lambda 条件可解析列名 */
    @BeforeAll
    static void initMybatisTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        assistant.setCurrentNamespace(CategoryAttributes.class.getName());
        TableInfoHelper.initTableInfo(assistant, CategoryAttributes.class);
    }

    @BeforeEach
    void setUp() {
        service = new CategoryAttributesService(categoryAttributesMapper);
    }

    private CategoryAttributes row(Long id, Long categoryId, Long attributeId) {
        CategoryAttributes a = new CategoryAttributes();
        a.setId(id);
        a.setCategoryId(categoryId);
        a.setAttributeId(attributeId);
        a.setRequired((byte) 1);
        a.setIsDefaultFilter((byte) 0);
        a.setSortOrder(1);
        return a;
    }

    @Test
    void listByCategory_shouldMapRelationRows() {
        when(categoryAttributesMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(row(1L, 1L, 3L)));

        List<CategoryAttributeVO> list = service.listByCategory(1L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getAttributeId()).isEqualTo(3L);
        assertThat(list.get(0).getRequired()).isEqualTo((byte) 1);
        assertThat(list.get(0).getIsDefaultFilter()).isZero();
    }

    @Test
    void create_shouldPersistBusinessFields() {
        when(categoryAttributesMapper.insert(any(CategoryAttributes.class))).thenAnswer(inv -> {
            CategoryAttributes a = inv.getArgument(0);
            a.setId(9L);
            return 1;
        });
        CategoryAttributeCreateReq req = new CategoryAttributeCreateReq();
        req.setCategoryId(1L);
        req.setAttributeId(3L);
        req.setRequired((byte) 1);
        req.setIsDefaultFilter((byte) 0);
        req.setSortOrder(2);

        CategoryAttributeVO vo = service.create(req);

        ArgumentCaptor<CategoryAttributes> captor = ArgumentCaptor.forClass(CategoryAttributes.class);
        verify(categoryAttributesMapper).insert(captor.capture());
        CategoryAttributes persisted = captor.getValue();
        assertThat(persisted.getCategoryId()).isEqualTo(1L);
        assertThat(persisted.getAttributeId()).isEqualTo(3L);
        assertThat(persisted.getRequired()).isEqualTo((byte) 1);
        assertThat(persisted.getCreatedAt()).isNull(); // 时间列由数据库维护
        assertThat(vo.getId()).isEqualTo(9L);
    }

    @Test
    void create_missingKey_shouldThrow400() {
        CategoryAttributeCreateReq req = new CategoryAttributeCreateReq();
        req.setCategoryId(1L);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(BizException.class)
                .hasMessage("类目ID与属性ID不能为空");
    }

    @Test
    void batchCreate_valid_shouldInsertEach() {
        CategoryAttributeBatchReq req = new CategoryAttributeBatchReq();
        req.setCategoryId(1L);
        CategoryAttributeBatchReq.Item i1 = new CategoryAttributeBatchReq.Item();
        i1.setAttributeId(3L);
        i1.setSortOrder(1);
        CategoryAttributeBatchReq.Item i2 = new CategoryAttributeBatchReq.Item();
        i2.setAttributeId(4L);
        i2.setSortOrder(2);
        req.setItems(List.of(i1, i2));

        service.batchCreate(req);

        ArgumentCaptor<CategoryAttributes> captor = ArgumentCaptor.forClass(CategoryAttributes.class);
        verify(categoryAttributesMapper, times(2)).insert(captor.capture());
        List<CategoryAttributes> inserted = captor.getAllValues();
        assertThat(inserted.get(0).getCategoryId()).isEqualTo(1L);
        assertThat(inserted.get(0).getAttributeId()).isEqualTo(3L);
        assertThat(inserted.get(1).getAttributeId()).isEqualTo(4L);
    }

    @Test
    void batchCreate_emptyItems_shouldThrow400() {
        CategoryAttributeBatchReq req = new CategoryAttributeBatchReq();
        req.setCategoryId(1L);

        assertThatThrownBy(() -> service.batchCreate(req))
                .isInstanceOf(BizException.class)
                .hasMessage("属性列表不能为空");
    }

    @Test
    void delete_shouldDeleteById() {
        service.delete(1L);

        verify(categoryAttributesMapper).deleteById(1L);
    }
}
