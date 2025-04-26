// auth-service/src/main/java/com/letsvpn/auth/controller/AuthController.java
package com.letsvpn.auth.controller;

import com.letsvpn.auth.dto.LoginRequest; // 引入 DTO
import com.letsvpn.auth.dto.RegisterRequest; // 引入 DTO
import com.letsvpn.auth.service.AuthService;
import com.letsvpn.common.core.response.R;
import io.swagger.v3.oas.annotations.Operation; // 引入 Operation
import io.swagger.v3.oas.annotations.media.Content; // 引入 Content
import io.swagger.v3.oas.annotations.media.ExampleObject; // 引入 ExampleObject (可选)
import io.swagger.v3.oas.annotations.media.Schema; // 引入 Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody; // 注意: 请求体用这个注解描述
import io.swagger.v3.oas.annotations.responses.ApiResponse; // 引入 ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses; // 引入 ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag; // 引入 Tag
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody; // Spring 的 @RequestBody 保留
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "认证授权", description = "用户注册与登录接口") // <--- 添加 Controller 分组 Tag
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "通过用户名和密码获取认证 Token 及用户信息") // <--- 添加 Operation
    @RequestBody(description = "用户登录凭证", required = true, // <--- 使用 @io.swagger.v3.oas.annotations.parameters.RequestBody 描述请求体
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LoginRequest.class))) // 关联 LoginRequest DTO
    @ApiResponses(value = { // <--- 添加 ApiResponses
            @ApiResponse(responseCode = "200", description = "登录成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = R.class), // 响应体是 R<Map<String, Object>>
                            examples = @ExampleObject(value = """
                                    {
                                      "code": 200,
                                      "msg": "success",
                                      "data": {
                                        "token": "eyJhbGciOiJIUzI1NiJ9...",
                                        "expiresIn": "2025-04-19T08:00:00Z",
                                        "vipExpireTime": "2026-04-18T10:00:00Z",
                                        "level": 1
                                      }
                                    }
                                    """) // 提供一个响应示例，说明 Map 内容
                    )),
            @ApiResponse(responseCode = "500", description = "登录失败 (用户名或密码错误 / 账号被封禁)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = R.class),
                            examples = @ExampleObject(value = """
                                        { "code": 500, "msg": "用户名或密码错误", "data": null }
                                        """))) // 失败响应示例
    })
    public R<Map<String, Object>> login(
            @org.springframework.web.bind.annotation.RequestBody LoginRequest request // <--- Spring 的 @RequestBody 保持不变
    ) {
        try {
            Map<String, Object> data = authService.login(request);
            return R.success(data);
        } catch (RuntimeException e) {
            // 这里的 RuntimeException 会被 GlobalExceptionHandler 捕获（如果配置了）
            // 或者直接返回 R.fail
            return R.fail(e.getMessage());
        }
    }


    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户账号") // <--- 添加 Operation
    @RequestBody(description = "用户注册信息", required = true, // <--- 描述请求体
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = RegisterRequest.class))) // 关联 RegisterRequest DTO
    @ApiResponses(value = { // <--- 添加 ApiResponses
            @ApiResponse(responseCode = "200", description = "注册成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = R.class),
                            examples = @ExampleObject(value = """
                                    { "code": 200, "msg": "success", "data": null }
                                    """) // 成功响应示例 (data 为 null)
                    )),
            @ApiResponse(responseCode = "500", description = "注册失败 (例如：用户名已存在)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = R.class),
                            examples = @ExampleObject(value = """
                                        { "code": 500, "msg": "用户名已存在", "data": null }
                                        """))) // 失败响应示例
    })
    public R<Void> register(
            @org.springframework.web.bind.annotation.RequestBody RegisterRequest request // <--- Spring 的 @RequestBody 保持不变
    ) {
        try {
            authService.register(request);
            // 对于 Void 类型，成功时 data 通常是 null
            return R.success(null);
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }
}