package com.letsvpn.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.letsvpn.user.entity.Node;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NodeMapper extends BaseMapper<Node> {

    @Select("SELECT * FROM node WHERE is_free = 1 AND status = 0 LIMIT 1")
    Node selectFreeNode();

    @Select("SELECT * FROM node WHERE level_required <= #{level} AND status = 0")
    List<Node> selectByMinLevel(@Param("level") int level);

    /**
     * 查询node表中所有不重复的、非空且非空字符串的IP地址。
     * @return IP地址列表
     */
    @Select("SELECT DISTINCT ip FROM node WHERE ip IS NOT NULL AND ip != ''")
    List<String> selectAllNodeIps();

    /**
     * 查询所有付费且状态正常的节点。
     * @return 付费节点列表
     */
    @Select("SELECT * FROM node WHERE is_free = 0 AND status = 0") // is_free = 0 代表付费, status = 0 代表正常
    List<Node> selectPaidActiveNodes();
}
