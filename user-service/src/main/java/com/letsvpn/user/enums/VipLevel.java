// 文件路径: user-service/src/main/java/com/letsvpn/user/enums/VipLevel.java
package com.letsvpn.user.enums;

import lombok.Getter;

@Getter
public enum VipLevel {
    FREE(0, "免费用户"),
    WEEKLY_VIP(1, "周会员"),
    MONTHLY_VIP(2, "月会员"),
    QUARTERLY_VIP(3, "季会员"),
    YEARLY_VIP(4, "年会员");

    private final int code;
    private final String description;

    VipLevel(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static VipLevel getByCode(int code) {
        for (VipLevel level : VipLevel.values()) {
            if (level.getCode() == code) {
                return level;
            }
        }
        return FREE; // 默认为免费用户
    }
}