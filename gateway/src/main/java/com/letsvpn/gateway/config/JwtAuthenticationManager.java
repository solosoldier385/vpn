// 你可以创建一个新文件，例如 JwtAuthenticationManager.java
package com.letsvpn.gateway.config; // 或者其他合适的包

import com.letsvpn.common.core.util.JwtUtils;
import com.letsvpn.common.core.util.TokenBlacklistUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final TokenBlacklistUtil blacklistUtil;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();
        String username;

        // 1. 基础校验 (由 JwtUtils.validate 处理签名和过期)
        if (!JwtUtils.validate(token)) {
            // 注意：如果 validate 内部吞掉了异常只返回 false，这里可能需要更详细的判断
            // 可以考虑让 validate 抛出异常，在这里捕获
            return Mono.error(new BadCredentialsException("无效的 Token (签名或已过期)"));
        }

        // 2. 检查黑名单
        if (blacklistUtil.isBlacklisted(token)) {
            return Mono.error(new BadCredentialsException("Token 已在黑名单中 (已登出或失效)"));
        }

        // 3. 获取用户名
        username = JwtUtils.getSubject(token);
        Long userId = JwtUtils.getUserIdFromToken(token); // 从token中获取userId




        if (username == null || userId == null) { // 同时检查userId
            return Mono.error(new BadCredentialsException("无法从 Token 中解析完整的用户信息 (username/userId)"));
        }

        // 这里我们选择将 userId 放入 details，principal 仍然是 username。
        Map<String, Object> details = new HashMap<>();
        details.put("userId", userId);


        UsernamePasswordAuthenticationToken authenticatedToken = new UsernamePasswordAuthenticationToken(
                username,
                token,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        authenticatedToken.setDetails(details); // 将包含userId的map设置为details


        // 4. 认证成功, 创建包含用户信息的 Authentication 对象
        // 这里的权限可以根据你的业务逻辑来设置，例如从 Token 的 claims 中读取
        return Mono.just(authenticatedToken);
    }
}