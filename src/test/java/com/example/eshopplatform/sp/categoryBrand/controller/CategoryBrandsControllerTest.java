package com.example.eshopplatform.sp.categoryBrand.controller;

import com.example.eshopplatform.config.SecurityConfig;
import com.example.eshopplatform.config.SecurityProperties;
import com.example.eshopplatform.security.JwtTokenProvider;
import com.example.eshopplatform.sp.categoryBrand.dto.CategoryBrandVO;
import com.example.eshopplatform.sp.categoryBrand.service.CategoryBrandsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 类目-品牌关联接口 Web 层测试（公开白名单 /api/v1/categories/**，走真实 Security 链）。
 */
@WebMvcTest(CategoryBrandsController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties(SecurityProperties.class)
class CategoryBrandsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryBrandsService categoryBrandsService;
    /** SecurityConfig 装配 JWT 过滤器需要；公开接口走白名单，过滤器不参与认证 */
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void listByCategory_shouldReturnItems() throws Exception {
        CategoryBrandVO item = new CategoryBrandVO();
        item.setId(1L);
        item.setCategoryId(1L);
        item.setBrandId(10L);
        item.setSortOrder(1);
        item.setBrandName("苹果");
        when(categoryBrandsService.listByCategory(1L)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/categories/1/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].brandName").value("苹果"));
    }

    @Test
    void replaceByCategory_shouldReplace() throws Exception {
        mockMvc.perform(put("/api/v1/categories/1/brands")
                        .contentType("application/json")
                        .content("{\"brandIds\":[10,20],\"sortOrder\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(categoryBrandsService).replaceByCategory(eq(1L), any());
    }
}
