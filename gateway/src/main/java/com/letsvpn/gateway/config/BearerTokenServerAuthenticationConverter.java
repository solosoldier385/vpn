package com.letsvpn.gateway.config; // 确认包名和你的项目结构一致

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 这个类不需要 @Component 注解，因为我们会在 SecurityConfig 中直接 new 它
public class BearerTokenServerAuthenticationConverter implements ServerAuthenticationConverter {

    private static final Logger log = LoggerFactory.getLogger(BearerTokenServerAuthenticationConverter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        // 从请求头获取 Authorization 的值
        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            // 如果 Header 存在且以 "Bearer " 开头
            String token = authorizationHeader.substring(BEARER_PREFIX.length()); // 提取 token 部分
            if (!token.isEmpty()) {
                log.debug("BearerTokenServerAuthenticationConverter: 提取到 Bearer Token (前缀): {}", token.substring(0, Math.min(token.length(), 10))+"...");
                // 创建一个初始的、未认证的 Authentication 对象
                // principal 设置为 null, token 作为 credential 传递给 AuthenticationManager
                return Mono.just(new UsernamePasswordAuthenticationToken(null, token));
            } else {
                log.debug("BearerTokenServerAuthenticationConverter: Authorization Header 中的 Token 为空");
            }
        } else {
            log.debug("BearerTokenServerAuthenticationConverter: 未找到或格式错误的 Bearer Token Header");
        }

        // 如果没有找到有效的 Bearer Token，返回空 Mono
        return Mono.empty();
    }
}