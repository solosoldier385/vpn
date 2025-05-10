// 文件路径: user-service/src/main/java/com/letsvpn/user/mapper/UserSubscriptionMapper.java
package com.letsvpn.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.letsvpn.user.entity.UserSubscription;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserSubscriptionMapper extends BaseMapper<UserSubscription> {
}