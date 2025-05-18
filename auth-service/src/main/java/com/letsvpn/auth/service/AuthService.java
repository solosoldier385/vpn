package com.letsvpn.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.auth.dto.LoginRequest;
import com.letsvpn.auth.dto.RegisterRequest;
import com.letsvpn.auth.dto.UserCreationInternalRequest;
import com.letsvpn.auth.entity.User;
import com.letsvpn.auth.feign.UserServiceClient;
import com.letsvpn.auth.mapper.UserMapper;
import com.letsvpn.common.core.exception.BizException;
import com.letsvpn.common.core.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
@Slf4j // Added
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
//    private final JwtUtils jwtUtils;

    private final UserServiceClient userServiceClient; // Added


    public Map<String, Object> login(LoginRequest request) {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", request.getUsername());

        User user = userMapper.selectOne(query);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new RuntimeException("账号已被封禁，请联系管理员");
        }


        String token = JwtUtils.generateToken(user.getUsername(),user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("expiresIn", JwtUtils.getExpiration(token));

        result.put("vipExpireTime", user.getVipExpireTime());
        result.put("level", user.getLevel());
        return result;
    }

    public void register(RegisterRequest request) {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", request.getUsername());

        if (userMapper.selectCount(query) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());


        userMapper.insert(user);



        // After local user creation, call user-service to setup user and assign free nodes
        try {
            UserCreationInternalRequest internalRequest = new UserCreationInternalRequest(user.getId().toString(), user.getUsername());
            com.letsvpn.common.core.response.R<Void> response = userServiceClient.setupNewUser(internalRequest);
            if (response == null || !com.letsvpn.common.core.response.R.isSuccess(response.getCode())) {
                log.error("Failed to setup new user in user-service for userId: {}. Response: {}", user.getId(), response);
                // Optionally, throw a specific exception or handle retry, compensation logic
                throw new BizException("User registration partially failed: Could not setup user profile and nodes. Please contact support.");
            }
            log.info("Successfully called user-service to setup new user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Error calling user-service for new user setup (userId: {}): {}", user.getId(), e.getMessage(), e);
            // This is a critical point. Decide on transactional behavior.
            // If this call fails, should the auth-service user registration be rolled back?
            // @Transactional will handle this if the exception propagates.
            // Consider specific handling for Feign exceptions (e.g., Hystrix fallback if used).
            throw new BizException("User registration failed during profile setup: " + e.getMessage());
        }


    }
}
