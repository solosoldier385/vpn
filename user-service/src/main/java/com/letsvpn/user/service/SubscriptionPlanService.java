// 文件路径: user-service/src/main/java/com/letsvpn/user/service/SubscriptionPlanService.java
package com.letsvpn.user.service;

import com.letsvpn.user.dto.VipPlanClientViewResponse;
import com.letsvpn.user.entity.SubscriptionPlan; // 如果内部接口需要返回原始实体

import java.util.List;

public interface SubscriptionPlanService {

    /**
     * 获取所有可供客户端展示的有效VIP套餐列表
     * @return 套餐列表
     */
    List<VipPlanClientViewResponse> getActiveDisplayPlans();

    /**
     * (内部接口，供order-service调用) 根据套餐ID获取套餐详情
     * order-service 在创建订单时，可能需要此接口来获取价格、时长、对应VIP等级等信息以确保一致性。
     * @param planId 套餐ID
     * @return 套餐实体，或一个包含必要信息的内部DTO
     */
    SubscriptionPlan getPlanDetailsForOrder(Long planId); // 或返回一个特定的内部DTO
}