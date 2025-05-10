// 文件路径: user-service/src/main/java/com/letsvpn/user/controller/SubscriptionPlanController.java
package com.letsvpn.user.controller;

import com.letsvpn.common.core.response.R;
import com.letsvpn.user.dto.VipPlanClientViewResponse;
import com.letsvpn.user.service.SubscriptionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "03. VIP订阅套餐 (Subscription Plans)", description = "查询可购买的VIP套餐列表")
@RestController
@RequestMapping("/user/v1/vip")
public class SubscriptionPlanController {

    @Autowired
    private SubscriptionPlanService subscriptionPlanService;

    @Operation(
            summary = "获取可购买的VIP套餐列表",
            description = "返回所有当前上架的可供用户选择购买的VIP套餐详细信息。此接口通常不需要认证。",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取套餐列表",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = VipPlanListResponseWrapper.class)) // 使用包装类
                    ),
                    @ApiResponse(responseCode = "204", description = "没有可用的套餐 (No Content)", content = @Content)
            }
    )
    @GetMapping("/plans")
    public R<List<VipPlanClientViewResponse>> getAvailableSubscriptionPlans() {
        List<VipPlanClientViewResponse> plans = subscriptionPlanService.getActiveDisplayPlans();
        return R.success(plans);
    }

    // 辅助包装类，用于Swagger正确解析泛型 R<List<VipPlanClientViewResponse>>
    @Schema(name = "VipPlanListResponseWrapper", description = "VIP套餐列表响应体包装类")
    private static class VipPlanListResponseWrapper extends R<List<VipPlanClientViewResponse>> {}

}