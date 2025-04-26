package com.letsvpn.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfoVO {
    private String username;
    private Integer level;
    private LocalDateTime vipExpireTime;
}
