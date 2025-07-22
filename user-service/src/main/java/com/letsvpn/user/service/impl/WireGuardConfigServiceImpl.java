// WireGuardConfigServiceImpl.java (实现 - 重点逻辑)
package com.letsvpn.user.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.common.core.exception.BizException;
import com.letsvpn.common.data.entity.User;
import com.letsvpn.common.data.entity.UserNode;
import com.letsvpn.common.data.mapper.UserMapper;
import com.letsvpn.common.data.mapper.UserNodeMapper;
import com.letsvpn.user.entity.Node;
import com.letsvpn.user.entity.WireguardKeyPool;
import com.letsvpn.user.mapper.NodeMapper;
import com.letsvpn.user.mapper.WireguardKeyPoolMapper;
import com.letsvpn.user.service.WireGuardConfigService;
import com.letsvpn.user.util.WireGuardKeyGenerator;
import com.letsvpn.user.vo.UserNodeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 推荐加上事务
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
// 可能需要的其他 import，例如用于执行命令或调用库

@Slf4j
@Service
@RequiredArgsConstructor
public class WireGuardConfigServiceImpl implements WireGuardConfigService {

    private final UserMapper userMapper;
    private final NodeMapper nodeMapper;
    private final UserNodeMapper userNodeMapper;
    private final WireguardKeyPoolMapper wireguardKeyPoolMapper;
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
        Optional<UserNode> existingConfigOpt = userNodeMapper.findByUserIdAndNodeId(user.getId(), nodeId);

        UserNode configToSave;
        if (existingConfigOpt.isPresent()) {
            // 更新现有配置
            configToSave = existingConfigOpt.get();
            log.info("用户 {} 在节点 {} 已有配置，进行更新...", username, nodeId);
        } else {
            // 创建新配置
            configToSave = UserNode.builder()
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
            userNodeMapper.updateById(configToSave);
        } else {
            userNodeMapper.insert(configToSave);
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
        List<String> usedIps = userNodeMapper.findAllocatedIpsByNodeId(nodeId);
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


    @Override
    @Transactional
    public UserNode assignOrGetUserNodeConfig(Long userId, Long nodeId) {
        // 检查用户和节点是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在: " + userId);
        }
        Node node = nodeMapper.selectById(nodeId);
        if (node == null) {
            throw new BizException("节点不存在: " + nodeId);
        }

        // 检查用户是否有权限访问该节点 (基于用户level和node.levelRequired)
        if (node.getLevelRequired() != null && user.getLevel() != null && user.getLevel() < node.getLevelRequired()) {
            // 进一步检查VIP是否过期
            if(Boolean.FALSE.equals(node.getIsFree())) { // 如果不是免费节点，才严格检查VIP
                if(user.getVipExpireTime() == null || user.getVipExpireTime().isBefore(LocalDateTime.now())){
                    log.warn("用户 {} (等级 {}) 尝试访问付费节点 {} (要求等级 {}), 但其VIP已过期或等级不足。",
                            userId, user.getLevel(), nodeId, node.getLevelRequired());
                    throw new BizException("您的VIP已过期或等级不足，无法分配或使用该节点。");
                }
            } else if (Boolean.TRUE.equals(node.getIsFree()) && node.getLevelRequired() > 0 && user.getLevel() < node.getLevelRequired()) {
                // 即使是免费节点，如果它也设置了levelRequired（例如限制游客使用特定免费节点），也需要判断
                log.warn("用户 {} (等级 {}) 尝试访问免费节点 {} (要求等级 {}), 但其等级不足。",
                        userId, user.getLevel(), nodeId, node.getLevelRequired());
                throw new BizException("您的等级不足，无法分配或使用该免费节点。");
            }
        }


        QueryWrapper<UserNode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("node_id", nodeId);
        UserNode existingConfig = userNodeMapper.selectOne(queryWrapper);

        if (existingConfig != null) {
            log.info("用户 {} 在节点 {} 上已存在配置，直接返回。配置ID: {}", userId, nodeId, existingConfig.getId());
            // 确保返回的配置是激活的，如果不是，可以考虑重新激活或提示
            if (Boolean.FALSE.equals(existingConfig.getIsActive())) {
                log.info("用户 {} 在节点 {} 的现有配置(ID:{})为非激活状态，将尝试激活。", userId, nodeId, existingConfig.getId());
                existingConfig.setIsActive(true);
                existingConfig.setUpdatedAt(LocalDateTime.now());
                userNodeMapper.updateById(existingConfig);
            }
            return existingConfig;
        }

        log.info("为用户 {} 在节点 {} 上创建新的WireGuard配置。", userId, nodeId);
        UserNode newConfig = new UserNode();
        newConfig.setUserId(userId);
        newConfig.setNodeId(nodeId);

        WireGuardKeyGenerator.KeyPair keyPair = WireGuardKeyGenerator.generateKeyPair();
        newConfig.setWgPeerPublicKey(keyPair.getPublicKey()); // 这是客户端（Peer）的公钥，由服务端生成
        newConfig.setWgPeerPrivateKey(keyPair.getPrivateKey());
        // 客户端的私钥需要安全地传递给客户端，服务端不应该保存客户端私钥。
        // 如果wg_private_key是服务端的，那它属于Node实体。

        String nextAvailableIp = getNextAvailableIpForNode(node);
        newConfig.setWgAllowedIps(nextAvailableIp + "/32");

        newConfig.setIsActive(true);
        newConfig.setCreatedAt(LocalDateTime.now());
        newConfig.setUpdatedAt(LocalDateTime.now());

        userNodeMapper.insert(newConfig);
        log.info("新配置创建成功，ID: {}，用户ID: {}, 节点ID: {}, 公钥: {}, IP: {}",
                newConfig.getId(), userId, nodeId, newConfig.getWgPeerPublicKey(), newConfig.getWgAllowedIps());
        return newConfig;
    }

//    private String getNextAvailableIpForNode(Node node) {
//        // ... (保持或改进你的IPAM逻辑) ...
//        // 这是一个关键的生产逻辑，需要健壮实现
//        // 示例（非常简化，需要替换）:
//        // 获取节点配置的网络基地址，例如 node.getWgInterfaceAddress() -> "10.0.1.1/24"
//        // 从这个CIDR中找到一个未被UserNodeConfig使用的IP
//        // SELECT wg_allowed_ips FROM user_node WHERE node_id = :nodeId
//        // 解析这些IP，找到一个可用的。
//
//        // 临时示例，确保这个逻辑是正确的
//        String networkSegment = node.getWgAddress(); // 例如 "10.0.1.0/24" 或 "10.0.1.1/24"
//        if (networkSegment == null || !networkSegment.contains("/")) {
//            log.error("节点 {} 的网络段 (wg_address) 配置错误: {}", node.getId(), networkSegment);
//            throw new BizException("节点网络配置错误，无法分配IP");
//        }
//        // 简化假设：总是能找到一个IP，实际需要更复杂的逻辑
//        // 例如，从 "10.0.1.1/24" 中分配 "10.0.1.X"
//        // 这里只是一个占位符，你需要一个真正的IPAM
//        long count = userNodeMapper.selectCount(new QueryWrapper<UserNode>().eq("node_id", node.getId()));
//        String[] networkParts = networkSegment.split("/")[0].split("\\.");
//        if (networkParts.length != 4) throw new BizException("节点网络地址格式错误");
//
//        long nextIpSuffix = 2 + count; // 假设从.2开始，且IP是连续分配的
//        if (nextIpSuffix > 254) throw new BizException("节点IP池已满");
//
//        return networkParts[0] + "." + networkParts[1] + "." + networkParts[2] + "." + nextIpSuffix;
//    }

