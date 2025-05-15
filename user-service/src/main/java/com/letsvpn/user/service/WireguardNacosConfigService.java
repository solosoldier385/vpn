package com.letsvpn.user.service;

public interface WireguardNacosConfigService {

    /**
     * 为指定的WireGuard节点生成其完整的配置，并发布到Nacos。
     * 配置内容包括节点的[Interface]部分和所有活动用户的[Peer]部分。
     *
     * @param nodeId 要发布配置的节点ID
     * @return boolean true 如果发布成功, false 如果失败
     * @throws Exception 如果过程中发生错误
     */
    boolean publishConfigForNode(Long nodeId) throws Exception;

    /**
     * 从Nacos中删除指定WireGuard节点的配置。
     * （可选功能，例如节点下线时使用）
     * @param nodeId 要删除配置的节点ID
     * @return boolean true 如果删除成功, false 如果失败
     * @throws Exception 如果过程中发生错误
     */
    // boolean removeConfigForNode(Long nodeId) throws Exception; // 可选
}