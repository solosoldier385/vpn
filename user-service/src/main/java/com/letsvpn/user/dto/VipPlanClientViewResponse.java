// 文件路径: user-service/src/main/java/com/letsvpn/user/dto/VipPlanClientViewResponse.java
package com.letsvpn.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "客户端展示的VIP套餐信息")
public class VipPlanClientViewResponse {
    private Long planId;
    private String name; // 内部名称
    private String displayName;
    private BigDecimal price;
    private String currency;
    private Integer durationDays;
    private String durationDescription;
    private String benefitsDescription;
    private BigDecimal originalPrice;
    private String tag;
    private boolean recommended; // 注意: boolean 类型
    private String pricePerDayInfo;
    private Integer associatedVipLevelCode;
    private String type;
}