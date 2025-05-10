package com.letsvpn.user.service;

import com.letsvpn.common.data.entity.UserNodeConfig;
import com.letsvpn.user.vo.UserNodeConfigVO;

import java.util.List;

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

    UserNodeConfig assignOrGetUserNodeConfig(Long userId, Long nodeId);

    /**
     * 根据用户ID获取该用户的所有节点配置列表 (返回VO)。
     *
     * @param userId 用户ID
     * @return 该用户的 UserNodeConfigVO 列表，如果没有则返回空列表。
     */
    List<UserNodeConfigVO> getUserNodeConfigsByUserId(Long userId);

}