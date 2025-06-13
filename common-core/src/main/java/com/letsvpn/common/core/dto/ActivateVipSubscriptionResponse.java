// 文件路径: user-service/src/main/java/com/letsvpn/user/dto/ActivateVipSubscriptionResponse.java
package com.letsvpn.common.core.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ActivateVipSubscriptionResponse {
    private boolean success;
    private String message;
    private Long userId;
    private Integer updatedVipLevelCode;
    private LocalDateTime newVipExpireTime;
}