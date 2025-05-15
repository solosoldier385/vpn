package com.letsvpn.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.common.core.exception.BizException;
import com.letsvpn.user.dto.NodeRegistrationRequest;
import com.letsvpn.user.dto.NodeUpdateRequest;
import com.letsvpn.user.entity.Node;
import com.letsvpn.user.mapper.NodeMapper;
import com.letsvpn.user.service.NodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NodeServiceImpl implements NodeService {

    private final NodeMapper nodeMapper;

    @Override
    @Transactional
    public Node createNode(NodeRegistrationRequest request) {
        log.info("收到创建新节点请求: {}", request.getName());

        // 检查节点是否已存在 (例如，根据 IP 和端口)
        QueryWrapper<Node> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ip", request.getIp()).eq("port", request.getPort());
        if (nodeMapper.selectCount(queryWrapper) > 0) {
            log.warn("节点已存在 (IP: {}, Port: {})", request.getIp(), request.getPort());
            throw new BizException("节点已存在 (IP: " + request.getIp() + ", Port: " + request.getPort() + ")");
        }

        Node node = new Node();
        BeanUtils.copyProperties(request, node); // 从 DTO 复制属性

        // 设置默认值或后端控制的值
        node.setStatus(0); // 默认状态为 0 (正常)
        node.setCreatedAt(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());

        try {
            int result = nodeMapper.insert(node);
            if (result > 0 && node.getId() != null) {
                log.info("新节点注册成功，ID: {}", node.getId());
                return node; // 返回包含ID的完整Node对象
            } else {
                log.error("节点插入数据库失败，没有返回 ID 或影响行数为 0");
                throw new BizException("节点注册失败，无法保存到数据库");
            }
        } catch (Exception e) {
            log.error("注册节点时发生数据库异常", e);
            throw new BizException("节点注册失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Node updateNode(Long nodeId, NodeUpdateRequest request, String requestIp) { // 添加 requestIp 参数
        log.info("尝试从IP: {} 更新节点ID: {}", requestIp, nodeId);

        // 1. IP 地址校验
        List<String> allowedIps = nodeMapper.selectAllNodeIps();
        if (allowedIps == null || allowedIps.isEmpty()) {
            // 如果数据库中没有任何节点IP（例如系统刚初始化，还没有任何节点），
            // 这里的行为需要根据业务定义：是拒绝所有更新，还是允许特定初始配置IP？
            // 当前实现为：如果没有配置任何节点IP，则拒绝所有来自外部的更新请求。
            log.warn("数据库中未找到任何已注册的节点IP。拒绝来自 {} 的更新请求。", requestIp);
            throw new BizException(HttpStatus.FORBIDDEN.value(), "访问被拒绝：系统中未配置授权IP。");
        }

        if (!allowedIps.contains(requestIp)) {
            log.warn("未授权的IP地址 {} 尝试更新节点ID: {}。允许的IP列表: {}", requestIp, nodeId, allowedIps);
            throw new BizException(HttpStatus.FORBIDDEN.value(), "访问被拒绝：您的IP地址 (" + requestIp + ") 未被授权执行此操作。");
        }

        log.info("IP地址 {} 已授权。继续处理节点ID: {} 的更新请求。", requestIp, nodeId);

        // 2. 获取并更新节点信息 (现有逻辑)
        Node node = nodeMapper.selectById(nodeId);
        if (node == null) {
            log.warn("尝试更新不存在的节点，ID: {}", nodeId);
            throw new BizException(HttpStatus.NOT_FOUND.value(), "未找到ID为 " + nodeId + " 的节点");
        }

        log.info("开始更新节点 ID: {} 的信息", nodeId);
        boolean updated = false;

        if (request.getName() != null) {
            node.setName(request.getName());
            updated = true;
        }
        if (request.getIp() != null) {
            // 如果允许通过此接口修改节点自身的IP地址，需要注意：
            // 修改后，下一次该节点再调用此接口时，其自身的IP（旧IP）可能就不在允许列表中了。
            // 除非修改IP的同时，也确保新的IP能被识别，或者此IP检查是针对“管理节点”的IP。
            // 当前逻辑是：请求方IP必须是 *当前*数据库中任一节点的IP。
            node.setIp(request.getIp());
            updated = true;
        }
        if (request.getPort() != null) {
            node.setPort(request.getPort());
            updated = true;
        }
        if (request.getLevelRequired() != null) {
            node.setLevelRequired(request.getLevelRequired());
            updated = true;
        }
        if (request.getIsFree() != null) {
            node.setIsFree(request.getIsFree());
            updated = true;
        }
        if (request.getStatus() != null) {
            node.setStatus(request.getStatus());
            updated = true;
        }
        if (request.getWgAddress() != null) {
            node.setWgAddress(request.getWgAddress());
            updated = true;
        }
        if (request.getWgPublicKey() != null) {
            node.setWgPublicKey(request.getWgPublicKey());
            updated = true;
        }
        if (request.getWgDns() != null) {
            node.setWgDns(request.getWgDns());
            updated = true;
        }

        if (updated) {
            node.setUpdatedAt(LocalDateTime.now());
            int updatedRows = nodeMapper.updateById(node);
            if (updatedRows > 0) {
                log.info("节点 ID: {} 信息更新成功", nodeId);
            } else {
                log.error("更新节点 ID: {} 信息时数据库操作未影响任何行", nodeId);
                throw new BizException("更新节点信息失败，请重试");
            }
        } else {
            log.info("节点 ID: {} 没有需要更新的字段", nodeId);
        }
        return node;
    }
}