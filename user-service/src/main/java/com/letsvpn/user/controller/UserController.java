package com.letsvpn.user.controller;

// ... 其他 import ...
import java.security.Principal; // <--- 引入 Principal
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import com.letsvpn.common.core.response.R;
import com.letsvpn.common.core.util.AuthContextHolder;
import com.letsvpn.common.data.entity.User;
import com.letsvpn.common.data.entity.UserNodeConfig;
import com.letsvpn.user.service.UserService;
import com.letsvpn.user.service.WireGuardConfigService;
import com.letsvpn.user.vo.UserInfoVO;
import com.letsvpn.user.vo.UserNodeConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
// import io.swagger.v3.oas.annotations.enums.ParameterIn; // 不再需要 ParameterIn.HEADER for X-User-Name
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement; // <--- 引入 SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// ... 其他 import ...

@Tag(name = "01. 用户信息与节点配置 (User Info & Node Configs)", description = "获取用户信息、节点配置等")
@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private WireGuardConfigService wireGuardConfigService;

    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息",
            description = "根据认证 Token 获取用户的详细信息。需要在请求头中提供有效的 `Authorization: Bearer <token>`。",
            security = @SecurityRequirement(name = "bearerAuth")) // <--- 指定需要 bearerAuth 安全方案
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取用户信息",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = R.class))),
            @ApiResponse(responseCode = "401", description = "未授权或 Token 无效", content = @Content),
            @ApiResponse(responseCode = "500", description = "服务器内部错误或用户不存在", content = @Content)
    })
    // --- 修改方法签名 ---
    public R<UserInfoVO> getUserInfo(
            // --- 恢复使用 @RequestHeader ---
            @Parameter(
                    name = "X-User-Name", // 参数名是 X-User-Name
                    description = "用户名 (由网关自动添加，客户端无需传递此Header)", // 描述清楚来源
                    required = true, // 网关确保传递，所以是必须的
                    in = ParameterIn.HEADER, // 在 Header 中
                    hidden = true // <<< 在 Swagger UI 中隐藏，客户端不应关心这个实现细节
            )
            @RequestHeader("X-User-Name") String username, // <--- 改回 RequestHeader

            @Parameter(hidden = true) // 保持隐藏 HttpServletRequest
            HttpServletRequest request
    ) {
        log.info("====== UserController.getUserInfo START (User from Header: {}) ======", username);
        // 打印 Header 的日志
        java.util.Collections.list(request.getHeaderNames()).forEach(headerName ->
                log.info("Header: {} = {}", headerName, request.getHeader(headerName))
        );

        // ... 后续业务逻辑不变 ...
        User user = userService.findByUsername(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
            return R.fail("用户不存在");
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setUsername(user.getUsername());
        vo.setLevel(user.getLevel());
        vo.setVipExpireTime(user.getVipExpireTime());

        log.info("====== UserController.getUserInfo END ======");
        return R.success(vo);
    }


    @Operation(
            summary = "获取当前用户的节点配置列表",
            description = "查询并返回当前认证用户已分配的所有节点的WireGuard配置信息。需要认证。",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取节点配置列表",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserNodeConfigListWrapper.class))),
                    @ApiResponse(responseCode = "401", description = "用户未认证",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = R.class))),
                    @ApiResponse(responseCode = "204", description = "用户没有节点配置 (No Content)", content = @Content)
            }
    )
    @GetMapping("/node/configs") // 根据实际接口路径调整
    public R<List<UserNodeConfigVO>> getUserNodeConfigs() {
        Long userId = AuthContextHolder.getRequiredUserId(); // 确保获取到userId
        // 假设 WireGuardConfigService 有一个方法可以获取用户的节点配置列表
        // 或者这个逻辑在 UserService 中
        List<UserNodeConfigVO> configs = wireGuardConfigService.getUserNodeConfigsByUserId(userId);
        if (configs == null || configs.isEmpty()) {
            return R.success(null); // 或者 R.ok(Collections.emptyList())
        }
        return R.success(configs);
    }


    @Schema(name = "UserNodeConfigListWrapper", description = "用户节点配置列表响应体包装类")
    private static class UserNodeConfigListWrapper extends R<List<UserNodeConfig>> {}

}