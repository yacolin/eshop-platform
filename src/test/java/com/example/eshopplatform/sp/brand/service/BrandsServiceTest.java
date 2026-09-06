package com.example.eshopplatform.sp.brand.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.sp.brand.dto.BrandsReq;
import com.example.eshopplatform.sp.brand.dto.BrandsVO;
import com.example.eshopplatform.sp.brand.entity.Brands;
import com.example.eshopplatform.sp.brand.mapper.BrandsMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 品牌表服务单元测试：写操作只落业务字段、时间列交数据库、删除/详情 404 语义。
 */
@ExtendWith(MockitoExtension.class)
class BrandsServiceTest {

    @Mock
    private BrandsMapper brandsMapper;

    private BrandsService service;

    /** 纯单测无 MyBatis 运行时：预初始化实体元数据，使 Lambda 条件可解析列名 */
    @BeforeAll
    static void initMybatisTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        assistant.setCurrentNamespace(Brands.class.getName());
        TableInfoHelper.initTableInfo(assistant, Brands.class);
    }

    @BeforeEach
    void setUp() {
        service = new BrandsService(brandsMapper);
    }

    private Brands brand(Long id, String name) {
        Brands b = new Brands();
        b.setId(id);
        b.setName(name);
        b.setEnglishName("Apple");
        b.setLogoUrl("http://x/logo.png");
        b.setFirstLetter("A");
        b.setSortOrder(1);
        b.setStatus((byte) 1);
        b.setDescription("品牌故事");
        b.setCreatedAt(LocalDateTime.of(2026, 8, 8, 10, 0));
        b.setUpdatedAt(LocalDateTime.of(2026, 8, 8, 10, 0));
        b.setDeletedAt(LocalDateTime.of(2026, 8, 8, 10, 0));
        return b;
    }

    private BrandsReq req() {
        BrandsReq r = new BrandsReq();
        r.setName("苹果");
        r.setEnglishName("Apple");
        r.setLogoUrl("http://x/logo.png");
        r.setFirstLetter("A");
        r.setSortOrder(1);
        r.setStatus((byte) 1);
        r.setDescription("品牌故事");
        return r;
    }

    @Test
    void create_shouldPersistBusinessFieldsOnly() {
        when(brandsMapper.insert(any(Brands.class))).thenAnswer(inv -> {
            Brands b = inv.getArgument(0);
            b.setId(10L);
            return 1;
        });

        BrandsVO vo = service.create(req());

        ArgumentCaptor<Brands> captor = ArgumentCaptor.forClass(Brands.class);
        verify(brandsMapper).insert(captor.capture());
        Brands persisted = captor.getValue();
        assertThat(persisted.getName()).isEqualTo("苹果");
        assertThat(persisted.getFirstLetter()).isEqualTo("A");
        assertThat(persisted.getSortOrder()).isEqualTo(1);
        // 自动列不接受客户端输入：时间列必须保持为空，由数据库默认值维护
        assertThat(persisted.getCreatedAt()).isNull();
        assertThat(persisted.getUpdatedAt()).isNull();
        assertThat(persisted.getDeletedAt()).isNull();
        assertThat(vo.getId()).isEqualTo(10L);
        assertThat(vo.getName()).isEqualTo("苹果");
    }

    @Test
    void create_withEmptyReq_shouldNotFailOnAutoColumns() {
        when(brandsMapper.insert(any(Brands.class))).thenReturn(1);

        service.create(new BrandsReq());

        ArgumentCaptor<Brands> captor = ArgumentCaptor.forClass(Brands.class);
        verify(brandsMapper).insert(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNull();
    }

    @Test
    void getById_shouldReturnVo() {
        when(brandsMapper.selectById(1L)).thenReturn(brand(1L, "苹果"));

        BrandsVO vo = service.getById(1L);

        assertThat(vo.getName()).isEqualTo("苹果");
        assertThat(vo.getCreatedAt()).isNotNull();
    }

    @Test
    void getById_missing_shouldThrow404() {
        when(brandsMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(BizException.class)
                .hasMessage("品牌不存在");
    }

    @Test
    void update_shouldPersistAndReturnVo() {
        when(brandsMapper.selectById(1L)).thenReturn(brand(1L, "苹果"));

        BrandsReq r = req();
        r.setName("华为");
        BrandsVO vo = service.update(1L, r);

        ArgumentCaptor<Brands> captor = ArgumentCaptor.forClass(Brands.class);
        verify(brandsMapper).updateById(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("华为");
        assertThat(captor.getValue().getCreatedAt()).isNotNull(); // 更新保留已有记录自动列
        assertThat(vo.getName()).isEqualTo("华为");
    }

    @Test
    void update_missing_shouldThrow404AndNotUpdate() {
        when(brandsMapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() -> service.update(1L, req()))
                .isInstanceOf(BizException.class)
                .hasMessage("品牌不存在");
        verify(brandsMapper, never()).updateById(any(Brands.class));
    }

    @Test
    void delete_existing_shouldDelete() {
        when(brandsMapper.selectById(1L)).thenReturn(brand(1L, "苹果"));

        service.delete(1L);

        verify(brandsMapper).deleteById(1L);
    }

    @Test
    void delete_missing_shouldThrow404AndNotDelete() {
        when(brandsMapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(BizException.class)
                .hasMessage("品牌不存在");
        verify(brandsMapper, never()).deleteById(any(Long.class));
    }

    @Test
    void page_shouldReturnTotalAndMappedList() {
        when(brandsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(inv -> {
            Page<Brands> p = inv.getArgument(0);
            p.setRecords(List.of(brand(1L, "苹果")));
            p.setTotal(1);
            return p;
        });

        PageResult<BrandsVO> result = service.page(1, 10, null, null, null);

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.list()).hasSize(1);
        assertThat(result.list().get(0).getName()).isEqualTo("苹果");
    }
}
