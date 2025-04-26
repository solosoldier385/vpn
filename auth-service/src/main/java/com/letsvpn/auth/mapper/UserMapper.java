package com.letsvpn.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.letsvpn.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
