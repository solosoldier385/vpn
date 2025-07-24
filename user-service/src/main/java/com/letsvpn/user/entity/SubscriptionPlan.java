// 文件路径: user-service/src/main/java/com/letsvpn/user/entity/SubscriptionPlan.java
package com.letsvpn.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("subscription_plan")
public class SubscriptionPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name; // 例如：周会员, 月会员

    private String type;// 青铜 白金

    private BigDecimal price;

    private Integer durationDays; // 持续天数

    private String description;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT) // 假设自动填充创建时间
    private LocalDateTime createdAt;

    // 新增字段，用于客户端展示 (请确保数据库表结构也已更新)
    private String displayName;         // 显示给用户的名称，如 "月卡"
    private String currency;            // 货币单位, e.g., "CNY"
    private String benefitsDescription; // 套餐权益描述摘要
    private BigDecimal originalPrice;   // 原价（用于促销展示）
    private String tag;                 // 标签，如 "热门推荐"
    private Boolean recommended;        // 是否推荐 (注意：数据库中通常用 tinyint(1) 表示布尔)
    private String pricePerDayInfo;     // 例如 "约￥0.5/天"
    private Integer associatedVipLevelCode; // 购买此套餐后用户将达到的VipLevel code

    private Integer status; // 1: active, 0: inactive/expired

}