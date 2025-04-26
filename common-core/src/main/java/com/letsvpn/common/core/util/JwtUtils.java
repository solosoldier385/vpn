package com.letsvpn.common.core.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtils {

    private static final String SECRET_KEY = "letsvpn-super-secret-key-1234567890!@#$"; // 可替换为更安全的配置
    public static final long EXPIRATION = 1000 * 60 * 60 * 24; // 1 天（毫秒）

    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    /**
     * 生成 JWT Token
     */
    public static String generateToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }



    /**
     * 解析 Token 并返回 subject（用户 ID/用户名）
     */
    public static String getSubject(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
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
