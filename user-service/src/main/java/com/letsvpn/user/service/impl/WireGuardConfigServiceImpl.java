// WireGuardConfigServiceImpl.java (实现 - 重点逻辑)
package com.letsvpn.user.service.impl;


import com.letsvpn.common.data.entity.User;
import com.letsvpn.common.data.entity.UserNodeConfig;
import com.letsvpn.common.data.mapper.UserMapper;
import com.letsvpn.common.data.mapper.UserNodeConfigMapper;
import com.letsvpn.user.entity.Node;
import com.letsvpn.user.mapper.NodeMapper;
import com.letsvpn.user.service.WireGuardConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 推荐加上事务

import java.util.List;
import java.util.Optional;
// 可能需要的其他 import，例如用于执行命令或调用库

@Slf4j
@Service
@RequiredArgsConstructor
public class WireGuardConfigServiceImpl implements WireGuardConfigService {

    private final UserMapper userMapper;
    private final NodeMapper nodeMapper;
    private final UserNodeConfigMapper userNodeConfigMapper;
    // 可能还需要注入用于IP分配、密钥生成、节点更新的服务或组件

    @Override
    @Transactional // 建议将整个流程放在一个事务中
    public String generateClientConfig(String username, Long nodeId) {
        // 1. 验证用户和节点是否存在及权限
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        // MyBatis Plus BaseMapper 自带 selectById
        Node node = nodeMapper.selectById(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("节点不存在");
        }
        // 检查节点状态
        if (node.getStatus() != 0) {
            throw new IllegalArgumentException("节点当前不可用");
        }
        // 检查用户等级是否满足节点要求 (示例逻辑)
        // if (!node.getIsFree() && user.getLevel() < node.getLevelRequired()) {
        //     throw new IllegalArgumentException("用户等级不足，无法访问该节点");
        // }
        // 检查 VIP 是否过期 (如果需要)
        // if (!node.getIsFree() && (user.getVipExpireTime() == null || user.getVipExpireTime().isBefore(LocalDateTime.now()))) {
        //     throw new IllegalArgumentException("VIP 已过期");
        // }

        // 2. 生成客户端密钥对 (!!! 核心实现，需要替换为实际逻辑 !!!)
        KeyPair clientKeyPair = generateWgKeyPair(); // 需要实现这个方法或调用库/命令
        String clientPrivateKey = clientKeyPair.getPrivateKey(); // 临时持有，用完丢弃
        String clientPublicKey = clientKeyPair.getPublicKey();

        // 3. 分配内网 IP 地址 (!!! 核心实现，需要替换为实际逻辑 !!!)
        String allocatedIpCidr = allocateIpAddress(node.getWgAddress(), nodeId); // 需要实现 IPAM 逻辑

        // 4. 保存/更新 Peer 配置到数据库
        // 尝试查找现有配置
        Optional<UserNodeConfig> existingConfigOpt = userNodeConfigMapper.findByUserIdAndNodeId(user.getId(), nodeId);

        UserNodeConfig configToSave;
        if (existingConfigOpt.isPresent()) {
            // 更新现有配置
            configToSave = existingConfigOpt.get();
            log.info("用户 {} 在节点 {} 已有配置，进行更新...", username, nodeId);
        } else {
            // 创建新配置
            configToSave = UserNodeConfig.builder()
                    .userId(user.getId())
                    .nodeId(nodeId)
                    .isActive(true) // 默认激活
                    .build();
            log.info("为用户 {} 在节点 {} 创建新配置...", username, nodeId);
        }
        // 更新公钥和 IP
        configToSave.setWgPeerPublicKey(clientPublicKey);
        configToSave.setWgAllowedIps(allocatedIpCidr);
        // MyBatis Plus 的 insertOrUpdate 可以基于 ID 判断，或者自己判断后调用 insert/update
        // 这里假设用 BaseMapper 的 saveOrUpdate (需要实体类有 @TableId)
        // 或者更精确地：
        if (configToSave.getId() != null) {
            userNodeConfigMapper.updateById(configToSave);
        } else {
            userNodeConfigMapper.insert(configToSave);
        }
        log.info("数据库 Peer 配置已保存/更新.");


        // 5. 触发 WireGuard 节点服务器更新 (!!! 核心实现，需要替换为实际逻辑 !!!)
        //    需要将 clientPublicKey 和 allocatedIpCidr 添加到节点的 wg 配置文件中
        //    并执行类似 'wg syncconf wg0 <(wg-quick strip wg0)' 的非中断命令
        boolean updateSuccess = triggerNodeConfigUpdate(node.getIp(), clientPublicKey, allocatedIpCidr);
        if (!updateSuccess) {
            log.error("更新节点 {} 的 WireGuard 配置失败!", node.getIp());
            // 注意：这里应该抛出异常，让事务回滚数据库的更改！
            throw new RuntimeException("更新节点服务器配置失败");
        }
        log.info("已触发节点 {} 配置更新.", node.getIp());

        // 6. 构造客户端配置文件内容
        String clientConfig = buildClientConfigFile(clientPrivateKey, allocatedIpCidr, node);
        log.info("客户端配置文件已生成.");

        // 7. 返回配置文件内容 (私钥已包含在内)
        return clientConfig;
    }

