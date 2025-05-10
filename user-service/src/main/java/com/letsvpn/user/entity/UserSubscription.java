// 文件路径: user-service/src/main/java/com/letsvpn/user/entity/UserSubscription.java
package com.letsvpn.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_subscription")
public class UserSubscription {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long planId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status; // 1: active, 0: inactive/expired
}