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
}
