package com.letsvpn.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user") // 数据库表名
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String email;

    private LocalDateTime vipExpireTime; // VIP 到期时间
    private Integer status; // 0=正常 1=封禁
    private Integer level;  // 用户等级（1=普通 2=高级 3=管理员等）

}
