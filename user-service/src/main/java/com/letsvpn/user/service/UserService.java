package com.letsvpn.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.letsvpn.common.data.entity.User;

public interface UserService extends IService<User> {
    User findByUsername(String username);
    void initializeNewUser(String userId, String username);
    /**
     * Initializes a new user by creating their profile in user-service,
     * setting default VIP status (free tier), and assigning free nodes.
     * This is typically called internally after user registration in auth-service.
     *
     * @param userId The unique ID of the user (typically from auth-service).
     * @param username The username.
     */
}
