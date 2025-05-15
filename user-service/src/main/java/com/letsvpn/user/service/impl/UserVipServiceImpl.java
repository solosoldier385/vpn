// 文件路径: user-service/src/main/java/com/letsvpn/user/service/impl/UserVipServiceImpl.java
package com.letsvpn.user.service.impl;

import com.letsvpn.common.core.exception.BizException;
import com.letsvpn.common.data.entity.User;
import com.letsvpn.common.data.entity.UserNode;
import com.letsvpn.common.data.mapper.UserMapper;
import com.letsvpn.user.dto.ActivateVipSubscriptionRequest;
import com.letsvpn.user.dto.ActivateVipSubscriptionResponse;
import com.letsvpn.user.dto.CurrentUserVipProfileResponse;
import com.letsvpn.user.entity.Node;
import com.letsvpn.user.entity.UserSubscription;
import com.letsvpn.user.enums.VipLevel;
import com.letsvpn.user.mapper.NodeMapper;
import com.letsvpn.user.mapper.SubscriptionPlanMapper;
import com.letsvpn.user.mapper.UserSubscriptionMapper;
import com.letsvpn.user.service.UserVipService;
import com.letsvpn.user.service.WireGuardConfigService;
import com.letsvpn.user.service.WireguardNacosConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper; // 引入QueryWrapper
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper; // 引入UpdateWrapper


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class UserVipServiceImpl implements UserVipService {

    private static final Logger log = LoggerFactory.getLogger(UserVipServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NodeMapper nodeMapper;

    @Autowired
    private UserSubscriptionMapper userSubscriptionMapper;

    @Autowired
    private SubscriptionPlanMapper subscriptionPlanMapper; // 确保注入

    @Autowired
    private WireGuardConfigService wireGuardConfigService;

    @Autowired
    private WireguardNacosConfigService wireguardNacosConfigService;


    @Override
    public CurrentUserVipProfileResponse getCurrentUserVipProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在: " + userId);
        }

        VipLevel currentLevel = VipLevel.getByCode(user.getLevel());
        LocalDateTime now = LocalDateTime.now();
        boolean isActiveVip = false;
        long daysRemaining = 0;

        if (user.getVipExpireTime() != null && user.getVipExpireTime().isAfter(now)) {
            if (currentLevel != VipLevel.FREE) {
                isActiveVip = true;
                daysRemaining = Duration.between(now, user.getVipExpireTime()).toDays();
                if (daysRemaining < 0) daysRemaining = 0; // 避免显示负数天（尽管isAfter已处理）
                // 如果需要更精确到小时/分钟，可以使用 Duration 对象的其他方法
            }
        } else {
            // VIP已过期或从未是VIP
            if (currentLevel != VipLevel.FREE) {
                // 如果数据库中记录的等级不是免费但已过期，个人资料中应显示为免费
                currentLevel = VipLevel.FREE;
                // checkAndProcessExpiredVips 任务会负责更新数据库中的过期状态
            }
        }
        // 如果 level 字段本身就是 FREE，则 isActiveVip 为 false
        if (VipLevel.getByCode(user.getLevel()) == VipLevel.FREE) {
            isActiveVip = false;
            daysRemaining = 0; // 免费用户无剩余天数概念
        }


        return CurrentUserVipProfileResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .currentVipLevel(currentLevel)
                .vipLevelDescription(currentLevel.getDescription())
                .vipExpireTime(user.getVipExpireTime())
                .isActiveVip(isActiveVip)
                .daysRemaining(daysRemaining)
                .build();
    }

    @Override
    @Transactional
    public ActivateVipSubscriptionResponse activateVipSubscription(ActivateVipSubscriptionRequest request) {
        log.info("开始激活用户VIP订阅：userId={}, planId={}, orderId={}", request.getUserId(), request.getPlanId(), request.getOrderId());

        User user = userMapper.selectById(request.getUserId());
        if (user == null) {
            log.error("激活VIP失败：用户不存在, userId={}", request.getUserId());
            throw new BizException("用户不存在: " + request.getUserId());
        }

        // 校验 targetVipLevelCode 是否有效
        VipLevel targetVipLevel = VipLevel.getByCode(request.getTargetVipLevelCode());
        if (targetVipLevel == VipLevel.FREE && request.getPlanDurationDays() > 0) {
            // 通常付费套餐不应将用户等级设置为免费
            log.error("激活VIP失败：目标VIP等级配置错误。planId={}, targetVipLevelCode={}", request.getPlanId(), request.getTargetVipLevelCode());
            throw new BizException("套餐对应的VIP等级配置无效");
        }

        LocalDateTime now = LocalDateTime.now(); // 理论上应该使用 request.getPaymentTime()作为计算基准
        LocalDateTime paymentTime = request.getPaymentTime();
        LocalDateTime currentVipExpireTime = user.getVipExpireTime();

        LocalDateTime newStartTime;
        // 如果用户当前不是VIP(等级为免费或等级不是免费但已过期), 或者首次购买
        if (user.getLevel() == null || Objects.equals(user.getLevel(), VipLevel.FREE.getCode()) ||
                (currentVipExpireTime != null && currentVipExpireTime.isBefore(paymentTime))) {
            newStartTime = paymentTime; // VIP从支付成功时刻开始计算
        } else {
            // 用户已经是VIP且未过期，则在原到期时间基础上续期
            newStartTime = currentVipExpireTime;
        }
        LocalDateTime newVipExpireTime = newStartTime.plusDays(request.getPlanDurationDays());

        user.setLevel(request.getTargetVipLevelCode());
        user.setVipExpireTime(newVipExpireTime);
        user.setUpdateTime(now); // 更新用户表的修改时间
        userMapper.updateById(user);
        log.info("用户表已更新：userId={}, newLevel={}, newExpireTime={}", user.getId(), request.getTargetVipLevelCode(), newVipExpireTime);

        UserSubscription subscription = new UserSubscription();
        subscription.setUserId(user.getId());
        subscription.setPlanId(request.getPlanId());
        subscription.setStartTime(newStartTime); // 记录实际的VIP周期开始时间
        subscription.setEndTime(newVipExpireTime);
        subscription.setStatus(1); // 激活
        // 这里可以考虑把 orderId 也存入 user_subscription 表，方便追踪
        userSubscriptionMapper.insert(subscription);
        log.info("用户订阅记录已创建：userSubscriptionId={}, userId={}, planId={}", subscription.getId(), user.getId(), request.getPlanId());

        // --- New logic: Provision user on nodes and update Nacos ---
        log.info("Attempting to provision user {} on relevant nodes and update Nacos.", user.getId());
        try {
            QueryWrapper<Node> nodeQueryWrapper = new QueryWrapper<>();
            nodeQueryWrapper.eq("status", 0);
            nodeQueryWrapper.eq("is_free",0);

            // 0 for active nodes (assuming 0 means active)

            // The assignOrGetUserNodeConfig method in WireGuardConfigServiceImpl already handles
            // permission checks (user level vs node.levelRequired), so we can get all active nodes.
            List<Node> allActiveNodes = nodeMapper.selectList(nodeQueryWrapper);

            if (allActiveNodes.isEmpty()) {
                log.info("No active nodes found to provision for user {}.", user.getId());
            } else {
                log.info("Found {} active nodes. Processing for user {}.", allActiveNodes.size(), user.getId());
            }

            for (Node node : allActiveNodes) {
                try {
                    log.info("Processing node ID: {} (Name: {}) for user ID: {}", node.getId(), node.getName(), user.getId());
                    // This method will create UserNode if not exists (generating keys, IP)
                    // or update existing one. It also contains permission logic.
                    UserNode userNodeConfig = wireGuardConfigService.assignOrGetUserNodeConfig(user.getId(), node.getId());

                    if (userNodeConfig != null && Boolean.TRUE.equals(userNodeConfig.getIsActive())) {
                        log.info("Successfully assigned/updated user {} on node {}. UserNodeConfig ID: {}. Attempting to publish Nacos config.", user.getId(), node.getId(), userNodeConfig.getId());
                        boolean nacosSuccess = wireguardNacosConfigService.publishConfigForNode(node.getId());
                        if (nacosSuccess) {
                            log.info("Nacos configuration published successfully for node ID: {}", node.getId());
                        } else {
                            log.warn("Failed to publish Nacos configuration for node ID: {}. Manual check might be needed.", node.getId());
                            // Depending on strictness, you might want to collect these errors
                        }
                    } else if (userNodeConfig == null) {
                        log.warn("User {} could not be assigned to node {} (assignOrGetUserNodeConfig returned null, possibly due to permissions or node setup). Skipping Nacos update for this node.", user.getId(), node.getId());
                    } else { // userNodeConfig not null but not active
                        log.warn("User {} assignment to node {} resulted in an inactive UserNodeConfig (ID: {}). Skipping Nacos update for this node.", user.getId(), node.getId(), userNodeConfig.getId());
                    }
                } catch (BizException e) {
                    // BizException from assignOrGetUserNodeConfig usually means user doesn't have permission for this node,
                    // or node is not configured properly for assignment. This is often an expected scenario for some nodes.
                    log.warn("Business exception while processing node ID: {} for user ID: {}. Message: '{}'. This node may not be applicable or accessible for the user.", node.getId(), user.getId(), e.getMessage());
                } catch (Exception e) {
                    // Catch other unexpected exceptions during node processing or Nacos publishing.
                    log.error("Unexpected error processing node ID: {} for user ID: {}. Nacos update for this node might be skipped.", node.getId(), user.getId(), e);
                    // Log and continue, or collect errors to indicate partial failure.
                }
            }
            log.info("Finished processing nodes for user {}.", user.getId());
        } catch (Exception e) {
            log.error("An error occurred during the node provisioning and Nacos update phase for user {}: {}", user.getId(), e.getMessage(), e);
            // This is an error in the overall node processing logic (e.g., fetching allActiveNodes).
            // The main VIP activation is still considered successful at this point.
        }
        // --- End of new logic ---


        return ActivateVipSubscriptionResponse.builder()
                .success(true)
                .message("VIP激活成功")
                .userId(user.getId())
                .updatedVipLevelCode(user.getLevel())
                .newVipExpireTime(user.getVipExpireTime())
                .build();
    }

    @Override
    @Transactional
    public void checkAndProcessExpiredVips() {
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("vip_expire_time")
                .lt("vip_expire_time", now)
                .ne("level", VipLevel.FREE.getCode());

        List<User> expiredUsers = userMapper.selectList(queryWrapper);

        if (expiredUsers != null && !expiredUsers.isEmpty()) {
            for (User user : expiredUsers) {
                log.info("处理过期VIP用户：userId={}, username={}, currentLevel={}, expireTime={}",
                        user.getId(), user.getUsername(), user.getLevel(), user.getVipExpireTime());
                Integer previousLevel = user.getLevel();
                user.setLevel(VipLevel.FREE.getCode());
                // user.setVipExpireTime(null); // 可选：是否清除过期时间，或保留
                user.setUpdateTime(now);
                userMapper.updateById(user);
                log.info("用户 {} ({}) 的VIP等级已从 {} 重置为免费。", user.getId(), user.getUsername(), previousLevel);

                // 更新 user_subscription 表中对应的记录状态为失效
                UpdateWrapper<UserSubscription> subUpdateWrapper = new UpdateWrapper<>();
                subUpdateWrapper.eq("user_id", user.getId())
                        .eq("status", 1) // 找到仍然标记为激活的
                        .lt("end_time", now); // 且其结束时间确实已过 (理论上上面的用户查询已筛选)
                UserSubscription subToExpire = new UserSubscription();
                subToExpire.setStatus(0); // 设置为失效
                int updatedSubs = userSubscriptionMapper.update(subToExpire, subUpdateWrapper);
                if (updatedSubs > 0) {
                    log.info("已更新用户 {} 的 {} 条过期订阅记录状态为失效。", user.getId(), updatedSubs);
                }
            }
            log.info("已处理 {} 个用户的过期VIP。", expiredUsers.size());
        } else {
            log.info("没有找到需要处理的过期VIP用户。");
        }
    }
}