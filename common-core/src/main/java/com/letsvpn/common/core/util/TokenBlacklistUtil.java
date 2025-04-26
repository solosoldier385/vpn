package com.letsvpn.common.core.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TokenBlacklistUtil {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "blacklist:";

    public void blacklist(String token, long expireMillis) {
        redisTemplate.opsForValue().set(  PREFIX + token, "1", expireMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }
}
