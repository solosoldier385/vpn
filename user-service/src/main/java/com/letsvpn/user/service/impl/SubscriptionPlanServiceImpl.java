// 文件路径: user-service/src/main/java/com/letsvpn/user/service/impl/SubscriptionPlanServiceImpl.java
package com.letsvpn.user.service.impl;

import com.letsvpn.common.core.exception.BizException;
import com.letsvpn.user.dto.VipPlanClientViewResponse;
import com.letsvpn.user.entity.SubscriptionPlan;
import com.letsvpn.user.mapper.SubscriptionPlanMapper;
import com.letsvpn.user.service.SubscriptionPlanService;
// import org.springframework.beans.BeanUtils; // 不再需要这个
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionPlanServiceImpl.class);

    @Autowired
    private SubscriptionPlanMapper planMapper;

    @Override
    public List<VipPlanClientViewResponse> getActiveDisplayPlans() {
        QueryWrapper<SubscriptionPlan> queryWrapper = new QueryWrapper<>();
        // 假设我们只展示状态为1 (可用) 的套餐
        queryWrapper.eq("status", 1); // 你需要在SubscriptionPlan实体和数据库表中添加status字段
        queryWrapper.orderByAsc("price"); // 例如按价格升序排列
        List<SubscriptionPlan> plans = planMapper.selectList(queryWrapper);

        if (plans == null || plans.isEmpty()) {
            log.info("没有找到可用的VIP套餐。");
            return Collections.emptyList();
        }

        return plans.stream().map(plan -> {
            // 手动将 SubscriptionPlan 的属性映射到 VipPlanClientViewResponse
            VipPlanClientViewResponse.VipPlanClientViewResponseBuilder builder = VipPlanClientViewResponse.builder();

            builder.planId(plan.getId());
            builder.name(plan.getName()); // 内部名称
            builder.displayName(plan.getDisplayName()); // 展示给用户的名称
            builder.price(plan.getPrice());
            builder.currency(plan.getCurrency());
            builder.durationDays(plan.getDurationDays());
            builder.durationDescription(plan.getDurationDays() + "天"); // 简单的时长描述，可以从数据库字段读取或更复杂逻辑生成

            // benefitsDescription 应该来自数据库字段 plan.getBenefitsDescription()
            // 如果 plan.getDescription() 是详细权益，而 benefitsDescription 是摘要，确保正确映射
            builder.benefitsDescription(plan.getBenefitsDescription() != null ? plan.getBenefitsDescription() : plan.getDescription());

            builder.originalPrice(plan.getOriginalPrice());
            builder.tag(plan.getTag());

            // Boolean recommended; 处理 boolean
            // 确保 plan.getRecommended() 返回的是 Boolean 对象，如果数据库是 tinyint(1)
            // MyBatis Plus 通常能正确映射 tinyint(1) 到 Boolean
            builder.recommended(Boolean.TRUE.equals(plan.getRecommended()));

            // pricePerDayInfo 应该来自数据库字段 plan.getPricePerDayInfo()
            // 或者在这里进行计算，例如：
            // if (plan.getPrice() != null && plan.getDurationDays() != null && plan.getDurationDays() > 0) {
            //     BigDecimal pricePerDay = plan.getPrice().divide(new BigDecimal(plan.getDurationDays()), 2, RoundingMode.HALF_UP);
            //     builder.pricePerDayInfo("约" + plan.getCurrency() + pricePerDay + "/天");
            // } else {
            //     builder.pricePerDayInfo(plan.getPricePerDayInfo()); // 使用数据库中的值
            // }
            builder.pricePerDayInfo(plan.getPricePerDayInfo());


            // 确保 associatedVipLevelCode 有值
            if (plan.getAssociatedVipLevelCode() == null) {
                log.warn("套餐 '{}' (ID:{}) 未配置 associatedVipLevelCode! 这可能导致客户端无法正确识别购买后的等级。",
                        plan.getName(), plan.getId());
                // 对于客户端视图，如果这个字段是必须的，可能需要抛出异常或过滤掉这个套餐
                // 或者 DTO 中此字段设为 Integer 而不是 int，以允许 null
            }
            builder.associatedVipLevelCode(plan.getAssociatedVipLevelCode());

            return builder.build();
        }).collect(Collectors.toList());
    }

    @Override
    public SubscriptionPlan getPlanDetailsForOrder(Long planId) {
        SubscriptionPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            log.warn("尝试获取套餐详情失败：套餐不存在, planId={}", planId);
            throw new BizException("套餐不存在: " + planId);
        }
        // 确保套餐是有效的，例如有价格、时长、关联的VIP等级代码，并且状态是可用
        if (plan.getStatus() == null || plan.getStatus() != 1) { // 假设 1 代表可用
            log.warn("尝试获取套餐详情失败：套餐 {} (ID:{}) 当前不可用 (status: {})", plan.getName(), planId, plan.getStatus());
            throw new BizException("所选套餐当前不可用");
        }
        if (plan.getPrice() == null || plan.getDurationDays() == null || plan.getDurationDays() <= 0 || plan.getAssociatedVipLevelCode() == null) {
            log.warn("尝试获取套餐详情失败：套餐 {} (ID:{}) 配置不完整或无效。", plan.getName(), planId);
            throw new BizException("套餐配置不完整或无效: " + planId);
        }
        return plan;
    }
}