    // 辅助方法：IP字符串转long (保持不变或使用库)
    private long ipToLong(String ipAddress) {
        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) {
            // 更健壮的错误处理
            throw new IllegalArgumentException("无效的IP地址格式: " + ipAddress);
        }
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result <<= 8;
            result |= (Integer.parseInt(parts[i]) & 0xFF);
        }
        return result;
    }

    // 辅助方法：long转IP字符串 (保持不变或使用库)
    private String longToIp(long ip) {
        return String.format("%d.%d.%d.%d",
                (ip >> 24 & 0xFF),
                (ip >> 16 & 0xFF),
                (ip >> 8 & 0xFF),
                (ip & 0xFF));
    }
    /**
     * 为指定节点分配下一个可用的Peer IP地址。
     * @param node 包含wg_address (例如 "10.0.0.1/22") 的节点对象
     * @return 分配到的IP地址字符串 (例如 "10.0.0.2")
     */
    public String getNextAvailableIpForNode(Node node) {
        if (node == null || node.getId() == null) {
            log.error("getNextAvailableIpForNode 调用时节点对象或节点ID为空");
            throw new BizException("节点信息不能为空");
        }
        String serverCidr = node.getWgAddress(); // 例如 "10.0.0.1/22"
        if (serverCidr == null || serverCidr.trim().isEmpty() || !serverCidr.contains("/")) {
            log.error("节点 {} 的网络段 (wg_address) 配置错误或为空: {}", node.getId(), serverCidr);
            throw new BizException("节点网络配置错误，无法分配IP (wg_address 无效)");
        }

        SubnetUtils utils;
        try {
            utils = new SubnetUtils(serverCidr);
            // inclusiveHostCount=false (默认) 会排除网络地址和广播地址
        } catch (IllegalArgumentException e) {
            log.error("无法解析节点 {} 的CIDR: {}。错误: {}", node.getId(), serverCidr, e.getMessage());
            throw new BizException("节点网络配置错误，无法解析CIDR");
        }

        SubnetUtils.SubnetInfo subnetInfo = utils.getInfo();
        String serverIpStr = serverCidr.split("/")[0];
        long serverIpLong;
        try {
            serverIpLong = ipToLong(serverIpStr);
        } catch (IllegalArgumentException e) {
            log.error("无法解析节点 {} 的服务器IP部分: {}。错误: {}", node.getId(), serverIpStr, e.getMessage());
            throw new BizException("节点网络配置错误，无法解析服务器IP");
        }


        // 1. 获取所有当前已占用的 IP 地址
        Set<Long> occupiedIps = new HashSet<>();
        occupiedIps.add(serverIpLong); // 添加服务器自身的 IP

        // 从数据库获取该节点下所有已分配给 Peers 的 AllowedIPs 列表
        List<String> allocatedPeerWgAllowedIps = userNodeMapper.findAllocatedIpsByNodeId(node.getId());
        if (allocatedPeerWgAllowedIps != null) {
            for (String wgAllowedIpEntry : allocatedPeerWgAllowedIps) {
                if (wgAllowedIpEntry != null && !wgAllowedIpEntry.trim().isEmpty()) {
                    // wg_allowed_ips 可能包含多个IP，或单个IP带掩码，例如 "10.0.0.5/32"
                    // 或者 "10.0.0.5/32, 192.168.1.0/24"
                    // 我们这里只关心分配给 Peer 在 WireGuard 网络内的那个 IP
                    // 通常是列表中的第一个 IP，并且我们只取 IP 地址部分
                    String[] ipsInEntry = wgAllowedIpEntry.split(",");
                    if (ipsInEntry.length > 0 && ipsInEntry[0] != null && !ipsInEntry[0].trim().isEmpty()) {
                        String primaryPeerIpWithMask = ipsInEntry[0].trim();
                        String peerIpStr = primaryPeerIpWithMask.split("/")[0];
                        try {
                            occupiedIps.add(ipToLong(peerIpStr));
                        } catch (IllegalArgumentException e) {
                            log.warn("无法解析节点 {} 已分配的Peer IP条目中的IP部分: {} (来自原始条目: {})。错误: {}",
                                    node.getId(), peerIpStr, wgAllowedIpEntry, e.getMessage());
                        }
                    }
                }
            }
        }

        // 2. 从子网的可用主机IP范围开始迭代，查找第一个未被占用的IP
        long lowUsableHostIpLong;
        long highUsableHostIpLong;
        try {
            lowUsableHostIpLong = ipToLong(subnetInfo.getLowAddress());     // 子网的第一个可用主机IP
            highUsableHostIpLong = ipToLong(subnetInfo.getHighAddress());   // 子网的最后一个可用主机IP
        } catch (IllegalArgumentException e) {
            log.error("无法解析节点 {} 的子网边界IP: LowAddress='{}', HighAddress='{}'. 错误: {}",
                    node.getId(), subnetInfo.getLowAddress(), subnetInfo.getHighAddress(), e.getMessage());
            throw new BizException("节点网络配置错误，无法解析子网边界IP");
        }


        for (long currentIpLong = lowUsableHostIpLong; currentIpLong <= highUsableHostIpLong; currentIpLong++) {
            if (!occupiedIps.contains(currentIpLong)) {
                // 找到了一个可用的 IP
                String assignedIp = longToIp(currentIpLong);
                log.info("为节点 {} (服务器IP: {}) 分配了新的 Peer IP: {}", node.getId(), serverIpStr, assignedIp);
                // 注意：在高并发情况下，这里依然存在选中同一个IP的风险，需要数据库层面的锁或唯一约束来保证最终IP分配的唯一性。
                // 例如，可以将 UserNodeConfig 表中 (node_id, peer_wg_ip) 设置为联合唯一索引。
                return assignedIp;
            }
        }

        // 如果循环完成，表示没有找到可用的 IP
        log.error("节点 {} IP池已满。CIDR: {}, 可用主机范围: {} - {}. 当前已记录占用IP数量: {} (包括服务器IP)",
                node.getId(), serverCidr, subnetInfo.getLowAddress(), subnetInfo.getHighAddress(), occupiedIps.size());
        throw new BizException("节点IP池已满，无可用IP");
    }


