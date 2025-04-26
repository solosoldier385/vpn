package com.letsvpn.gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange; // 注意这里的参数类型
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler; // 对应的 Handler 接口
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component // 声明为 Spring Bean
@RequiredArgsConstructor
@Slf4j
public class CustomServerAuthenticationFailureHandler implements ServerAuthenticationFailureHandler {

    private final ObjectMapper objectMapper; // 注入 ObjectMapper

    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException exception) {
        // 这个方法会在 AuthenticationWebFilter 认证失败时被调用
        log.warn("Authentication failed via AuthenticationWebFilter, triggering CustomServerAuthenticationFailureHandler: {}", exception.getMessage());

        // 获取 Response 对象
        org.springframework.http.server.reactive.ServerHttpResponse response = webFilterExchange.getExchange().getResponse();

        // 设置状态码和响应头
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 创建 JSON 响应体
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", HttpStatus.UNAUTHORIZED.value());
        // 可以使用固定的消息，或者从 exception.getMessage() 获取更具体的信息（如果需要）
        responseBody.put("msg", "未认证或token无效");
        responseBody.put("data", null);

        // 序列化并写入响应
        byte[] responseBytes;
        try {
            responseBytes = objectMapper.writeValueAsBytes(responseBody);
        } catch (JsonProcessingException e) {
            log.error("Error serializing custom 401 failure response body", e);
            responseBytes = "{\"code\":401,\"msg\":\"Internal Server Error during error response generation\",\"data\":null}".getBytes();
        }

        DataBufferFactory bufferFactory = response.bufferFactory();
        DataBuffer buffer = bufferFactory.wrap(responseBytes);

        // 返回写入操作的 Mono
        return response.writeWith(Mono.just(buffer));
    }
}