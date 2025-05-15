// 文件路径: user-service/src/main/java/com/letsvpn/user/service/UserVipService.java
package com.letsvpn.user.service;

import com.letsvpn.user.dto.ActivateVipSubscriptionRequest;
import com.letsvpn.user.dto.ActivateVipSubscriptionResponse;
import com.letsvpn.user.dto.CurrentUserVipProfileResponse;

public interface UserVipService {

    /**
     * 获取当前认证用户的VIP信息
     * @param userId 当前认证的用户ID
     * @return 用户VIP概览信息
     */
    CurrentUserVipProfileResponse getCurrentUserVipProfile(Long userId);

    /**
     * 激活或更新用户的VIP订阅 (由pay-service在支付成功后调用)
     * @param request 包含用户ID, 订单ID, 套餐信息等
     * @return 激活结果
     */
    ActivateVipSubscriptionResponse activateVipSubscription(ActivateVipSubscriptionRequest request);

    /**
     * 检查并处理已过期的VIP (定时任务调用)
     */
    void checkAndProcessExpiredVips();
}