package com.letsvpn.common.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.letsvpn.common.data.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    User selectByUsername(String username);
    // 如果后期需要写自定义 SQL，可以在这里加注解方法
}
