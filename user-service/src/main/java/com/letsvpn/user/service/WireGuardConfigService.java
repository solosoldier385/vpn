package com.letsvpn.user.service;

import com.letsvpn.common.data.entity.UserNode;
import com.letsvpn.user.vo.UserNodeVO;

import java.util.List;
import java.util.Set;

public interface WireGuardConfigService {
    /**
     * 为指定用户和节点生成 WireGuard 客户端配置.
     * @param username 用户名
     * @param nodeId   节点 ID
     * @return WireGuard 配置文件的内容 (字符串)
     * @throws IllegalArgumentException 如果请求无效 (用户/节点不存在, 无权限, 无法分配 IP 等)
     * @throws RuntimeException 如果发生内部错误 (密钥生成/节点更新失败等)
     */
    String generateClientConfig(String username, Long nodeId);

    UserNode assignOrGetUserNodeConfig(Long userId, Long nodeId);

    /**
     * 根据用户ID获取该用户的所有节点配置列表 (返回VO)。
     *
     * @param userId 用户ID
     * @return 该用户的 UserNodeConfigVO 列表，如果没有则返回空列表。
     */
    List<UserNodeVO> getUserNodeConfigsByUserId(Long userId);


    /**
     * 为所有活跃节点生成密钥对
     * @param batchSizePerNode 每个节点生成的密钥对数量
     */
    void generateKeyPairsForAllNodes(int batchSizePerNode);

    /**
     * 分配密钥
     * 检查表user_node，是否有了这个user_id这个node_id对应的一套密钥，如果没有，
     * 从wireguard_key_pool表中拿一个未分配的到user_node，这个过程称为分配密钥，
     * 写入到user_node表之后，把拿出的wireguard_key_pool记录update为已分配
     * 
     * @param userId 用户ID
     * @param nodeId 节点ID
     * @return 分配的用户节点配置
     */
    UserNode allocateKey(Long userId, Long nodeId);

    /**
     * 清理重复的IP地址
     */
    void cleanDuplicateAddresses();

    /**
     * 清理指定节点的所有密钥对
     * @param nodeId 节点ID
     */
    void cleanAllKeysByNodeId(Long nodeId);

    /**
     * 根据节点ID查询 wireguard_key_pool 表中的所有数据并生成完整的 wg0.conf 配置文件内容
     * @param nodeId 节点ID
     * @return 完整的 wg0.conf 配置文件内容
     */
    String generateWgConfigForNodeFromKeyPool(Long nodeId);

    /**
     * 获取节点所有已占用的IP地址
     * @param nodeId 节点ID
     * @return 已占用的IP地址集合
     */
    Set<String> getOccupiedIpsForNode(Long nodeId);

    /**
     * 为指定节点生成指定数量的密钥对
     * @param nodeId 节点ID
     * @param batchSize 生成数量
     * @return 实际生成的数量
     */
    int generateKeyPairsForNode(Long nodeId, int batchSize);

    int allocateKeyPairforNode(Long nodeId, int batchSize);
}