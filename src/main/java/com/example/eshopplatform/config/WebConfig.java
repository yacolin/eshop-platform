package com.example.eshopplatform.config;

import com.example.eshopplatform.common.web.AdminController;
import com.example.eshopplatform.common.web.PublicController;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <p>
 * Web MVC 配置：控制器路由前缀统一收口
 * </p>
 *
 * <p>控制器只写业务路径（如 {@code @RequestMapping("/brands")}），由本配置按端统一加前缀：
 * <ul>
 *   <li>{@link AdminController} → {@code /api/v1/admin}；</li>
 *   <li>{@link PublicController} → {@code /api/v1/public}。</li>
 * </ul>
 * 注意：Spring 的 {@code PathMatchConfigurer#addPathPrefix} 对同一控制器<b>只取第一个命中的前缀</b>
 * （{@code RequestMappingHandlerMapping#getPathPrefix} 命中即返回），故两端各用一个「完整前缀」，
 * 不再叠加 {@code /api/v1}。运行时最终路径与显式写全路径一致，Security 的
 * {@code admin-paths}/{@code whitelist} 与 springdoc 的 {@code paths-to-match} 仍按完整路径匹配；
 * 未标注的控制器（如健康检查 {@code /api/v1/health}）不受影响。</p>
 *
 * @since 2026-09-12
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** 管理端统一前缀 */
    static final String ADMIN_PATH_PREFIX = "/api/v1/admin";

    /** 公开/小程序端统一前缀 */
    static final String PUBLIC_PATH_PREFIX = "/api/v1/public";

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(ADMIN_PATH_PREFIX,
                c -> c.isAnnotationPresent(AdminController.class));
        configurer.addPathPrefix(PUBLIC_PATH_PREFIX,
                c -> c.isAnnotationPresent(PublicController.class));
    }
}
