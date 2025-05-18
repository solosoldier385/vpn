package com.letsvpn.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.letsvpn.common.core.exception.BizException;
import com.letsvpn.common.data.entity.User;
import com.letsvpn.common.data.mapper.UserMapper;
import com.letsvpn.user.entity.Node;
import com.letsvpn.user.enums.VipLevel;
import com.letsvpn.user.mapper.NodeMapper;
import com.letsvpn.user.service.NodeService;
import com.letsvpn.user.service.UserService;
import com.letsvpn.user.service.WireGuardConfigService;
import com.letsvpn.user.vo.UserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NodeService nodeService; // 保持注入，因为 NodeService 可能有其他职责

    @Autowired
    private NodeMapper nodeMapper; // 新增注入 NodeMapper 用于查询免费节点

    @Autowired
    private WireGuardConfigService wireGuardConfigService; // 新增注入 WireGuardConfigService


    @Override
    public User findByUsername(String username) {
        return baseMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }



    @Override
    @Transactional // 建议对涉及多个数据库写操作的方法使用事务
    public void initializeNewUser(String userId, String username) {
        // 检查用户是否已存在 (通过 auth-service 传递过来的 userId，它应该是全局唯一的)
        User user = userMapper.selectById(userId);


        // 为新创建的、vipLevel为NO_VIP的用户分配免费节点
        // user 对象是从 auth-service 传来的，应该已经设置了 vipLevel = 0 (VipLevel.NO_VIP.getLevel())
        if (user.getLevel() != null && user.getLevel().equals(VipLevel.FREE.getCode())) {
            log.info("用户 {} 是新注册的普通用户 (VIP级别 {})，开始分配免费节点。", user.getId(), user.getLevel());

            QueryWrapper<Node> freeNodesQuery = new QueryWrapper<>();
            freeNodesQuery.lambda().eq(Node::getLevelRequired, VipLevel.FREE.getCode());
            List<Node> freeNodes = nodeMapper.selectList(freeNodesQuery);

            if (freeNodes.isEmpty()) {
                log.info("系统中没有配置免费节点，用户 {} 无免费节点可分配。", user.getId());
            } else {
                log.info("发现 {} 个免费节点，正在为用户 {} 分配...", freeNodes.size(), user.getId());
                for (Node freeNode : freeNodes) {
                    try {
                        // 调用现有的密钥分配逻辑
                        // createUserNodeConfig 内部应能处理用户已拥有此节点配置的情况 (幂等性)
                        log.debug("为用户 {} 分配免费节点 ID: {}", user.getId(), freeNode.getId());
                        wireGuardConfigService.assignOrGetUserNodeConfig(user.getId(), freeNode.getId());
                        log.info("成功为用户 {} 分配免费节点 ID: {}", user.getId(), freeNode.getId());
                    } catch (Exception e) {
                        // 记录错误，但继续尝试分配其他免费节点
                        log.error("为用户 {} 分配免费节点 ID: {} 时发生错误: {}", user.getId(), freeNode.getId(), e.getMessage(), e);
                    }
                }
            }
        } else {
            log.info("用户 {} (VIP级别: {}) 不是新注册的普通用户，或VIP级别未正确设置，跳过自动分配免费节点流程。",
                    user.getId(), user.getLevel());
        }
    }






}
