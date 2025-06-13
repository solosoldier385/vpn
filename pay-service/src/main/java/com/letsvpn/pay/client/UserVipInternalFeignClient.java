package com.letsvpn.pay.client;


import com.letsvpn.common.core.dto.ActivateVipSubscriptionRequest;
import com.letsvpn.common.core.dto.ActivateVipSubscriptionResponse;
import com.letsvpn.common.core.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service")
public interface UserVipInternalFeignClient {
    @PostMapping("/user/v1/internal/vip/activate")
    R<ActivateVipSubscriptionResponse> activateVip(@RequestBody ActivateVipSubscriptionRequest request);
} 