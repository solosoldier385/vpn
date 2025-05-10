// 文件路径: user-service/src/main/java/com/letsvpn/user/service/impl/UserVipServiceImpl.java
package com.letsvpn.user.service.impl;

import com.letsvpn.common.core.exception.BizException;
import com.letsvpn.common.data.entity.User;
import com.letsvpn.common.data.mapper.UserMapper;
import com.letsvpn.user.dto.ActivateVipSubscriptionRequest;
import com.letsvpn.user.dto.ActivateVipSubscriptionResponse;
import com.letsvpn.user.dto.CurrentUserVipProfileResponse;
import com.letsvpn.user.entity.UserSubscription;
import com.letsvpn.user.enums.VipLevel;
import com.letsvpn.user.mapper.SubscriptionPlanMapper;
import com.letsvpn.user.mapper.UserSubscriptionMapper;
import com.letsvpn.user.service.UserVipService;
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
    private UserSubscriptionMapper userSubscriptionMapper;

    @Autowired
    private SubscriptionPlanMapper subscriptionPlanMapper; // 确保注入


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