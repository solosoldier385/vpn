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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct; // 使用 Jakarta EE 9+
// 如果是旧版 Spring (非 Spring Boot 3+), 可能使用 javax.annotation.PostConstruct
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

@Service
@Slf4j
public class WireguardNacosConfigServiceImpl implements WireguardNacosConfigService {

    private final NodeMapper nodeMapper;
    private final UserNodeMapper userNodeMapper; // 使用重命名后的 UserNodeMapper


    //@Value("${spring.cloud.nacos.discovery.server-addr}") // 从配置文件读取Nacos服务器地址
//    @Value("${spring.cloud.nacos.config.server-addr}")
    @Value("${spring.cloud.nacos.config.server-addr:127.0.0.1:8848}")

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

    public boolean publishConfigForNode(Long nodeId) { // 移除非NacosException的声明，内部处理或转为运行时异常
        Node node = nodeMapper.selectById(nodeId);
        if (node == null) {
            log.error("无法发布配置到 Nacos: 未找到 ID 为 {} 的节点.", nodeId);
            return false;
        }
        if (node.getWgPrivateKey() == null || node.getWgPrivateKey().isEmpty()) {
            log.error("无法为节点 ID {} 发布配置到 Nacos: wgPrivateKey 缺失.", nodeId);
            return false;
        }
        // wgAddress 字段对于IPAM仍然重要，但不再直接写入 [Interface] 的 Address 行
        if (node.getWgAddress() == null || node.getWgAddress().isEmpty()) {
            log.warn("节点 ID {} 的 wgAddress 缺失. 虽然不直接写入wg0.conf的Address行, 但IPAM可能依赖此信息.", nodeId);
            // 根据您的业务逻辑，这里可能是错误，也可能只是警告
        }
        if (node.getPort() == null) { // Port 对应 ListenPort
            log.error("无法为节点 ID {} 发布配置到 Nacos: port (ListenPort) 缺失.", nodeId);
            return false;
        }

        QueryWrapper<UserNode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("node_id", nodeId).eq("is_active", true);
        List<UserNode> activePeers = userNodeMapper.selectList(queryWrapper);

        StringBuilder sb = new StringBuilder();
        // [Interface] 部分 - 仅包含 wg syncconf 直接支持的参数
        sb.append("[Interface]\n");
        sb.append("# Node: ").append(node.getName()).append(" (ID: ").append(nodeId).append(")\n");
        sb.append("# 注意: 此配置文件主要用于 wg syncconf 更新 Peers。接口IP地址 (Address) 和路由需通过其他方式管理。\n");
        sb.append("PrivateKey = ").append(node.getWgPrivateKey()).append("\n");
        sb.append("ListenPort = ").append(node.getPort()).append("\n");
        // 配置文件中不再包含 Address = ...
        // 也不包含 PostUp, PostDown, DNS, MTU 等 wg-quick 特定指令
        sb.append("\n");

        // [Peer] 部分
        if (activePeers != null && !activePeers.isEmpty()) {
            // 对 Peers 进行排序，以确保配置文件内容的顺序稳定性
            // 您可以根据 PublicKey 或 UserID 进行排序，这里以 UserID 为例
            activePeers.sort(Comparator.comparing(UserNode::getUserId, Comparator.nullsLast(Comparator.naturalOrder())));

            for (UserNode peer : activePeers) {
                if (peer.getWgPeerPublicKey() == null || peer.getWgPeerPublicKey().isEmpty() ||
                        peer.getWgAllowedIps() == null || peer.getWgAllowedIps().isEmpty()) {
                    log.warn("跳过节点 ID {} 上的用户 ID {} 的 Peer，原因：缺少 PublicKey 或 AllowedIPs.", nodeId, peer.getUserId());
                    continue;
                }
                sb.append("[Peer]\n");
                sb.append("# UserID: ").append(peer.getUserId()).append("\n");
                sb.append("PublicKey = ").append(peer.getWgPeerPublicKey()).append("\n");
                sb.append("AllowedIPs = ").append(peer.getWgAllowedIps()).append("\n"); // 例如 "10.0.0.2/32"

                // 可选：PresharedKey (如果您的 UserNode 实体中有此字段)
                // if (peer.getWgPresharedKey() != null && !peer.getWgPresharedKey().isEmpty()) {
                //    sb.append("PresharedKey = ").append(peer.getWgPresharedKey()).append("\n");
                // }
                // 可选：PersistentKeepalive (如果您的 UserNode 实体中有此字段)
                // if (peer.getPersistentKeepalive() != null && peer.getPersistentKeepalive() > 0) {
                //    sb.append("PersistentKeepalive = ").append(peer.getPersistentKeepalive()).append("\n");
                // }
                sb.append("\n");
            }
        }

        String newConfigContent = sb.toString();
        String dataId = NACOS_DATA_ID_PREFIX + nodeId + NACOS_DATA_ID_SUFFIX;
        boolean success = false;
        try {
            // 假设 Nacos 的 namespace ID 是从其他地方配置或获取的，如果不是 public
            // String namespaceId = nacosProperties.getNamespace(); // 示例
            success = configService.publishConfig(dataId, NACOS_WIREGUARD_GROUP, newConfigContent);
            // 对于Nacos V2, publishConfig(dataId, group, content, type) 更常用，type可以是 "properties", "yaml", "text" 等
            // success = configService.publishConfig(dataId, NACOS_WIREGUARD_GROUP, newConfigContent, "text");


            if (success) {
                log.info("成功将节点 ID {} 的 WireGuard 配置发布到 Nacos (DataID: {}, Group: {}). 内容长度: {}",
                        nodeId, dataId, NACOS_WIREGUARD_GROUP, newConfigContent.length());
                log.debug("为节点 {} 发布的配置内容:\n{}", nodeId, newConfigContent);
            } else {
                log.error("未能将节点 ID {} 的 WireGuard 配置发布到 Nacos (DataID: {}, Group: {}).",
                        nodeId, dataId, NACOS_WIREGUARD_GROUP);
            }
        } catch (Exception e) { // NacosException 是受检查异常，但configService.publishConfig也可能抛其他运行时异常
            log.error("发布配置到 Nacos 时发生异常 (DataID: {}, Group: {}): {}", dataId, NACOS_WIREGUARD_GROUP, e.getMessage(), e);
            success = false; // 确保返回false
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