// user-service/src/main/java/com/letsvpn/user/controller/TestAuthController.java
package com.letsvpn.user.controller;

import com.letsvpn.common.core.util.AuthContextHolder;
import com.letsvpn.common.core.response.R; // 假设通用返回体
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Hidden // 在Swagger中隐藏，仅用于测试
@RestController
@RequestMapping("/user/v1/test/auth-context")
public class TestAuthController {

    @GetMapping("/current-user-info")
    public R<Map<String, Object>> getCurrentUserInfoFromHeaders() {
        Long userId = AuthContextHolder.getUserId();
        String username = AuthContextHolder.getUsername();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("X-User-ID-read", userId);
        userInfo.put("X-User-Name-read", username);

        if (userId == null || username == null) {
            return R.fail("Failed to read user info from headers via AuthContextHolder");
        }
        return R.success(userInfo);
    }
}