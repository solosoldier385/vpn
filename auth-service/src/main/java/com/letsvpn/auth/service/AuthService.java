package com.letsvpn.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.auth.dto.LoginRequest;
import com.letsvpn.auth.dto.RegisterRequest;
import com.letsvpn.auth.entity.User;
import com.letsvpn.auth.mapper.UserMapper;
import com.letsvpn.common.core.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

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


        String token = JwtUtils.generateToken(user.getUsername());
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
    }
}
