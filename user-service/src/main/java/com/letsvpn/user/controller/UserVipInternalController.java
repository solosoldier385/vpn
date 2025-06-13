// 文件路径: user-service/src/main/java/com/letsvpn/user/controller/UserVipInternalController.java
package com.letsvpn.user.controller;

import com.letsvpn.common.core.response.R;
import com.letsvpn.common.core.dto.ActivateVipSubscriptionRequest;
import com.letsvpn.common.core.dto.ActivateVipSubscriptionResponse;
import com.letsvpn.user.entity.SubscriptionPlan; // 如果需要返回这个
import com.letsvpn.user.service.SubscriptionPlanService;
import com.letsvpn.user.service.UserVipService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Hidden // 在Swagger UI中隐藏此内部接口控制器
@Tag(name = "用户VIP管理 (内部)", description = "供其他服务调用的内部接口")
@RestController
@RequestMapping("/user/v1/internal/vip")
public class UserVipInternalController {

    @Autowired
    private UserVipService userVipService;

    @Autowired
    private SubscriptionPlanService subscriptionPlanService;


    @Operation(summary = "激活用户VIP订阅 (供pay-service调用)")
    @PostMapping("/activate")
    public R<ActivateVipSubscriptionResponse> activateSubscription(@Valid @RequestBody ActivateVipSubscriptionRequest request) {
        // 此处应有服务间认证逻辑，例如检查内部调用token或IP白名单
        ActivateVipSubscriptionResponse response = userVipService.activateVipSubscription(request);
        if (response.isSuccess()) {
            return R.success(response);
        } else {
            return R.fail(response.getMessage()); // 或更具体的错误处理
        }
    }

    @Operation(summary = "获取套餐详情 (供pay-service调用)")
    @GetMapping("/plan/{planId}")
    public R<SubscriptionPlan> getPlanDetailsForOrder(@PathVariable Long planId) {
        // 此处应有服务间认证逻辑
        SubscriptionPlan plan = subscriptionPlanService.getPlanDetailsForOrder(planId);
        return R.success(plan);
    }

}