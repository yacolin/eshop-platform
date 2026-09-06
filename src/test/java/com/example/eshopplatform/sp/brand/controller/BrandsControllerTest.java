package com.example.eshopplatform.sp.brand.controller;

import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.common.ErrorCode;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.config.SecurityConfig;
import com.example.eshopplatform.config.SecurityProperties;
import com.example.eshopplatform.security.JwtTokenProvider;
import com.example.eshopplatform.sp.brand.dto.BrandsVO;
import com.example.eshopplatform.sp.brand.service.BrandsService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 品牌接口 Web 层测试（公开白名单 /api/v1/brands/**，走真实 Security 链）。
 */
@WebMvcTest(BrandsController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties(SecurityProperties.class)
class BrandsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BrandsService brandsService;
    /** SecurityConfig 装配 JWT 过滤器需要；公开接口走白名单，过滤器不参与认证 */
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private BrandsVO vo() {
        BrandsVO vo = new BrandsVO();
        vo.setId(1L);
        vo.setName("苹果");
        vo.setEnglishName("Apple");
        vo.setLogoUrl("http://x/logo.png");
        vo.setFirstLetter("A");
        vo.setSortOrder(1);
        vo.setStatus((byte) 1);
        vo.setDescription("品牌故事");
        return vo;
    }

    @Test
    void page_shouldReturnEnvelope() throws Exception {
        when(brandsService.page(eq(1), eq(10), isNull(), isNull(), isNull()))
                .thenReturn(PageResult.of(1, List.of(vo())));

        mockMvc.perform(get("/api/v1/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].name").value("苹果"));
    }

    @Test
    void page_withFilters_shouldPassQueryParams() throws Exception {
        when(brandsService.page(eq(1), eq(10), eq("苹果"), eq("A"), eq("1")))
                .thenReturn(PageResult.of(1, List.of(vo())));

        mockMvc.perform(get("/api/v1/brands")
                        .param("name", "苹果").param("firstLetter", "A").param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].firstLetter").value("A"));
    }

    @Test
    void detail_shouldReturnVo() throws Exception {
        when(brandsService.getById(1L)).thenReturn(vo());

        mockMvc.perform(get("/api/v1/brands/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("苹果"));
    }

    @Test
    void detail_missing_shouldReturn404WithErrorCode() throws Exception {
        when(brandsService.getById(99L))
                .thenThrow(new BizException(ErrorCode.NOT_FOUND, "品牌不存在", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/v1/brands/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40400))
                .andExpect(jsonPath("$.message").value("品牌不存在"));
    }

    @Test
    void create_shouldAcceptCamelBodyAndReturnVo() throws Exception {
        when(brandsService.create(any())).thenReturn(vo());

        mockMvc.perform(post("/api/v1/brands")
                        .contentType("application/json")
                        .content("{\"name\":\"苹果\",\"firstLetter\":\"A\",\"sortOrder\":1,\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("苹果"));
    }

    @Test
    void update_shouldReturnUpdatedVo() throws Exception {
        when(brandsService.update(eq(1L), any())).thenReturn(vo());

        mockMvc.perform(put("/api/v1/brands/1")
                        .contentType("application/json")
                        .content("{\"name\":\"苹果\",\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("苹果"));
    }

    @Test
    void delete_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/v1/brands/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(brandsService).delete(1L);
    }
}
