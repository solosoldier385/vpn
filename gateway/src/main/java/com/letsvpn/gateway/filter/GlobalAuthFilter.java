package com.letsvpn.gateway.filter;

// 保持 Slf4j, Component, RequiredArgsConstructor, GlobalFilter 等 import
// 引入必要的 Security 类
import com.letsvpn.common.core.util.AuthContextHolder;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder; // 用于排序
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Component // 确保这个注解是激活的
@RequiredArgsConstructor
public class GlobalAuthFilter implements GlobalFilter, Ordered {

    // 不再需要 TokenBlacklistUtil

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 尝试从 SecurityContext 获取 Authentication 对象
        return ReactiveSecurityContextHolder.getContext()
                .filter(context -> context.getAuthentication() != null) // 过滤掉没有认证信息的 context
                .flatMap(context -> {
                    Authentication authentication = context.getAuthentication();




                    // 检查 authentication 是否确实是代表已认证用户（有时可能是匿名用户）
                    // 注意：根据 JwtAuthenticationManager 的实现，成功时 principal 就是 username
                    if (authentication.isAuthenticated() && authentication.getPrincipal() instanceof String) {
                        String username = (String) authentication.getPrincipal(); // 或者用 authentication.getName()
                        Long userId = null;

                        Object details = authentication.getDetails();
                        if (details instanceof Map) {
                            @SuppressWarnings("unchecked") // 我们知道 details 是 Map<String, Object>
                            Map<String, Object> authDetails = (Map<String, Object>) details;
                            Object userIdObj = authDetails.get("userId");
                            if (userIdObj instanceof Long) {
                                userId = (Long) userIdObj;
                            } else if (userIdObj instanceof Integer) { // 以防万一是Integer
                                userId = ((Integer) userIdObj).longValue();
                            } else if (userIdObj != null) {
                                try {
                                    userId = Long.parseLong(String.valueOf(userIdObj));
                                } catch (NumberFormatException e) {
                                    log.warn("GlobalAuthFilter: Failed to parse userId from authentication details: {}", userIdObj, e);
                                }
                            }
                        }


                        if (userId != null) {
                            log.info("GlobalAuthFilter [Order {}]: 用户 '{}' (ID:{}) 已认证. 添加 X-User-Name 和 X-User-ID 请求头.", getOrder(), username, userId);
                            ServerWebExchange mutatedExchange = exchange.mutate()
                                    .request(exchange.getRequest().mutate()
                                            .header(AuthContextHolder.HEADER_USER_NAME, username)
                                            .header(AuthContextHolder.HEADER_USER_ID, String.valueOf(userId)) // 添加 X-User-ID
                                            .build())
                                    .build();
                            return chain.filter(mutatedExchange);
                        } else {
                            log.warn("GlobalAuthFilter [Order {}]: 用户 '{}' 已认证，但未能从Authentication details中获取有效的userId. 仅添加 X-User-Name.", getOrder(), username);
                            // 即使没有userId，也至少添加username
                            ServerWebExchange mutatedExchange = exchange.mutate()
                                    .request(exchange.getRequest().mutate()
                                            .header(AuthContextHolder.HEADER_USER_NAME, username)
                                            .build())
                                    .build();
                            return chain.filter(mutatedExchange);
                        }

                    } else {
                        log.debug("GlobalAuthFilter [Order {}]: SecurityContext 中存在 Authentication，但不是预期的已认证用户 Principal。", getOrder());
                        // 如果不是预期的认证对象，则不修改 request 继续链
                        return chain.filter(exchange);
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // 如果 SecurityContext 为空 (例如对于白名单路径)，则不修改 request 继续链
                    log.debug("GlobalAuthFilter [Order {}]: 未找到 SecurityContext，直接继续过滤器链。", getOrder());
                    return chain.filter(exchange);
                }));
    }

    @Override
    public int getOrder() {
        // 需要在 Spring Security 的认证过滤器 (AUTHENTICATION: -100) 之后运行
        // 但要在路由过滤器 (如 LoadBalancerClientFilter: 10150) 之前运行
        return SecurityWebFiltersOrder.AUTHENTICATION.getOrder() + 5; // 例如 -95，给其他可能的认证后过滤器留点空间
    }
}