package com.letsvpn.user.service.impl;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.common.data.entity.UserNode; // 确保这是重命名后的 UserNode
import com.letsvpn.common.data.mapper.UserNodeMapper; // 确保这是重命名后的 UserNodeMapper
import com.letsvpn.user.entity.Node;
import com.letsvpn.user.mapper.NodeMapper;
import com.letsvpn.user.service.WireguardNacosConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct; // 使用 Jakarta EE 9+
// 如果是旧版 Spring (非 Spring Boot 3+), 可能使用 javax.annotation.PostConstruct
import java.util.List;
import java.util.Properties;

@Service
@Slf4j
public class WireguardNacosConfigServiceImpl implements WireguardNacosConfigService {

    private final NodeMapper nodeMapper;
    private final UserNodeMapper userNodeMapper; // 使用重命名后的 UserNodeMapper


    @Value("${spring.cloud.nacos.discovery.server-addr}") // 从配置文件读取Nacos服务器地址
    private String nacosServerAddr;

    @Value("${spring.cloud.nacos.discovery.config.namespace:}") // 从配置文件读取Nacos命名空间，默认为空（public）
    private String nacosNamespace;

    // Nacos 配置相关常量
    private static final String NACOS_WIREGUARD_GROUP = "WIREGUARD_CONFIG"; // Nacos配置分组
    private static final String NACOS_DATA_ID_PREFIX = "wireguard.node.";   // Nacos配置Data ID前缀
    private static final String NACOS_DATA_ID_SUFFIX = ".conf";             // Nacos配置Data ID后缀

    private ConfigService configService;

    public WireguardNacosConfigServiceImpl(NodeMapper nodeMapper, UserNodeMapper userNodeMapper) {
        this.nodeMapper = nodeMapper;
        this.userNodeMapper = userNodeMapper;
    }

