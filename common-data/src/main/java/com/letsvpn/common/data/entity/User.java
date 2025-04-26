package com.letsvpn.common.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    private Long id;
    private String username;
    private Integer level;
    private LocalDateTime vipExpireTime;
    private Integer status;
}
