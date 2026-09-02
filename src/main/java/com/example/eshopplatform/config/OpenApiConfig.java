package com.example.eshopplatform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI 配置。
 * 文档地址：http://localhost:8080/swagger-ui.html（Security 白名单已放行）
 * 目前暂无业务接口，文档仅展示基础设施接口（如 /api/v1/health）；
 * 标签命名约定：tag name 用英文驼峰（如 productAdmin），中文说明统一写在
 * description；认证方式：请求头 Authorization: Bearer &lt;accessToken&gt;。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eshopplatformOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("电商平台 API")
                        .description("""
                                电商小程序平台 1.0 接口文档。
                                认证方式：请求头 Authorization: Bearer <accessToken>""")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Access Token，格式：Bearer <token>")));
    }
}
