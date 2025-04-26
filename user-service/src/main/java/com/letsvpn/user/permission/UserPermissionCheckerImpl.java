package com.letsvpn.user.permission;

import com.letsvpn.common.core.security.PermissionChecker;
import com.letsvpn.common.data.entity.User;
import com.letsvpn.common.data.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UserPermissionCheckerImpl implements PermissionChecker {
    @Autowired
    private final UserMapper userMapper;

    @Override
    public boolean hasPermission(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) return false;
        if (user.getStatus() != 0) return false;
        if (user.getVipExpireTime() == null || user.getVipExpireTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        return true;
    }
}
