package com.letsvpn.common.core.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {

    private static final String SECRET_KEY = "letsvpn-super-secret-key-1234567890!@#$"; // 可替换为更安全的配置
    public static final long EXPIRATION = 1000 * 60 * 60 * 24; // 1 天（毫秒）

    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    public static final String CLAIM_USER_ID = "user_id"; // 自定义Claim常量

    public static String generateToken(String username, Long userId) { // 增加userId参数
        Map<String, Object> claims = new HashMap<>();
        if (userId != null) {
            claims.put(CLAIM_USER_ID, userId);
        }
        // 你也可以添加其他claims，如roles等

        return Jwts.builder()
                .setSubject(username)
                .addClaims(claims) // 添加自定义claims
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }



    public static Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getSubject(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    public static Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.get(CLAIM_USER_ID, Long.class) : null;
    }


    /**
     * 校验 Token 是否有效
     */
    public static boolean validate(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 获取 Token 过期时间
     */
    public static Date getExpiration(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
        } catch (Exception e) {
            return null;
        }
    }


}
