// 文件路径: user-service/src/main/java/com/letsvpn/user/dto/ActivateVipSubscriptionRequest.java
package com.letsvpn.user.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivateVipSubscriptionRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "套餐ID不能为空")
    private Long planId;

    @NotNull(message = "订单ID不能为空")
    private String orderId;

    @NotNull(message = "支付时间不能为空")
    private LocalDateTime paymentTime;

    @NotNull(message = "套餐时长天数不能为空")
    private Integer planDurationDays;

    @NotNull(message = "目标VIP等级代码不能为空")
    private Integer targetVipLevelCode;
}