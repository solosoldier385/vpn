package com.letsvpn.user.service;

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
}