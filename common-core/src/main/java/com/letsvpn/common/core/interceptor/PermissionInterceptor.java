//package com.letsvpn.common.core.interceptor;
//
//import com.letsvpn.common.core.annotation.CheckVip;
//import com.letsvpn.common.core.security.PermissionChecker;
//import com.letsvpn.common.core.util.JwtUtils;
//import com.letsvpn.common.core.response.R;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.method.HandlerMethod;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.PrintWriter;
//
//
//@RequiredArgsConstructor
//public class PermissionInterceptor implements HandlerInterceptor {
//
//    private final PermissionChecker permissionChecker;
//    private final ObjectMapper objectMapper;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//
//        if (!(handler instanceof HandlerMethod)) return true;
//
//        HandlerMethod method = (HandlerMethod) handler;
//        CheckVip annotation = method.getMethodAnnotation(CheckVip.class);
//        if (annotation == null) return true;
//
//        String token = request.getHeader("Authorization");
//        if (token == null || !token.startsWith("Bearer ")) {
//            return reject(response, "缺少或非法Token");
//        }
//
//        String username = JwtUtils.getSubject(token.substring(7));
//        if (username == null) {
//            return reject(response, "无效Token");
//        }
//
//        if (!permissionChecker.hasPermission(username)) {
//            return reject(response, "权限不足或已被封禁");
//        }
//
//        return true;
//    }
//
//    private boolean reject(HttpServletResponse response, String message) throws Exception {
//        response.setContentType("application/json;charset=UTF-8");
//        response.setStatus(403);
//        PrintWriter writer = response.getWriter();
//        writer.write(objectMapper.writeValueAsString(R.fail(message)));
//        writer.flush();
//        return false;
//    }
//}
