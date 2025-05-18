package com.letsvpn.auth.feign;

import com.letsvpn.auth.dto.UserCreationInternalRequest;
import com.letsvpn.common.core.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", path = "/api/user") // Assuming user-service is registered with this name in Nacos
public interface UserServiceClient {

    @PostMapping("/internal/create")
    R<Void> setupNewUser(@RequestBody UserCreationInternalRequest request);
}