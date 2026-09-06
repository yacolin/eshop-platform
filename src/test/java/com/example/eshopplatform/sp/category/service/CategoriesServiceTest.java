package com.example.eshopplatform.sp.category.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.sp.category.dto.CategoriesReq;
import com.example.eshopplatform.sp.category.dto.CategoriesTreeVO;
import com.example.eshopplatform.sp.category.dto.CategoriesVO;
import com.example.eshopplatform.sp.category.entity.Categories;
import com.example.eshopplatform.sp.category.mapper.CategoriesMapper;
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
 * 类目表服务单元测试：查询面（平铺/根/非根/子级/层级/树）、
 * 写操作只落业务字段、类目下品牌关联（列表组装 / 事务替换）。
 */
@ExtendWith(MockitoExtension.class)
class CategoriesServiceTest {

    @Mock
    private CategoriesMapper categoriesMapper;

    private CategoriesService service;

    /** 纯单测无 MyBatis 运行时：预初始化实体元数据，使 Lambda 条件可解析列名 */
    @BeforeAll
    static void initMybatisTableInfo() {
        for (Class<?> clazz : new Class<?>[]{Categories.class}) {
            MybatisConfiguration configuration = new MybatisConfiguration();
            MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
            assistant.setCurrentNamespace(clazz.getName());
            TableInfoHelper.initTableInfo(assistant, clazz);
        }
    }

    @BeforeEach
    void setUp() {
        service = new CategoriesService(categoriesMapper);
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

    private CategoriesReq req() {
        CategoriesReq r = new CategoriesReq();
        r.setName("手机");
        r.setParentId(1L);
        r.setLevel((byte) 2);
        r.setPath("1/");
        r.setSortOrder(1);
        r.setStatus((byte) 1);
        return r;
    }

    // ---------- 写操作 ----------

    @Test
    void create_shouldPersistBusinessFieldsOnly() {
        when(categoriesMapper.insert(any(Categories.class))).thenAnswer(inv -> {
            Categories c = inv.getArgument(0);
            c.setId(20L);
            return 1;
        });

        CategoriesVO vo = service.create(req());

        ArgumentCaptor<Categories> captor = ArgumentCaptor.forClass(Categories.class);
        verify(categoriesMapper).insert(captor.capture());
        Categories persisted = captor.getValue();
        assertThat(persisted.getName()).isEqualTo("手机");
        assertThat(persisted.getParentId()).isEqualTo(1L);
        assertThat(persisted.getPath()).isEqualTo("1/");
        // 自动列不接受客户端输入：时间列保持为空，由数据库默认值维护
        assertThat(persisted.getCreatedAt()).isNull();
        assertThat(persisted.getUpdatedAt()).isNull();
        assertThat(persisted.getDeletedAt()).isNull();
        assertThat(vo.getId()).isEqualTo(20L);
        assertThat(vo.getName()).isEqualTo("手机");
    }

    @Test
    void getById_missing_shouldThrow404() {
        when(categoriesMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(BizException.class)
                .hasMessage("类目不存在");
    }

    @Test
    void update_missing_shouldThrow404AndNotUpdate() {
        when(categoriesMapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() -> service.update(1L, req()))
                .isInstanceOf(BizException.class)
                .hasMessage("类目不存在");
        verify(categoriesMapper, never()).updateById(any(Categories.class));
    }

    @Test
    void update_shouldPersistBusinessFields() {
        when(categoriesMapper.selectById(1L)).thenReturn(category(1L, 0L, "电子产品", (byte) 1));

        CategoriesReq r = req();
        r.setName("数码");
        CategoriesVO vo = service.update(1L, r);

        ArgumentCaptor<Categories> captor = ArgumentCaptor.forClass(Categories.class);
        verify(categoriesMapper).updateById(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("数码");
        assertThat(captor.getValue().getDeletedAt()).isNull();
        assertThat(vo.getName()).isEqualTo("数码");
    }

    @Test
    void delete_missing_shouldThrow404AndNotDelete() {
        when(categoriesMapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(BizException.class)
                .hasMessage("类目不存在");
        verify(categoriesMapper, never()).deleteById(any(Long.class));
    }

    // ---------- 查询面 ----------

    @Test
    void page_shouldReturnMappedList() {
        when(categoriesMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(inv -> {
            Page<Categories> p = inv.getArgument(0);
            p.setRecords(List.of(category(1L, 0L, "电子产品", (byte) 1)));
            p.setTotal(1);
            return p;
        });

        PageResult<CategoriesVO> result = service.page(1, 10, null, null, null, null);

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.list()).hasSize(1);
        assertThat(result.list().get(0).getName()).isEqualTo("电子产品");
    }

    @Test
    void listAllRootNonRootChildrenLevel_shouldReturnMapped() {
        Categories root = category(1L, 0L, "电子产品", (byte) 1);
        Categories child = category(2L, 1L, "手机", (byte) 2);
        // 调用顺序：listAll -> listRoot -> listNonRoot -> listChildren -> listByLevel
        when(categoriesMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(root), List.of(root), List.of(child), List.of(child), List.of(child));

        assertThat(service.listAll()).extracting(CategoriesVO::getName).containsExactly("电子产品");
        assertThat(service.listRoot()).extracting(CategoriesVO::getName).containsExactly("电子产品");
        List<CategoriesVO> nonRoot = service.listNonRoot();
        assertThat(nonRoot).hasSize(1);
        assertThat(nonRoot.get(0).getParentId()).isEqualTo(1L);
        assertThat(service.listChildren(1L)).extracting(CategoriesVO::getName).containsExactly("手机");
        List<CategoriesVO> level2 = service.listByLevel((byte) 2);
        assertThat(level2).hasSize(1);
        assertThat(level2.get(0).getLevel()).isEqualTo((byte) 2);
        verify(categoriesMapper, times(5)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void tree_shouldBuildNestedStructure() {
        when(categoriesMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(
                        category(1L, 0L, "电子产品", (byte) 1),
                        category(2L, 1L, "手机", (byte) 2),
                        category(3L, 2L, "智能手机", (byte) 3),
                        category(4L, 0L, "服装", (byte) 1)));

        List<CategoriesTreeVO> tree = service.tree(null);

        assertThat(tree).hasSize(2);
        assertThat(tree.get(0).getName()).isEqualTo("电子产品");
        assertThat(tree.get(0).getChildren()).hasSize(1);
        assertThat(tree.get(0).getChildren().get(0).getName()).isEqualTo("手机");
        assertThat(tree.get(0).getChildren().get(0).getChildren().get(0).getName()).isEqualTo("智能手机");
        assertThat(tree.get(1).getName()).isEqualTo("服装");
        assertThat(tree.get(1).getChildren()).isEmpty();
    }

    // ---------- 类目下品牌（关联） ----------
}
