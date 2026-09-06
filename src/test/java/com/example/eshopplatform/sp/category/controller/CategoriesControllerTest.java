package com.example.eshopplatform.sp.category.controller;

import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.common.ErrorCode;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.config.SecurityConfig;
import com.example.eshopplatform.config.SecurityProperties;
import com.example.eshopplatform.security.JwtTokenProvider;
import com.example.eshopplatform.sp.category.dto.CategoriesTreeVO;
import com.example.eshopplatform.sp.category.dto.CategoriesVO;
import com.example.eshopplatform.sp.categoryAttribute.dto.CategoryAttributeVO;
import com.example.eshopplatform.sp.category.dto.CategoryBrandVO;
import com.example.eshopplatform.sp.category.service.CategoriesService;
import com.example.eshopplatform.sp.categoryAttribute.service.CategoryAttributesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 类目接口 Web 层测试（公开白名单 /api/v1/categories/**，走真实 Security 链）。
 */
@WebMvcTest(CategoriesController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties(SecurityProperties.class)
class CategoriesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriesService categoriesService;
    @MockitoBean
    private CategoryAttributesService categoryAttributesService;
    /** SecurityConfig 装配 JWT 过滤器需要；公开接口走白名单，过滤器不参与认证 */
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private CategoriesVO vo(Long id, String name) {
        CategoriesVO vo = new CategoriesVO();
        vo.setId(id);
        vo.setName(name);
        vo.setParentId(0L);
        vo.setLevel((byte) 1);
        vo.setSortOrder(1);
        vo.setStatus((byte) 1);
        return vo;
    }

    private CategoriesTreeVO treeNode(Long id, String name) {
        CategoriesTreeVO node = new CategoriesTreeVO();
        node.setId(id);
        node.setName(name);
        node.setParentId(0L);
        node.setLevel((byte) 1);
        node.setStatus((byte) 1);
        return node;
    }

    @Test
    void page_shouldPassParentFilter() throws Exception {
        when(categoriesService.page(eq(1), eq(10), eq(1L), isNull(), isNull(), isNull()))
                .thenReturn(PageResult.of(1, List.of(vo(2L, "手机"))));

        mockMvc.perform(get("/api/v1/categories").param("parent_id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].name").value("手机"));
    }

    @Test
    void all_shouldReturnList() throws Exception {
        when(categoriesService.listAll()).thenReturn(List.of(vo(1L, "电子产品")));

        mockMvc.perform(get("/api/v1/categories/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("电子产品"));
    }

    @Test
    void rootAndNonRoot_shouldReturnList() throws Exception {
        when(categoriesService.listRoot()).thenReturn(List.of(vo(1L, "电子产品")));
        when(categoriesService.listNonRoot()).thenReturn(List.of(vo(2L, "手机")));

        mockMvc.perform(get("/api/v1/categories/root"))
                .andExpect(jsonPath("$.data[0].name").value("电子产品"));
        mockMvc.perform(get("/api/v1/categories/nonroot"))
                .andExpect(jsonPath("$.data[0].name").value("手机"));
    }

    @Test
    void children_shouldReturnSubList() throws Exception {
        when(categoriesService.listChildren(1L)).thenReturn(List.of(vo(2L, "手机")));

        mockMvc.perform(get("/api/v1/categories/1/children"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(2));
    }

    @Test
    void level_shouldReturnList() throws Exception {
        when(categoriesService.listByLevel((byte) 2)).thenReturn(List.of(vo(3L, "平板")));

        mockMvc.perform(get("/api/v1/categories/level/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("平板"));
    }

    @Test
    void tree_shouldReturnNestedJson() throws Exception {
        CategoriesTreeVO child = treeNode(2L, "手机");
        CategoriesTreeVO root = treeNode(1L, "电子产品");
        root.setChildren(List.of(child));
        when(categoriesService.tree(isNull())).thenReturn(List.of(root));

        mockMvc.perform(get("/api/v1/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("电子产品"))
                .andExpect(jsonPath("$.data[0].children[0].name").value("手机"));
    }

    @Test
    void categoryBrands_shouldReturnItems() throws Exception {
        CategoryBrandVO item = new CategoryBrandVO();
        item.setId(1L);
        item.setCategoryId(1L);
        item.setBrandId(10L);
        item.setSortOrder(1);
        item.setBrandName("苹果");
        when(categoriesService.listCategoryBrands(1L)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/categories/1/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].brandName").value("苹果"));
    }

    @Test
    void updateCategoryBrands_shouldReplace() throws Exception {
        mockMvc.perform(put("/api/v1/categories/1/brands")
                        .contentType("application/json")
                        .content("{\"brandIds\":[10,20],\"sortOrder\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(categoriesService).replaceCategoryBrands(eq(1L), any());
    }

    @Test
    void categoryAttributes_shouldReturnList() throws Exception {
        CategoryAttributeVO attr = new CategoryAttributeVO();
        attr.setId(1L);
        attr.setCategoryId(1L);
        attr.setAttributeId(3L);
        when(categoryAttributesService.listByCategory(1L)).thenReturn(List.of(attr));

        mockMvc.perform(get("/api/v1/categories/1/attributes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].attributeId").value(3));
    }

    @Test
    void detail_missing_shouldReturn404WithErrorCode() throws Exception {
        when(categoriesService.getById(99L))
                .thenThrow(new BizException(ErrorCode.NOT_FOUND, "类目不存在", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/v1/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40400))
                .andExpect(jsonPath("$.message").value("类目不存在"));
    }
}
