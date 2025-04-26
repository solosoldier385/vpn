package com.letsvpn.gateway.config; // 确认包名与你的项目结构一致

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper; // 用于 JSON 序列化
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer; // WebFlux 的数据缓冲
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint; // 使用 WebFlux 的 EntryPoint 接口
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange; // 使用 WebFlux 的 Exchange 对象
import reactor.core.publisher.Mono; // 使用 Reactor 的 Mono

import java.util.HashMap;
import java.util.Map;

@Component // 声明为 Spring Bean，以便可以注入
@RequiredArgsConstructor // 使用 Lombok 自动生成构造函数 (需要 final 字段)
@Slf4j
public class CustomServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper; // 注入 Jackson 的 ObjectMapper 来处理 JSON

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        // 记录认证失败日志
        log.warn("认证失败，触发自定义认证入口点: {}", ex.getMessage());

        // 从 exchange 获取 ServerHttpResponse 对象 (WebFlux 的 Response)
        org.springframework.http.server.reactive.ServerHttpResponse response = exchange.getResponse();

        // 设置响应状态码为 401 Unauthorized
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        // 设置响应头 Content-Type 为 application/json
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 创建你希望返回的 JSON 响应体内容 (使用 Map 或自定义的 R 对象)
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", HttpStatus.UNAUTHORIZED.value()); // 状态码 401
        responseBody.put("msg", "未认证或token无效"); // 错误消息
        responseBody.put("data", null); // 根据你的 R 结构，data 设为 null

        // 将响应体 Map 序列化为 JSON 字节数组
        byte[] responseBytes;
        try {
            responseBytes = objectMapper.writeValueAsBytes(responseBody);
        } catch (JsonProcessingException e) {
            // 如果序列化出错，记录错误并返回一个备用的错误消息
            log.error("序列化自定义401响应体时出错", e);
            responseBytes = "{\"code\":401,\"msg\":\"生成错误响应时发生内部服务器错误\",\"data\":null}".getBytes();
        }

        // 获取响应的 BufferFactory 并创建包含 JSON 字节的 DataBuffer
        DataBufferFactory bufferFactory = response.bufferFactory();
        DataBuffer buffer = bufferFactory.wrap(responseBytes);

        // 将 Buffer 写入响应并返回 Mono<Void> 表示完成
        // response.writeWith 会处理响应的发送
        return response.writeWith(Mono.just(buffer));
    }
}