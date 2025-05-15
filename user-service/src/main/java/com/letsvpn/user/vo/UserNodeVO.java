// 文件路径: user-service/src/main/java/com/letsvpn/user/vo/UserNodeConfigVO.java
package com.letsvpn.user.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // 序列化时忽略null字段，使响应更简洁
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户节点配置视图对象 (用于客户端展示)")
public class UserNodeVO {

    @Schema(description = "配置ID (user_node_config.id)", example = "1")
    private Long configId; // 从 UserNodeConfig.id 获取

    @Schema(description = "用户ID", example = "101")
    private Long userId;

    @Schema(description = "节点ID", example = "201")
    private Long nodeId;

    // --- 从 Node 实体获取的信息 ---
    @Schema(description = "节点名称", example = "香港CN2节点-01")
    private String name;

    @Schema(description = "节点显示名称/位置 (例如国家/地区旗帜 + 地名)", example = "🇭🇰 香港")
    private String nodeLocation; // 你可以组合 Node.countryCode (转emoji) 和 Node.locationName

    @Schema(description = "节点IP或域名 (服务端连接地址)", example = "hk.example.com")
    private String ip; // Node.ip 或 Node.host

    @Schema(description = "节点端口 (服务端连接端口)", example = "49572")
    private Integer port; // Node.port

    @Schema(description = "节点要求的最低VIP等级代码 (0=免费)", example = "0")
    private Integer nodeLevelRequired;

    @Schema(description = "节点是否为免费节点", example = "true")
    private Boolean isFree;


//    @Schema(description = "服务端为用户生成的WireGuard Peer公钥 (客户端的公钥)", example = "/ZLbnLMsHW5Q0iwfaM0JSDxMhltAlVfDzsluXfQ3XC8=")
//    private String wgPeerPublicKey; // UserNodeConfig.wgPeerPublicKey

    @Schema(description = "服务端的WireGuard Peer公钥 (服务端的公钥)", example = "/ZLbnLMsHW5Q0iwfaM0JSDxMhltAlVfDzsluXfQ3XC8=")
    private String wgPublicKey; // UserNodeConfig.wgPeerPublicKey

    @Schema(description = "服务端为用户生成的WireGuard Peer私钥 (客户端的私钥)", example = "/ZLbnLMsHW5Q0iwfaM0JSDxMhltAlVfDzsluXfQ3XC8=")
    private String wgPrivateKey; // UserNodeConfig.wgPeerPublicKey

    @Schema(description = "分配给用户的VPN内网IP (CIDR格式)", example = "10.0.0.3/32")
    private String wgAddress;    // UserNodeConfig.wgAllowedIps

    @Schema(description = "该配置是否激活 (1=激活, 0=禁用)", example = "true")
    private Boolean isActive;         // UserNodeConfig.isActive

    @Schema(description = "创建时间 (ISO 8601格式)", example = "2025-05-10T10:00:00", type = "string", format = "date-time")
    private LocalDateTime createdAt;  // UserNodeConfig.createdAt

    // 你可以根据需要添加其他字段，例如节点延迟、负载等（如果这些信息可获取）
    // @Schema(description = "节点当前延迟(ms)", example = "50", nullable = true)
    // private Integer nodeLatency;

    @Schema(description = "dns", example = "10.0.0.1")
    private String wgDns;    // UserNodeConfig.wgAllowedIps

}