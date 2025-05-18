package com.letsvpn.common.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.letsvpn.common.data.entity.UserNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Optional;

@Mapper
public interface UserNodeMapper extends BaseMapper<UserNode> {

    // 根据 userId 和 nodeId 查询配置 (用于判断是否存在/更新)
    // 使用 Optional 避免空指针，需要 Java 8+
    // 如果不用 Optional，返回 UserNodeConfig 并判断 null
    @Select("SELECT * FROM user_node WHERE user_id = #{userId} AND node_id = #{nodeId} LIMIT 1")
    Optional<UserNode> findByUserIdAndNodeId(@Param("userId") Long userId, @Param("nodeId") Long nodeId);

    // 查询某个节点下所有已分配的 IP (用于 IPAM 分配新 IP)
    @Select("SELECT wg_allowed_ips FROM user_node WHERE node_id = #{nodeId}")
    List<String> findAllocatedIpsByNodeId(@Param("nodeId") Long nodeId);


}