package com.letsvpn.gateway; // 包名可能需要调整

import org.springdoc.core.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j; // 需要 lombok
import java.util.stream.Collectors;

@Component
@Slf4j
public class DebugSwaggerProps {

    // 使用 required=false 避免在 Bean 找不到时启动失败
    @Autowired(required = false)
    private SwaggerUiConfigProperties swaggerUiConfigProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void checkSwaggerProps() {
        log.warn("==================== Swagger Debug Start ====================");
        if (swaggerUiConfigProperties == null) {
            log.warn("DEBUG CHECK: SwaggerUiConfigProperties bean is NULL!");
        } else {
            log.warn("DEBUG CHECK: SwaggerUiConfigProperties bean FOUND.");
            log.warn("DEBUG CHECK: swaggerUiConfigProperties.isEnabled() = {}", swaggerUiConfigProperties.isEnabled());
            log.warn("DEBUG CHECK: swaggerUiConfigProperties.getPath() = {}", swaggerUiConfigProperties.getPath());
            if (swaggerUiConfigProperties.getUrls() == null) {
                log.warn("DEBUG CHECK: swaggerUiConfigProperties.getUrls() is NULL");
            } else {
                log.warn("DEBUG CHECK: swaggerUiConfigProperties.getUrls() size = {}", swaggerUiConfigProperties.getUrls().size());
                // 打印具体的 URL 配置
                String urlsString = swaggerUiConfigProperties.getUrls().stream()
                        .map(url -> "  Name: " + url.getName() + ", URL: " + url.getUrl())
                        .collect(Collectors.joining("\n"));
                log.warn("DEBUG CHECK: swaggerUiConfigProperties.getUrls() content:\n{}", urlsString);
            }
            // 检查是否有冲突的 url 或 configUrl 属性被设置了值（它们应该是 null）
            log.warn("DEBUG CHECK: swaggerUiConfigProperties.getUrl() = {}", swaggerUiConfigProperties.getUrl());
            log.warn("DEBUG CHECK: swaggerUiConfigProperties.getConfigUrl() = {}", swaggerUiConfigProperties.getConfigUrl());
        }
        log.warn("==================== Swagger Debug End ======================");
    }
}