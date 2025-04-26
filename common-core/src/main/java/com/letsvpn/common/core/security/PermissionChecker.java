package com.letsvpn.common.core.security;

import org.springframework.stereotype.Component;

public interface PermissionChecker {

    /**
     * 检查该用户是否有访问权限（如 VIP、生效状态等）
     * @param username 用户名（从 JWT 中提取）
     * @return true = 放行，false = 禁止访问
     */
    boolean hasPermission(String username);
}
