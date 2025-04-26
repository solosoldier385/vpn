package com.letsvpn.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.letsvpn.common.data.entity.User;

public interface UserService extends IService<User> {
    User findByUsername(String username);
}