    // --- 需要实现的辅助方法 (示例，需要具体实现) ---

    private static class KeyPair {
        private String privateKey;
        private String publicKey;
        // getters...
        public KeyPair(String priv, String pub) { this.privateKey = priv; this.publicKey = pub; }
        public String getPrivateKey() { return privateKey; }
        public String getPublicKey() { return publicKey; }
    }

    private KeyPair generateWgKeyPair() {
        // TODO: 实现密钥对生成逻辑
        // 例如，执行命令行 "wg genkey" 和 "wg pubkey"
        // 或者使用 BouncyCastle 等 Java 库
        // !!! 确保安全可靠 !!!
        log.warn("generateWgKeyPair() 需要实现!");
        // 临时返回占位符 - 必须替换
        return new KeyPair("CLIENT_PRIVATE_KEY_PLACEHOLDER", "CLIENT_PUBLIC_KEY_PLACEHOLDER");
    }

    private String allocateIpAddress(String subnetCidr, Long nodeId) {
        // TODO: 实现 IP 地址分配 (IPAM) 逻辑
        // 1. 解析 subnetCidr (e.g., "10.0.0.2/24") 获取网络地址和可用范围
        // 2. 查询 userNodeConfigMapper.findAllocatedIpsByNodeId(nodeId) 获取该节点已用 IP
        // 3. 在可用范围内查找一个未被使用的 IP
        // 4. 返回 CIDR 格式 (e.g., "10.0.0.X/32")
        // !!! 确保线程安全和唯一性 !!!
        log.warn("allocateIpAddress() 需要实现!");
        // 临时返回占位符 - 必须替换
        // 注意：需要根据 node 的 wg_address 动态生成，这里只是示例
        List<String> usedIps = userNodeConfigMapper.findAllocatedIpsByNodeId(nodeId);
        // 简单示例：从 10.0.0.3 开始找第一个没用的
        String baseIp = "10.0.0."; // 假设网段是这个，需要从 node.wgAddress 解析
        int startIp = 3;
        while(true) {
            String candidateIp = baseIp + startIp;
            String candidateCidr = candidateIp + "/32";
            if (!usedIps.contains(candidateCidr)) {
                return candidateCidr;
            }
            startIp++;
            if (startIp > 254) { // 简单边界检查
                throw new RuntimeException("无法为节点 " + nodeId + " 分配 IP 地址，可能已满");
            }
        }
        // return "10.0.0.X/32";
    }

    private boolean triggerNodeConfigUpdate(String nodeIp, String clientPublicKey, String clientAllowedIps) {
        // TODO: 实现触发节点更新的逻辑
        // 可以是：
        // 1. SSH 连接到 nodeIp，执行 'wg set peer <pubkey> allowed-ips <ips>' 或修改文件后执行 'wg syncconf'
        // 2. 调用部署在节点上的 Agent 的 API
        // 3. 发送消息到队列，由节点 Agent 监听处理
        // 4. 使用 Ansible/SaltStack 等工具执行更新
        // !!! 确保安全可靠且使用非中断命令 !!!
        log.warn("triggerNodeConfigUpdate() 需要实现! 目标节点 IP: {}", nodeIp);
        // 临时返回 true - 必须替换
        return true;
    }

    private String buildClientConfigFile(String clientPrivateKey, String clientAddressCidr, Node node) {
        // 使用 StringBuilder 或 String.format 构造配置文件内容
        StringBuilder sb = new StringBuilder();
        sb.append("[Interface]\n");
        sb.append("PrivateKey = ").append(clientPrivateKey).append("\n");
        sb.append("Address = ").append(clientAddressCidr).append("\n");
        if (node.getWgDns() != null && !node.getWgDns().isEmpty()) {
            sb.append("DNS = ").append(node.getWgDns()).append("\n");
        }
        sb.append("\n");
        sb.append("[Peer]\n");
        sb.append("PublicKey = ").append(node.getWgPublicKey()).append("\n"); // 服务器公钥
        sb.append("AllowedIPs = 0.0.0.0/0, ::/0").append("\n"); // 路由所有流量
        sb.append("Endpoint = ").append(node.getIp()).append(":").append(node.getPort()).append("\n");
        // sb.append("PersistentKeepalive = 25\n"); // 可选
        return sb.toString();
    }
}