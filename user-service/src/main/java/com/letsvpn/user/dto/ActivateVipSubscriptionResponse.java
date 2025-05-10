// 文件路径: user-service/src/main/java/com/letsvpn/user/dto/ActivateVipSubscriptionResponse.java
package com.letsvpn.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "当前用户VIP档案信息")
public class ActivateVipSubscriptionResponse {
    private boolean success;
    private String message;
    private Long userId;
    private Integer updatedVipLevelCode;
    private LocalDateTime newVipExpireTime;
}