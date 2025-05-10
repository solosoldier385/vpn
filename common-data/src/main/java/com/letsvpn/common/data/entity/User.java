// 文件路径: common-data/src/main/java/com/letsvpn/common/data/entity/User.java
package com.letsvpn.common.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode; // 确保 equals 和 hashCode 也被正确生成

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false) // 酌情添加
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO) // 明确指定主键和自增类型
    private Long id;

    private String username;
    private String password; // 假设存在
    private String email;    // 假设存在

    private Integer level; // VIP等级代码 (例如 0:免费, 1:周, 2:月, 3:季, 4:年)
    private LocalDateTime vipExpireTime;

    private Integer status; // 0: inactive, 1: active, 2: banned

    private LocalDateTime createTime; // 假设存在
    private LocalDateTime updateTime; // 假设存在
}