package com.example.eshopplatform.common.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 公开/小程序端控制器标记
 * </p>
 *
 * <p>配合 {@code WebConfig#configurePathMatch} 的 {@code addPathPrefix}，给标注本注解的
 * 控制器统一加上 {@code /api/v1/public} 前缀：控制器 {@code @RequestMapping} 只写业务路径
 * （如 {@code /products}），无需重复端前缀。运行时路径与显式写全路径完全一致，
 * Security 的 whitelist 与 springdoc 分组仍按完整路径匹配。</p>
 *
 * @since 2026-09-12
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PublicController {
}