//    @Override
//    public Map<String, Object> generateClientConfigById(Long userNodeConfigId, Long userId) {
//        // ... (之前的实现逻辑，确保从nodeMapper获取节点信息) ...
//        UserNodeConfig config = userNodeConfigMapper.selectById(userNodeConfigId);
//        if (config == null) throw new BizException("用户节点配置不存在: " + userNodeConfigId);
//        if (!config.getUserId().equals(userId)) throw new BizException("无权访问该节点配置");
//        if (Boolean.FALSE.equals(config.getIsActive())) throw new BizException("该节点配置当前未激活");
//
//        Node node = nodeMapper.selectById(config.getNodeId());
//        if (node == null || !Objects.equals(node.getStatus(), 0)) throw new BizException("关联的节点当前不可用");
//
//        User user = userMapper.selectById(userId);
//        if (user == null) throw new BizException("用户不存在");
//        if (node.getLevelRequired() != null && user.getLevel() != null && user.getLevel() < node.getLevelRequired()) {
//            if(Boolean.FALSE.equals(node.getIsFree())) {
//                if(user.getVipExpireTime() == null || user.getVipExpireTime().isBefore(LocalDateTime.now())){
//                    throw new BizException("您的VIP已过期或等级不足，无法使用该节点。");
//                }
//            } else if (Boolean.TRUE.equals(node.getIsFree()) && node.getLevelRequired() > 0 && user.getLevel() < node.getLevelRequired()) {
//                throw new BizException("您的等级不足，无法使用该免费节点。");
//            }
//        }
//
//        StringBuilder sb = new StringBuilder();
//        sb.append("[Interface]\n");
//        sb.append("# Client PublicKey: ").append(config.getWgPeerPublicKey()).append("\n");
//        sb.append("# 请将与上述公钥对应的客户端私钥填入下一行\n");
//        sb.append("PrivateKey = <在此填写您的客户端私钥>\n");
//        sb.append("Address = ").append(config.getWgAllowedIps()).append("\n");
//        if (node.getWgDns() != null && !node.getWgDns().isEmpty()) {
//            sb.append("DNS = ").append(node.getWgDns()).append("\n");
//        }
//        sb.append("\n[Peer]\n");
//        sb.append("# Server Node: ").append(node.getName()).append("\n");
//        sb.append("PublicKey = ").append(node.getWgPublicKey()).append("\n"); // 服务器节点的公钥
//        sb.append("AllowedIPs = 0.0.0.0/0, ::/0\n");
//        sb.append("Endpoint = ").append(node.getIp()).append(":").append(node.getPort()).append("\n");
////        if (node.getWgPresharedKey() != null && !node.getWgPresharedKey().isEmpty()) {
////            sb.append("PresharedKey = ").append(node.getWgPresharedKey()).append("\n");
////        }
//        sb.append("PersistentKeepalive = 25\n");
//
//        Map<String, Object> result = new java.util.HashMap<>();
//        result.put("config_string", sb.toString());
//        result.put("client_public_key", config.getWgPeerPublicKey()); // 客户端应使用的公钥
//        result.put("server_public_key", node.getWgPublicKey());   // 服务器的公钥
//        result.put("server_endpoint", node.getIp() + ":" + node.getPort());
//        result.put("assigned_ip", config.getWgAllowedIps());
//        result.put("dns_servers", node.getWgDns());
////        if (node.getWgPresharedKey() != null && !node.getWgPresharedKey().isEmpty()) {
////            result.put("preshared_key", node.getWgPresharedKey());
////        }
//        result.put("note", "请将您本地生成的与公钥 " + config.getWgPeerPublicKey() + " 配对的私钥填入配置文件中的PrivateKey字段。");
//
//        return result;
//    }

    /**
     * 实现：根据用户ID获取该用户的所有节点配置列表 (返回VO)。
     */
    @Override
    public List<UserNodeVO> getUserNodeConfigsByUserId(Long userId) {
        if (userId == null) {
            log.warn("尝试获取用户节点配置列表失败：userId为空。");
            return Collections.emptyList();
        }

        QueryWrapper<UserNode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        // queryWrapper.eq("is_active", true); // 根据业务需求决定是否只查询激活的
        queryWrapper.orderByAsc("node_id");

        List<UserNode> configs = userNodeMapper.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(configs)) {
            log.info("用户ID {} 没有找到任何节点配置。", userId);
            return Collections.emptyList();
        }

        // 获取所有相关的节点ID，以便一次性查询节点信息，避免N+1查询
        List<Long> nodeIds = configs.stream()
                .map(UserNode::getNodeId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Node> nodeMap = Collections.emptyMap();
        if (!CollectionUtils.isEmpty(nodeIds)) {
            List<Node> nodes = nodeMapper.selectBatchIds(nodeIds);
            nodeMap = nodes.stream().collect(Collectors.toMap(Node::getId, Function.identity()));
        }

        // 组装VO列表
        Map<Long, Node> finalNodeMap = nodeMap; // effectively final for lambda
        List<UserNodeVO> vos = configs.stream().map(config -> {
            Node node = finalNodeMap.get(config.getNodeId());
            UserNodeVO.UserNodeVOBuilder voBuilder = UserNodeVO.builder();

            // 从 UserNodeConfig 复制基础属性
            voBuilder.configId(config.getId());
            voBuilder.userId(config.getUserId());
            voBuilder.nodeId(config.getNodeId());
//            voBuilder.wgPeerPublicKey(config.getWgPeerPublicKey());
            voBuilder.wgAddress(config.getWgAllowedIps());
            voBuilder.isActive(config.getIsActive());
            voBuilder.createdAt(config.getCreatedAt());

            // 从关联的 Node 填充信息
            if (node != null) {
                voBuilder.name(node.getName());
                // 假设Node实体有countryCode和locationName字段用于组合显示

                voBuilder.ip(node.getIp()); // 或 node.getHost()
                voBuilder.port(node.getPort());
                voBuilder.nodeLevelRequired(node.getLevelRequired());
                voBuilder.wgPublicKey(node.getWgPublicKey());
                voBuilder.isFree(node.getIsFree());
                voBuilder.wgPrivateKey(config.getWgPeerPrivateKey());
                voBuilder.wgDns(node.getWgDns());


                String template = "vless://c093e6d4-6ec5-4e82-9479-8f382cb2ae0a@141.98.199.58:443?encryption=none&flow=xtls-rprx-vision&security=reality&sni=www.cloudflare.com&fp=chrome&pbk=gvqfZ4Wa3uZ7O1tqiVbs515qcHILVTjDmVbxacED3gQ&sid=4f77d10f&type=tcp#SG-Reality";


                QueryWrapper<WireguardKeyPool> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("public_key", config.getWgPeerPublicKey());
                queryWrapper1.eq("private_key", config.getWgPeerPrivateKey());
                // queryWrapper.eq("is_active", true); // 根据业务需求决定是否只查询激活的

                WireguardKeyPool keyPool = wireguardKeyPoolMapper.selectOne(queryWrapper1);

                if (keyPool == null) {
                    throw new BizException("密钥分配错误 userId: " + userId+",nodeId: " + config.getNodeId());
                }

                String uuid = keyPool.getUuid();
                String ip = node.getSingboxIp();

                String result = template
                        .replace("c093e6d4-6ec5-4e82-9479-8f382cb2ae0a", uuid)
                        .replace("141.98.199.58", ip);

                voBuilder.vlessUri(result);


            } else {
                log.warn("用户配置 configId={} 关联的 nodeId={} 未找到对应的Node实体！", config.getId(), config.getNodeId());
                voBuilder.name("节点信息丢失"); // 或其他默认值
                voBuilder.nodeLocation("未知位置");
            }
            return voBuilder.build();
        }).collect(Collectors.toList());

        log.info("为用户ID {} 查询到并转换了 {} 条 UserNodeConfigVO。", userId, vos.size());
        return vos;
    }

    @Override
    @Transactional
    public void generateKeyPairsForAllNodes(int batchSizePerNode) {
        log.info("开始为所有活跃节点生成密钥对，每个节点 {} 对", batchSizePerNode);

        // 获取所有活跃节点
        QueryWrapper<Node> nodeQuery = new QueryWrapper<>();
        nodeQuery.eq("status", 0); // 状态为0表示活跃
        List<Node> activeNodes = nodeMapper.selectList(nodeQuery);

        List<Node> freeNodes = activeNodes.stream()
                .filter(node -> node.getIsFree())
                .collect(Collectors.toList());

        List<Node> paidNodes = activeNodes.stream()
                .filter(node -> !node.getIsFree())
                .collect(Collectors.toList());

        List<User> users = userMapper.selectList(new QueryWrapper<User>().eq("status", 0));

        List<User> freeUsers = users.stream().filter(user -> user.getLevel()==0).collect(Collectors.toList());

        List<User> paidUsers = users.stream().filter(user -> user.getLevel()!=0).collect(Collectors.toList());


        for (Node node : activeNodes) {
            try {
                generateKeyPairsForNode(node.getId(), batchSizePerNode);
            } catch (Exception e) {
                log.error("为节点 {} 生成密钥对时发生错误", node.getId(), e);
                // 继续处理其他节点
            }
        }

        log.info("完成所有活跃节点的密钥对生成");

        //为所有已有user分配
        for (Node freeNode : freeNodes) {
            for (User user : users) {
                allocateKey(user.getId(), freeNode.getId());
            }
        }

        for (Node paidNode : paidNodes) {
            for (User paidUser : paidUsers) {
                allocateKey(paidUser.getId(),paidNode.getId());
            }
        }

        log.info("为已有用户分配密钥对生成");

    }


    @Override
    @Transactional
    public int generateKeyPairsForNode(Long nodeId, int batchSize) {
        log.info("开始为节点 {} 生成 {} 对WireGuard密钥", nodeId, batchSize);
        
        Node node = nodeMapper.selectById(nodeId);
        if (node == null) {
            log.error("节点 {} 不存在", nodeId);
            throw new BizException("节点不存在: " + nodeId);
        }

        // 获取该节点所有已占用的IP地址（包括user_node表和wireguard_key_pool表）
        Set<String> occupiedIps = getOccupiedIpsForNode(nodeId);
        
        // 解析节点的网络配置
        String serverCidr = node.getWgAddress();
        if (serverCidr == null || serverCidr.trim().isEmpty() || !serverCidr.contains("/")) {
            log.error("节点 {} 的网络段配置错误: {}", nodeId, serverCidr);
            throw new BizException("节点网络配置错误: " + serverCidr);
        }

        // 验证节点CIDR格式
        if (!isValidCidr(serverCidr)) {
            log.error("节点 {} 的CIDR格式无效: {}", nodeId, serverCidr);
            throw new BizException("节点CIDR格式无效: " + serverCidr);
        }

        // 提取节点自身的IP地址，确保不与节点自身IP冲突
        String serverIp = serverCidr.split("/")[0];
        String serverIpCidr = serverIp + "/32";
        occupiedIps.add(serverIpCidr); // 添加节点自身IP到已占用列表
        log.info("节点 {} 自身IP: {} 已添加到占用列表", nodeId, serverIpCidr);

        SubnetUtils utils;
        try {
            utils = new SubnetUtils(serverCidr);
        } catch (IllegalArgumentException e) {
            log.error("无法解析节点 {} 的CIDR: {}", nodeId, serverCidr);
            throw new BizException("无法解析节点CIDR: " + serverCidr);
        }

        SubnetUtils.SubnetInfo subnetInfo = utils.getInfo();
        long lowUsableHostIpLong = ipToLong(subnetInfo.getLowAddress());
        long highUsableHostIpLong = ipToLong(subnetInfo.getHighAddress());

        int generatedCount = 0;
        long currentIpLong = lowUsableHostIpLong;

        for (int i = 0; i < batchSize && currentIpLong <= highUsableHostIpLong; currentIpLong++) {
            String candidateIp = longToIp(currentIpLong);
            String candidateCidr = candidateIp + "/32";
            
            // 验证生成的CIDR格式
            if (!isValidCidr(candidateCidr)) {
                log.warn("生成的CIDR格式无效，跳过: {}", candidateCidr);
                continue;
            }
            
            // 检查IP是否已被占用（包括节点自身IP）
            if (!occupiedIps.contains(candidateCidr)) {
                try {
                    WireguardKeyPool newConfig = new WireguardKeyPool();
                    newConfig.setNodeId(nodeId);

                    WireGuardKeyGenerator.KeyPair keyPair = WireGuardKeyGenerator.generateKeyPair();
                    newConfig.setPublicKey(keyPair.getPublicKey());
                    newConfig.setPrivateKey(keyPair.getPrivateKey());
                    newConfig.setAddress(candidateCidr);
                    newConfig.setStatus(0);
                    newConfig.setCreatedAt(LocalDateTime.now());
                    newConfig.setUuid(UUID.randomUUID().toString());
                    
                    wireguardKeyPoolMapper.insert(newConfig);
                    generatedCount++;
                    
                    // 将新分配的IP添加到已占用集合中，避免后续循环重复分配
                    occupiedIps.add(candidateCidr);
                    
                    log.debug("为节点 {} 生成第 {} 个密钥对，分配IP: {}", nodeId, generatedCount, candidateCidr);
                } catch (Exception e) {
                    log.error("为节点 {} 生成第 {} 个密钥对失败", nodeId, i + 1, e);
                }
            } else {
                log.debug("IP {} 已被占用，跳过", candidateCidr);
            }
        }

        if (generatedCount < batchSize) {
            log.warn("节点 {} 的IP池空间不足，只生成了 {} 个密钥对，请求数量: {}", nodeId, generatedCount, batchSize);
        }

        log.info("为节点 {} 成功生成 {} 对WireGuard密钥", nodeId, generatedCount);
        return generatedCount;
    }


    @Override
    @Transactional
    public int allocateKeyPairforNode(Long nodeId, int batchSize) {
        log.info("开始为节点 {} 生成 {} 对WireGuard密钥", nodeId, batchSize);

        Node node = nodeMapper.selectById(nodeId);
        if (node == null) throw new BizException("节点不存在: " + nodeId);

        Set<String> occupiedIps = getOccupiedIpsForNode(nodeId);

        String serverCidr = node.getWgAddress();
        if (serverCidr == null || !serverCidr.contains("/")) {
            throw new BizException("节点网络配置错误: " + serverCidr);
        }

        if (!isValidCidr(serverCidr)) {
            throw new BizException("节点CIDR格式无效: " + serverCidr);
        }

        String baseIp = serverCidr.split("/")[0];
        int subnetBits = Integer.parseInt(serverCidr.split("/")[1]);

        // 计算每个子网最多多少主机（排除 .0 和 .255）
        long hostsPerSubnet = (1L << (32 - subnetBits));

        long currentIpLong = ipToLong(baseIp);
        occupiedIps.add(baseIp + "/32");

        int generatedCount = 0;
        while (generatedCount < batchSize) {
            // 遍历当前子网
            long subnetStart = currentIpLong & (~(hostsPerSubnet - 1)); // 当前子网起点
            long subnetEnd = subnetStart + hostsPerSubnet - 1;

            for (long ipLong = subnetStart; ipLong <= subnetEnd && generatedCount < batchSize; ipLong++) {
                String ip = longToIp(ipLong);
                String cidr = ip + "/32";

                // 跳过保留地址
                if (ip.endsWith(".0") || ip.endsWith(".255") || !isValidCidr(cidr) || occupiedIps.contains(cidr)) {
                    continue;
                }

                try {
                    WireguardKeyPool newConfig = new WireguardKeyPool();
                    newConfig.setNodeId(nodeId);
                    WireGuardKeyGenerator.KeyPair keyPair = WireGuardKeyGenerator.generateKeyPair();
                    newConfig.setPublicKey(keyPair.getPublicKey());
                    newConfig.setPrivateKey(keyPair.getPrivateKey());
                    newConfig.setAddress(cidr);
                    newConfig.setStatus(0);
                    newConfig.setCreatedAt(LocalDateTime.now());
                    newConfig.setUuid(UUID.randomUUID().toString());

                    wireguardKeyPoolMapper.insert(newConfig);
                    occupiedIps.add(cidr);
                    generatedCount++;

                    log.debug("为节点 {} 分配第 {} 个 IP: {}", nodeId, generatedCount, cidr);
                } catch (Exception e) {
                    log.error("生成失败 IP: {}", cidr, e);
                }
            }

            // 下一个子网
            currentIpLong = subnetEnd + 1;
        }

        // 分配用户
        List<User> allUsers = userMapper.selectList(new QueryWrapper<User>().eq("status", 0));
        List<User> targetUsers = node.getIsFree()
                ? allUsers
                : allUsers.stream().filter(u -> u.getLevel() != 0).collect(Collectors.toList());

        for (User user : targetUsers) {
            allocateKey(user.getId(), nodeId);
        }

        log.info("为节点 {} 成功生成 {} 对WireGuard密钥", nodeId, generatedCount);
        return generatedCount;
    }



    /**
     * 验证CIDR格式是否有效
     * @param cidr CIDR字符串，如 "192.168.1.0/24"
     * @return 是否有效
     */
    private boolean isValidCidr(String cidr) {
        if (cidr == null || cidr.trim().isEmpty()) {
            return false;
        }
        
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            return false;
        }
        
        // 验证IP地址部分
        String ip = parts[0];
        if (!isValidIp(ip)) {
            return false;
        }
        
        // 验证子网掩码部分
        try {
            int mask = Integer.parseInt(parts[1]);
            return mask >= 0 && mask <= 32;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 验证IP地址格式是否有效
     * @param ip IP地址字符串
     * @return 是否有效
     */
    private boolean isValidIp(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }
        
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        
        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 获取节点所有已占用的IP地址
     */
    @Override
    public Set<String> getOccupiedIpsForNode(Long nodeId) {
        Set<String> occupiedIps = new HashSet<>();
        
        // 获取节点信息，提取节点自身IP
        Node node = nodeMapper.selectById(nodeId);
        if (node != null && node.getWgAddress() != null && !node.getWgAddress().trim().isEmpty()) {
            String serverCidr = node.getWgAddress();
            if (serverCidr.contains("/")) {
                String serverIp = serverCidr.split("/")[0];
                String serverIpCidr = serverIp + "/32";
                occupiedIps.add(serverIpCidr);
                log.debug("节点 {} 自身IP: {} 已添加到占用列表", nodeId, serverIpCidr);
            }
        }
        
        // 1. 从user_node表获取已分配的IP
        List<String> userNodeIps = userNodeMapper.findAllocatedIpsByNodeId(nodeId);
        if (userNodeIps != null) {
            for (String ipEntry : userNodeIps) {
                if (ipEntry != null && !ipEntry.trim().isEmpty()) {
                    String[] ipsInEntry = ipEntry.split(",");
                    if (ipsInEntry.length > 0 && ipsInEntry[0] != null && !ipsInEntry[0].trim().isEmpty()) {
                        occupiedIps.add(ipsInEntry[0].trim());
                    }
                }
            }
        }
        
        // 2. 从wireguard_key_pool表获取已分配的IP
        QueryWrapper<WireguardKeyPool> keyPoolQuery = new QueryWrapper<>();
        keyPoolQuery.eq("node_id", nodeId)
                   .select("address");
        List<WireguardKeyPool> keyPoolIps = wireguardKeyPoolMapper.selectList(keyPoolQuery);
        if (keyPoolIps != null) {
            for (WireguardKeyPool keyPool : keyPoolIps) {
                if (keyPool.getAddress() != null && !keyPool.getAddress().trim().isEmpty()) {
                    occupiedIps.add(keyPool.getAddress().trim());
                }
            }
        }
        
        log.debug("节点 {} 已占用IP数量: {}", nodeId, occupiedIps.size());
        return occupiedIps;
    }

    // 辅助方法示例，用于构建节点位置显示 (你可以根据Node实体实际字段调整)
    // private String buildNodeLocationDisplay(String countryCode, String locationName) {
    //     if (countryCode != null && !countryCode.isEmpty()) {
    //         try {
    //             // 这里需要一个库或方法将国家代码转为emoji旗帜
    //             // String flag = CountryCodeToEmoji.getFlag(countryCode);
    //             // return flag + " " + (locationName != null ? locationName : "");
    //             return countryCode.toUpperCase() + " " + (locationName != null ? locationName : ""); // 简化版
    //         } catch (Exception e) {
    //             log.warn("无法转换国家代码 {} 为旗帜emoji", countryCode);
    //         }
    //     }
    //     return locationName != null ? locationName : "未知";
    // }

    @Override
    @Transactional
    public UserNode allocateKey(Long userId, Long nodeId) {
        log.info("开始为用户 {} 在节点 {} 分配密钥", userId, nodeId);
        
        // 1. 检查user_node表是否已有该用户在该节点的配置
        QueryWrapper<UserNode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("node_id", nodeId);
        UserNode existingConfig = userNodeMapper.selectOne(queryWrapper);
        
        if (existingConfig != null) {
            log.info("用户 {} 在节点 {} 上已存在配置，直接返回。配置ID: {}", userId, nodeId, existingConfig.getId());
            return existingConfig;
        }
        
        // 2. 从wireguard_key_pool表中获取一个未分配的密钥对
        QueryWrapper<WireguardKeyPool> keyPoolQuery = new QueryWrapper<>();
        keyPoolQuery.eq("node_id", nodeId)
                   .eq("status", 0) // 未分配状态
                   .last("LIMIT 1");
        WireguardKeyPool availableKey = wireguardKeyPoolMapper.selectOne(keyPoolQuery);
        
        if (availableKey == null) {
            log.error("节点 {} 没有可用的密钥对", nodeId);
            throw new BizException("节点密钥池已耗尽，请联系管理员");
        }
        
        // 3. 创建新的user_node记录
        UserNode newConfig = UserNode.builder()
                .userId(userId)
                .nodeId(nodeId)
                .wgPeerPublicKey(availableKey.getPublicKey())
                .wgPeerPrivateKey(availableKey.getPrivateKey())
                .wgAllowedIps(availableKey.getAddress())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // 4. 保存到user_node表
        userNodeMapper.insert(newConfig);
        log.info("成功创建用户节点配置，ID: {}", newConfig.getId());
        
        // 5. 更新wireguard_key_pool记录为已分配状态
        availableKey.setStatus(1); // 已分配
        availableKey.setAssignedUserId(userId);
        availableKey.setAssignedAt(LocalDateTime.now());
        availableKey.setUpdatedAt(LocalDateTime.now());
        wireguardKeyPoolMapper.updateById(availableKey);
        
        log.info("成功为用户 {} 在节点 {} 分配密钥，密钥池记录ID: {}", userId, nodeId, availableKey.getId());
        
        return newConfig;
    }

    @Override
    @Transactional
    public void cleanDuplicateAddresses() {
        log.info("开始清理重复的IP地址");
        int cleanedCount = wireguardKeyPoolMapper.cleanDuplicateAddresses();
        log.info("成功清理 {} 条重复的IP地址记录", cleanedCount);
    }

    @Override
    @Transactional
    public void cleanAllKeysByNodeId(Long nodeId) {
        log.info("开始清理节点 {} 的所有密钥对", nodeId);
        int cleanedCount = wireguardKeyPoolMapper.cleanAllKeysByNodeId(nodeId);
        log.info("成功清理节点 {} 的 {} 条密钥对记录", nodeId, cleanedCount);
    }

    @Override
    public String generateWgConfigForNodeFromKeyPool(Long nodeId) {
        Node node = nodeMapper.selectById(nodeId);
        if (node == null) {
            log.error("无法生成配置: 未找到 ID 为 {} 的节点.", nodeId);
            return null;
        }
        if (node.getWgPrivateKey() == null || node.getWgPrivateKey().isEmpty()) {
            log.error("无法为节点 ID {} 生成配置: wgPrivateKey 缺失.", nodeId);
            return null;
        }
        if (node.getPort() == null) {
            log.error("无法为节点 ID {} 生成配置: port (ListenPort) 缺失.", nodeId);
            return null;
        }

        // 查询 wireguard_key_pool 表中该节点的所有已分配密钥
        QueryWrapper<WireguardKeyPool> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("node_id", nodeId);
                   //.eq("status", 1) // 1-已分配
                   //.isNotNull("assigned_user_id");
        List<WireguardKeyPool> allocatedKeys = wireguardKeyPoolMapper.selectList(queryWrapper);

        StringBuilder sb = new StringBuilder();
        // [Interface] 部分
        sb.append("[Interface]\n");
        sb.append("# Node: ").append(node.getName()).append(" (ID: ").append(nodeId).append(")\n");
        sb.append("# 注意: 此配置文件主要用于 wg syncconf 更新 Peers。接口IP地址 (Address) 和路由需通过其他方式管理。\n");
        sb.append("PrivateKey = ").append(node.getWgPrivateKey()).append("\n");
        sb.append("ListenPort = ").append(node.getPort()).append("\n");
        sb.append("\n");

        // [Peer] 部分
        if (allocatedKeys != null && !allocatedKeys.isEmpty()) {
            // 对 Peers 进行排序，以确保配置文件内容的顺序稳定性
            allocatedKeys.sort(Comparator.comparing(WireguardKeyPool::getAssignedUserId, Comparator.nullsLast(Comparator.naturalOrder())));

            for (WireguardKeyPool keyPool : allocatedKeys) {
                if (keyPool.getPublicKey() == null || keyPool.getPublicKey().isEmpty() ||
                        keyPool.getAddress() == null || keyPool.getAddress().isEmpty()) {
                    log.warn("跳过节点 ID {} 上的用户 ID {} 的 Peer，原因：缺少 PublicKey 或 Address.", nodeId, keyPool.getAssignedUserId());
                    continue;
                }
                sb.append("[Peer]\n");
                sb.append("# UserID: ").append(keyPool.getAssignedUserId()).append("\n");
                sb.append("PublicKey = ").append(keyPool.getPublicKey()).append("\n");
                sb.append("AllowedIPs = ").append(keyPool.getAddress()).append("\n");
                sb.append("\n");
            }
        }

        String configContent = sb.toString();
        log.info("成功生成节点 ID {} 的 WireGuard 配置内容. 包含 {} 个 Peer.", nodeId, 
                allocatedKeys != null ? allocatedKeys.size() : 0);
        log.debug("为节点 {} 生成的配置内容:\n{}", nodeId, configContent);
        
        return configContent;
    }
}