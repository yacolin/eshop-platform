package com.example.eshopplatform.sp.categoryAttribute.controller;

import com.example.eshopplatform.config.SecurityConfig;
import com.example.eshopplatform.config.SecurityProperties;
import com.example.eshopplatform.security.JwtTokenProvider;
import com.example.eshopplatform.sp.categoryAttribute.dto.CategoryAttributeVO;
import com.example.eshopplatform.sp.categoryAttribute.service.CategoryAttributesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 类目属性关联接口 Web 层测试（公开白名单 /api/v1/category-attributes/**）。
 */
@WebMvcTest(CategoryAttributesController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties(SecurityProperties.class)
class CategoryAttributesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryAttributesService categoryAttributesService;
    /** SecurityConfig 装配 JWT 过滤器需要；公开接口走白名单，过滤器不参与认证 */
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void list_missingCategoryId_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/category-attributes"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000))
                .andExpect(jsonPath("$.message").value("categoryId 不能为空"));
    }

    @Test
    void list_shouldReturnRows() throws Exception {
        CategoryAttributeVO vo = new CategoryAttributeVO();
        vo.setId(1L);
        vo.setCategoryId(1L);
        vo.setAttributeId(3L);
        when(categoryAttributesService.listByCategory(1L)).thenReturn(List.of(vo));

        mockMvc.perform(get("/api/v1/category-attributes").param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].attributeId").value(3));
    }

    @Test
    void create_shouldAcceptBodyAndReturnVo() throws Exception {
        CategoryAttributeVO vo = new CategoryAttributeVO();
        vo.setId(9L);
        vo.setCategoryId(1L);
        vo.setAttributeId(3L);
        when(categoryAttributesService.create(any())).thenReturn(vo);

        mockMvc.perform(post("/api/v1/category-attributes")
                        .contentType("application/json")
                        .content("{\"categoryId\":1,\"attributeId\":3,\"sortOrder\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(9));
    }

    @Test
    void batchCreate_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/v1/category-attributes/batch")
                        .contentType("application/json")
                        .content("{\"categoryId\":1,\"items\":[{\"attributeId\":3},{\"attributeId\":4}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void delete_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/v1/category-attributes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(categoryAttributesService).delete(1L);
    }
}
