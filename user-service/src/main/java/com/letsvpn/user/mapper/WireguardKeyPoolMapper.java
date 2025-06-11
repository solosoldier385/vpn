package com.letsvpn.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.letsvpn.user.entity.WireguardKeyPool;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface WireguardKeyPoolMapper extends BaseMapper<WireguardKeyPool> {

    /**
     * 根据节点ID查询可用的密钥对数量
     */
    @Select("SELECT COUNT(*) FROM wireguard_key_pool WHERE node_id = #{nodeId} AND status = 0")
    Long countAvailableKeysByNodeId(@Param("nodeId") Long nodeId);

    /**
     * 根据节点ID获取一个可用的密钥对
     */
    @Select("SELECT * FROM wireguard_key_pool WHERE node_id = #{nodeId} AND status = 0 LIMIT 1")
    WireguardKeyPool getAvailableKeyByNodeId(@Param("nodeId") Long nodeId);

    /**
     * 根据节点ID获取所有可用的密钥对
     */
    @Select("SELECT * FROM wireguard_key_pool WHERE node_id = #{nodeId} AND status = 0")
    List<WireguardKeyPool> getAvailableKeysByNodeId(@Param("nodeId") Long nodeId);

    /**
     * 分配密钥对给用户
     */
    @Update("UPDATE wireguard_key_pool SET status = 1, assigned_user_id = #{userId}, assigned_at = NOW() WHERE id = #{keyId} AND status = 0")
    int assignKeyToUser(@Param("keyId") Long keyId, @Param("userId") Long userId);

    /**
     * 释放密钥对（取消分配）
     */
    @Update("UPDATE wireguard_key_pool SET status = 0, assigned_user_id = NULL, assigned_at = NULL WHERE id = #{keyId} AND status = 1")
    int releaseKey(@Param("keyId") Long keyId);

    /**
     * 根据用户ID查询已分配的密钥对
     */
    @Select("SELECT * FROM wireguard_key_pool WHERE assigned_user_id = #{userId} AND status = 1")
    List<WireguardKeyPool> getAssignedKeysByUserId(@Param("userId") Long userId);

    /**
     * 根据节点ID和用户ID查询密钥对
     */
    @Select("SELECT * FROM wireguard_key_pool WHERE node_id = #{nodeId} AND assigned_user_id = #{userId} AND status = 1 LIMIT 1")
    WireguardKeyPool getKeyByNodeIdAndUserId(@Param("nodeId") Long nodeId, @Param("userId") Long userId);

    /**
     * 批量插入密钥对
     */
    int batchInsert(@Param("keys") List<WireguardKeyPool> keys);

    /**
     * 清理过期的密钥对（状态为已失效的）
     */
    @Update("DELETE FROM wireguard_key_pool WHERE status = 2 AND updated_at < DATE_SUB(NOW(), INTERVAL 30 DAY)")
    int cleanExpiredKeys();

    /**
     * 清理重复的IP地址（保留ID最小的记录）
     */
    @Delete("DELETE w1 FROM wireguard_key_pool w1 " +
            "INNER JOIN wireguard_key_pool w2 " +
            "WHERE w1.node_id = w2.node_id " +
            "AND w1.address = w2.address " +
            "AND w1.id > w2.id")
    int cleanDuplicateAddresses();

    /**
     * 清理指定节点的所有密钥对
     */
    @Delete("DELETE FROM wireguard_key_pool WHERE node_id = #{nodeId}")
    int cleanAllKeysByNodeId(@Param("nodeId") Long nodeId);
} 