    @PostConstruct
    public void init() {
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", nacosServerAddr);
            if (nacosNamespace != null && !nacosNamespace.isEmpty()) {
                properties.put("namespace", nacosNamespace);
            }
            // 如果Nacos Server开启了鉴权，还需要配置用户名密码
            // properties.put("username", "your_nacos_username");
            // properties.put("password", "your_nacos_password");
            this.configService = NacosFactory.createConfigService(properties);
            log.info("Nacos ConfigService initialized successfully. Server: {}, Namespace: {}", nacosServerAddr, nacosNamespace == null || nacosNamespace.isEmpty() ? "public" : nacosNamespace);
        } catch (NacosException e) {
            log.error("Error initializing Nacos ConfigService: {}", e.getMessage(), e);
            // 在这里抛出异常或采取其他错误处理措施，可能会阻止应用启动，这是合理的
            throw new RuntimeException("Failed to initialize Nacos ConfigService", e);
        }
    }

    @Override
    public boolean publishConfigForNode(Long nodeId) throws NacosException {
        Node node = nodeMapper.selectById(nodeId);
        if (node == null) {
            log.error("Cannot publish config to Nacos: Node with ID {} not found.", nodeId);
            return false;
        }
        if (node.getWgPrivateKey() == null || node.getWgPrivateKey().isEmpty()) {
            log.error("Cannot publish config to Nacos for Node ID {}: wgPrivateKey is missing.", nodeId);
            return false;
        }
        if (node.getWgAddress() == null || node.getWgAddress().isEmpty()) {
            log.error("Cannot publish config to Nacos for Node ID {}: wgAddress is missing.", nodeId);
            return false;
        }
        if (node.getPort() == null) {
            log.error("Cannot publish config to Nacos for Node ID {}: port (ListenPort) is missing.", nodeId);
            return false;
        }


        QueryWrapper<UserNode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("node_id", nodeId).eq("is_active", true);
        List<UserNode> activePeers = userNodeMapper.selectList(queryWrapper);

        StringBuilder sb = new StringBuilder();
        // [Interface] section
        sb.append("[Interface]\n");
        sb.append("# Node: ").append(node.getName()).append(" (ID: ").append(nodeId).append(")\n");
        sb.append("Address = ").append(node.getWgAddress()).append("\n");
        sb.append("ListenPort = ").append(node.getPort()).append("\n");
        sb.append("PrivateKey = ").append(node.getWgPrivateKey()).append("\n");
        // TODO: 考虑添加 PostUp 和 PostDown 脚本，这些脚本可以存储在Node实体中，或者作为全局配置
        // 示例 PostUp/PostDown (需要根据实际网络接口和需求调整)
        // sb.append("PostUp = iptables -A FORWARD -i %i -j ACCEPT; iptables -A FORWARD -o %i -j ACCEPT; iptables -t nat -A POSTROUTING -o <EXTERNAL_INTERFACE_NAME> -j MASQUERADE\n");
        // sb.append("PostDown = iptables -D FORWARD -i %i -j ACCEPT; iptables -D FORWARD -o %i -j ACCEPT; iptables -t nat -D POSTROUTING -o <EXTERNAL_INTERFACE_NAME> -j MASQUERADE\n");
        sb.append("\n");

        // [Peer] sections
        if (activePeers != null) {
            for (UserNode peer : activePeers) {
                if (peer.getWgPeerPublicKey() == null || peer.getWgPeerPublicKey().isEmpty() ||
                    peer.getWgAllowedIps() == null || peer.getWgAllowedIps().isEmpty()) {
                    log.warn("Skipping peer for user_id {} on node_id {} due to missing PublicKey or AllowedIPs.", peer.getUserId(), nodeId);
                    continue;
                }
                sb.append("[Peer]\n");
                sb.append("# UserID: ").append(peer.getUserId()).append("\n");
                sb.append("PublicKey = ").append(peer.getWgPeerPublicKey()).append("\n");
                sb.append("AllowedIPs = ").append(peer.getWgAllowedIps()).append("\n");
                // Optional: PresharedKey if used and stored in UserNode entity
                // if (peer.getWgPresharedKey() != null && !peer.getWgPresharedKey().isEmpty()) {
                //    sb.append("PresharedKey = ").append(peer.getWgPresharedKey()).append("\n");
                // }
                sb.append("\n");
            }
        }

        String newConfigContent = sb.toString();
        String dataId = NACOS_DATA_ID_PREFIX + nodeId + NACOS_DATA_ID_SUFFIX;

        boolean success = configService.publishConfig(dataId, NACOS_WIREGUARD_GROUP, newConfigContent);
        if (success) {
            log.info("Successfully published WireGuard config for node ID {} to Nacos (DataID: {}, Group: {}). Content length: {}",
                    nodeId, dataId, NACOS_WIREGUARD_GROUP, newConfigContent.length());
            log.debug("Published config for Node {}:\n{}", nodeId, newConfigContent); // DEBUG级别打印完整配置
        } else {
            log.error("Failed to publish WireGuard config for node ID {} to Nacos (DataID: {}, Group: {}).",
                    nodeId, dataId, NACOS_WIREGUARD_GROUP);
        }
        return success;
    }

    // 可选的删除配置方法
    // @Override
    // public boolean removeConfigForNode(Long nodeId) throws NacosException {
    //     String dataId = NACOS_DATA_ID_PREFIX + nodeId + NACOS_DATA_ID_SUFFIX;
    //     boolean success = configService.removeConfig(dataId, NACOS_WIREGUARD_GROUP);
    //     if (success) {
    //         log.info("Successfully removed WireGuard config for node ID {} from Nacos (DataID: {}, Group: {}).", nodeId, dataId, NACOS_WIREGUARD_GROUP);
    //     } else {
    //         log.error("Failed to remove WireGuard config for node ID {} from Nacos (DataID: {}, Group: {}).", nodeId, dataId, NACOS_WIREGUARD_GROUP);
    //     }
    //     return success;
    // }
}