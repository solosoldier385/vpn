package com.letsvpn.user.controller;

import com.letsvpn.common.core.response.R;
import com.letsvpn.common.core.util.JwtUtils;
import com.letsvpn.common.data.entity.User;
import com.letsvpn.common.data.mapper.UserMapper;
import com.letsvpn.user.entity.Node;
import com.letsvpn.user.mapper.NodeMapper;
import com.letsvpn.user.service.WireGuardConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Tag(name = "节点信息", description = "获取可用节点信息的接口") // <--- 添加 Controller 分组 Tag
@RestController
@RequestMapping("/user/node")
@RequiredArgsConstructor
@Log4j2
public class NodeController {

    private final NodeMapper nodeMapper;
    private final UserMapper userMapper; // UserMapper 可能也需要注入 (虽然代码里没标 @Autowired 但 @RequiredArgsConstructor 会处理 final)
    private WireGuardConfigService wireGuardConfigService;

    @GetMapping("/free")
    @Operation(summary = "获取免费节点", description = "获取一个当前可用的免费节点信息，此接口无需认证。") // <--- 添加 Operation
    @ApiResponses(value = { // <--- 添加 ApiResponses
            @ApiResponse(responseCode = "200", description = "获取成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = R.class))), // 响应体是 R<Node>
            @ApiResponse(responseCode = "500", description = "服务器内部错误或无可用免费节点", content = @Content)
    })
    public R<Node> freeNode() {
        return R.success(nodeMapper.selectFreeNode());
    }

    // @CheckVip // 这个自定义注解 Swagger 无法直接识别其作用，需要在 description 中说明
    @GetMapping("/list")
    @Operation(summary = "获取可用节点列表", description = "获取当前认证用户可用的所有节点列表（包括免费和VIP节点）。需要有效的 Bearer Token 认证。同时受 @CheckVip 限制（需要用户为VIP）。") // <--- 添加 Operation
    @SecurityRequirement(name = "bearerAuth") // <--- 指定需要 bearerAuth 认证
    @ApiResponses(value = { // <--- 添加 ApiResponses
            @ApiResponse(responseCode = "200", description = "获取成功",
                    content = @Content(mediaType = "application/json",
                            // 描述 R<List<Node>> 结构，data 字段是一个 Node 数组
                            schema = @Schema(implementation = R.class) // 尝试让Springdoc推断 R<List<Node>>
                            // 如果上面推断不清晰，可以用下面更具体的描述(但可能更复杂)：
                            /* schema = @Schema(allOf = R.class, properties = {
                                 @ExtensionProperty(name = "data", value = """
                                  { "type": "array", "items": { "$ref": "#/components/schemas/Node" } }
                                 """)
                            })*/
                            // 或者直接描述 data 部分的内容：
                            /* array = @ArraySchema(schema = @Schema(implementation = Node.class)) */
                    )
            ),
            @ApiResponse(responseCode = "401", description = "未授权或 Token 无效", content = @Content),
            @ApiResponse(responseCode = "500", description = "服务器内部错误或用户不存在", content = @Content)
    })
    public R<List<Node>> listVipNodes(
            @Parameter(description = "认证 Token (需要 'Bearer ' 前缀)", required = true, in = ParameterIn.HEADER, name = "Authorization") // <--- 添加 Parameter 描述
            @RequestHeader("Authorization") String authHeader
            // 注意：这里方法内部是自己解析 Token 获取 username，而不是依赖 Principal
            // 这意味着网关那边可以不用修改 GlobalAuthFilter 来添加 X-User-Name 了
            // 但也意味着 user-service 需要依赖 JwtUtils 和 UserMapper，并且会重复解析 Token
    ) {
        String token = authHeader.replace("Bearer ", "");
        String username = JwtUtils.getSubject(token); // 自己解析 Token
        if (username == null) {
            // 这里应该返回 401 或 400 更合适，但遵循现有逻辑返回 R.fail
            return R.fail("无效Token");
        }

        User user = userMapper.selectByUsername(username); // 查询用户
        if (user == null) {
            return R.fail("用户不存在");
        }

        int level = getVipLevel(user.getVipExpireTime()); // 根据 VIP 时间计算等级
        List<Node> nodes = nodeMapper.selectByMinLevel(level); // 查询节点
        return R.success(nodes);
    }

    // 这个私有方法 Swagger 不会关心
    private int getVipLevel(LocalDateTime expireTime) {
        if (expireTime == null || expireTime.isBefore(LocalDateTime.now())) return 0;
        long days = ChronoUnit.DAYS.between(LocalDateTime.now(), expireTime);
        if (days >= 90) return 3;
        if (days >= 30) return 2;
        if (days >= 7) return 1;
        return 0;
    }



    // --- 新增生成配置文件的接口 ---
    @PostMapping("/{nodeId}/wireguard-config")
    @Operation(summary = "获取指定节点的 WireGuard 配置文件",
            description = "为当前认证用户生成指定节点的 WireGuard 客户端配置文件。需要有效的 Bearer Token 认证。服务器会生成密钥对并将完整配置返回。",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功生成配置文件",
                    // 注意：响应体是纯文本的配置文件内容
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)),
            @ApiResponse(responseCode = "400", description = "请求错误（如节点ID无效、无法分配IP等）", content = @Content),
            @ApiResponse(responseCode = "401", description = "未授权", content = @Content),
            @ApiResponse(responseCode = "403", description = "禁止访问（如用户无权限访问该节点）", content = @Content),
            @ApiResponse(responseCode = "404", description = "节点不存在", content = @Content),
            @ApiResponse(responseCode = "500", description = "服务器内部错误（如密钥生成失败、节点更新失败）", content = @Content)
    })
    public R<String> generateWireGuardConfig(
            @Parameter(description = "要生成配置的节点 ID", required = true) @PathVariable Long nodeId,
            Principal principal // 用于获取用户名
    ) {
        if (principal == null || principal.getName() == null) {
            return R.fail("无法获取认证用户信息"); // 或者返回 401/403
        }
        String username = principal.getName();

        try {
            String configContent = wireGuardConfigService.generateClientConfig(username, nodeId);
            // 注意：这里直接返回 String 可能 R 泛型处理不了，需要调整 R 或 Controller 返回值
            // 方案1: 修改 R<T> 使其能接受 String 或修改返回类型
            // 方案2: 将配置包装在一个对象里返回 R<ConfigObject>
            // 暂时先用 R.success，假设 R 可以处理 String，或者后续调整
            return R.success(configContent);
        } catch (IllegalArgumentException e) {
            // 例如节点不存在、无权限、无法分配 IP 等
            return R.fail("请求错误: " + e.getMessage());
        } catch (RuntimeException e) {
            // 例如密钥生成失败、节点更新失败等
            log.error("生成 WireGuard 配置时出错, user: {}, node: {}", username, nodeId, e);
            return R.fail("生成配置失败: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        int resutl =0;
        int i=10000;
        for(int j=1;j<=i;j++) {
            if(wanshu(j)){
                resutl++;
            }
        }
        System.out.println(resutl);
    }

    public static boolean wanshu(int n){
        int sum = 0;
        for(int i=1;i<n;i++){
            if(n%i==0){
                sum+=i;
            }
        }
        return sum==n;
    }



}