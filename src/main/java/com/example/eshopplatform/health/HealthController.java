package com.example.eshopplatform.health;

import com.example.eshopplatform.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查接口：验证服务已启动、可正常响应。
 * 路径已加入 Security 白名单（eshop.security.whitelist），无需登录即可访问。
 * 部署探活 / 启动自检：GET /api/v1/health
 */
@Tag(name = "health", description = "健康检查（启动探活）")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("service", "eshop-platform");
        data.put("time", System.currentTimeMillis());
        return ApiResponse.ok(data);
    }
}
