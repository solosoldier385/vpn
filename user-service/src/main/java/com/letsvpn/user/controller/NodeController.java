package com.letsvpn.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.common.core.exception.BizException;
import com.letsvpn.common.core.response.R;
import com.letsvpn.common.core.util.JwtUtils;
import com.letsvpn.common.data.entity.User;
import com.letsvpn.common.data.mapper.UserMapper;
import com.letsvpn.user.dto.NodeRegistrationRequest;
import com.letsvpn.user.dto.NodeUpdateRequest;
import com.letsvpn.user.entity.Node;
import com.letsvpn.user.mapper.NodeMapper;
import com.letsvpn.user.service.NodeService;
import com.letsvpn.user.service.WireGuardConfigService;
import com.letsvpn.user.vo.NodeVO;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "节点信息", description = "获取可用节点信息的接口") // <--- 添加 Controller 分组 Tag
@RestController
@RequestMapping("/user/node")
@RequiredArgsConstructor
@Log4j2
public class NodeController {

    private final NodeMapper nodeMapper;
    private final NodeService nodeService;
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
    @Operation(summary = "获取可用节点列表", description = "获取所有可用的节点列表（包括免费和VIP节点）。此接口无需认证，任何人都可访问。")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = R.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
    })
    public R<List<NodeVO>> listVipNodes() {
        try {
            // 查询所有状态正常的节点
            List<Node> nodes = nodeMapper.selectByMinLevel(0);
            
            // 转换为NodeVO，过滤敏感字段
            List<NodeVO> nodeVOs = nodes.stream()
                    .map(node -> NodeVO.builder()
                            .id(node.getId())
                            .name(node.getName())
                            .levelRequired(node.getLevelRequired())
                            .isFree(node.getIsFree())
                            .status(node.getStatus())
                            .createdAt(node.getCreatedAt())
                            .updatedAt(node.getUpdatedAt())
                            .wgDns(node.getWgDns())
                            .build())
                    .collect(Collectors.toList());
            
            return R.success(nodeVOs);
        } catch (Exception e) {
            log.error("获取节点列表失败", e);
            return R.fail("获取节点列表失败: " + e.getMessage());
        }
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
    @Hidden
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


    @Hidden // <--- 添加 @Hidden 注解
    @PostMapping("/register")
    // Operation 和其他 Swagger 注解可以保留，但 Hidden 会使其不显示
    @Operation(summary = "注册新节点 (脚本调用)", description = "接收来自新部署节点的上报信息，并将其添加到数据库。**需要严格的安全控制！**")
    public R<Node> registerNode( // 返回类型改为 R<Node> 以便返回完整节点信息
                                 @Validated @RequestBody NodeRegistrationRequest request
                                 // TODO: 在这里添加安全校验逻辑! 例如检查 X-API-Key 或调用方 IP 地址
                                 // 例如: @RequestHeader("X-Provisioning-Key") String apiKey
                                 // if (!isValidApiKey(apiKey)) { return R.fail(HttpStatus.UNAUTHORIZED.value(), "无效的 API Key"); }
    ) {
        // 安全性 TODO: 比如验证请求来源的 API Key 或 IP 地址
        try {
            Node newNode = nodeService.createNode(request);
            return R.success(newNode);
        } catch (BizException e) {
            log.warn("注册节点失败: {}", e.getMessage());
            // 根据 BizException 中的具体错误信息或自定义错误码返回不同状态
            return R.fail(e.getMessage()); // 可以考虑更细致的错误码
        } catch (Exception e) {
            log.error("注册节点时发生意外错误", e);
            return R.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(),"节点注册失败，服务器内部错误");
        }
    }

    @Hidden // <--- 添加 @Hidden 注解
    @PutMapping("/{nodeId}") // 路径从 /{nodeId}/port 改为 /{nodeId}
    @Operation(summary = "更新节点信息 (脚本调用)", description = "更新指定节点的信息。**需要严格的安全控制！**")
    public R<Node> updateNode( // 方法名和参数 DTO 更改
                               @Parameter(description = "要更新的节点 ID", required = true) @PathVariable Long nodeId,
                               @Validated @RequestBody NodeUpdateRequest request,
                               // TODO: 在这里添加安全校验逻辑! 例如检查 X-API-Key 或调用方 IP 地址
                               HttpServletRequest httpServletRequest
    ) {
        // 安全性 TODO: 比如验证请求来源的 API Key 或 IP 地址
        String requestIp = httpServletRequest.getRemoteAddr();

        log.info("收到来自IP: {} 对节点ID: {} 的更新请求，数据: {}", requestIp, nodeId, request);

        try {
            Node updatedNode = nodeService.updateNode(nodeId, request, requestIp);
            return R.success(updatedNode);
        } catch (BizException e) {
            log.warn("更新节点 ID {} 失败: {}", nodeId, e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("未找到ID为")) {
                return R.fail(HttpStatus.NOT_FOUND.value(), e.getMessage());
            }
            return R.fail(e.getMessage());
        } catch (Exception e) {
            log.error("更新节点 ID {} 时发生意外错误: {}", nodeId, request, e);
            return R.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(),"更新节点信息失败，服务器内部错误");
        }
    }









}