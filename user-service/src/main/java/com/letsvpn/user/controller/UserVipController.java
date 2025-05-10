// 文件路径: user-service/src/main/java/com/letsvpn/user/controller/UserVipController.java
package com.letsvpn.user.controller;

import com.letsvpn.common.core.response.R;
import com.letsvpn.common.core.util.AuthContextHolder;
import com.letsvpn.user.dto.CurrentUserVipProfileResponse;
import com.letsvpn.user.service.UserVipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "02. 用户VIP信息 (User VIP)", description = "查询用户当前VIP状态等信息")
@RestController
@RequestMapping("/user/v1/vip")
public class UserVipController {

    @Autowired
    private UserVipService userVipService;

    // 假设你有一个工具类 AuthContextHolder 来从JWT或安全上下文中获取当前用户ID
    // 如果没有，你需要通过 @RequestHeader("Authorization") 解析JWT，或依赖Spring Security的AuthenticationPrincipal
    // @Autowired
    // private AuthContextHolder authContextHolder;


    @Operation(
            summary = "获取当前登录用户的VIP信息",
            description = "查询并返回当前已认证用户的VIP等级、到期时间、剩余天数等信息。需要有效的JWT Token进行认证，并通过网关在请求头中传递X-User-ID。",
            security = @SecurityRequirement(name = "bearerAuth"), // 引用在SwaggerConfig中定义的SecurityScheme
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取VIP信息",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CurrentUserVipProfileResponseWrapper.class))), // 使用包装类以便正确显示 R<T>
                    @ApiResponse(responseCode = "401", description = "用户未认证或Token无效/请求头缺失用户信息",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = R.class))),
                    @ApiResponse(responseCode = "404", description = "用户不存在",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = R.class)))
            }
    )
    @GetMapping("/profile")
    public R<CurrentUserVipProfileResponse> getCurrentUserProfile() {
        // Long currentUserId = authContextHolder.getCurrentUserId(); // 从token获取用户ID
        // 示例：先写死一个用户ID，你需要替换为实际从token获取的逻辑
        Long currentUserId = AuthContextHolder.getUserId(); // 假设这个方法能拿到用户ID
        if (currentUserId == null) {
            return R.fail("用户未认证");
        }
        CurrentUserVipProfileResponse profile = userVipService.getCurrentUserVipProfile(currentUserId);
        return R.success(profile);
    }

    // 辅助包装类，用于Swagger正确解析泛型 R<CurrentUserVipProfileResponse>
    @Schema(name = "CurrentUserVipProfileResponseWrapper", description = "获取当前用户VIP信息响应体包装类")
    private static class CurrentUserVipProfileResponseWrapper extends R<CurrentUserVipProfileResponse> {}

}