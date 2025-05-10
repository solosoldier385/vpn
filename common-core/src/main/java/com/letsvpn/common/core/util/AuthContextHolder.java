// 文件路径: common-core/src/main/java/com/letsvpn/common/core/util/AuthContextHolder.java
package com.letsvpn.common.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class AuthContextHolder {

    private static final Logger log = LoggerFactory.getLogger(AuthContextHolder.class);

    // 定义网关向下游服务传递用户信息的请求头常量
    public static final String HEADER_USER_ID = "X-User-ID";
    public static final String HEADER_USER_NAME = "X-User-Name";
    // 你还可以定义其他需要的头，例如 X-User-Roles 等

    /**
     * 获取当前的 HttpServletRequest 对象。
     * @return HttpServletRequest 如果在Servlet请求上下文中，否则返回null。
     */
    private static HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        // 如果不是在标准的HTTP Servlet请求上下文中（例如，在异步线程或非Web环境中），则可能为null
        log.trace("Not in a Servlet request context, cannot retrieve HttpServletRequest.");
        return null;
    }

    /**
     * 从请求头中获取当前用户的 UserID。
     * 此请求头应由 API 网关在验证 JWT 后设置并向下游服务传递。
     *
     * @return 用户ID (Long)，如果请求头不存在、为空或无效则返回 null。
     */
    public static Long getUserId() {
        HttpServletRequest request = getCurrentHttpRequest();
        if (request == null) {
            log.warn("HttpServletRequest is null. Cannot get UserId from header. This might happen in non-request threads or if RequestContextHolder is not populated.");
            return null;
        }

        String userIdStr = request.getHeader(HEADER_USER_ID);
        if (userIdStr == null || userIdStr.isEmpty()) {
            log.warn("Header '{}' not found or is empty in the current request.", HEADER_USER_ID);
            return null;
        }

        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            log.error("Failed to parse UserId from header '{}': value='{}'. Invalid format.", HEADER_USER_ID, userIdStr, e);
            return null;
        }
    }

    /**
     * 从请求头中获取当前用户的 Username。
     * 此请求头应由 API 网关在验证 JWT 后设置并向下游服务传递。
     *
     * @return 用户名 (String)，如果请求头不存在或为空则返回 null。
     */
    public static String getUsername() {
        HttpServletRequest request = getCurrentHttpRequest();
        if (request == null) {
            log.warn("HttpServletRequest is null. Cannot get Username from header.");
            return null;
        }

        String username = request.getHeader(HEADER_USER_NAME);
        if (username == null || username.isEmpty()) {
            log.warn("Header '{}' not found or is empty in the current request.", HEADER_USER_NAME);
            return null; // 明确返回null
        }
        return username;
    }

    /**
     * 获取当前用户ID，如果无法从请求头中获取（例如未认证、请求头缺失或格式错误），
     * 则抛出 {@link IllegalStateException}。
     * 用于业务逻辑中明确需要用户ID且认为此时用户必须已认证的场景。
     *
     * @return 用户ID (Long)
     * @throws IllegalStateException 如果用户ID无法获取。
     */
    public static Long getRequiredUserId() {
        Long userId = getUserId();
        if (userId == null) {
            // 在生产环境中，这里通常会有一个全局异常处理器来捕获此类异常并返回合适的错误响应给客户端
            throw new IllegalStateException("Required UserID not found in request headers ('" + HEADER_USER_ID + "'). Ensure the API Gateway is correctly forwarding user identity or the request is authenticated.");
        }
        return userId;
    }

    /**
     * 获取当前用户名，如果无法从请求头中获取，则抛出 {@link IllegalStateException}。
     *
     * @return 用户名 (String)
     * @throws IllegalStateException 如果用户名无法获取。
     */
    public static String getRequiredUsername() {
        String username = getUsername();
        if (username == null) {
            throw new IllegalStateException("Required Username not found in request headers ('" + HEADER_USER_NAME + "'). Ensure the API Gateway is correctly forwarding user identity or the request is authenticated.");
        }
        return username;
    }
}