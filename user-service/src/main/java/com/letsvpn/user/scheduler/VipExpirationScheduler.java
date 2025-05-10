// 文件路径: user-service/src/main/java/com/letsvpn/user/scheduler/VipExpirationScheduler.java
package com.letsvpn.user.scheduler;

import com.letsvpn.user.service.UserVipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class VipExpirationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(VipExpirationScheduler.class);

    @Autowired
    private UserVipService userVipService;

    // 从配置文件读取cron表达式，提供默认值
    @Value("${app.scheduler.vip-expiration-check.cron:0 0 1 * * ?}") // 默认每天凌晨1点
    private String vipExpirationCheckCron;

    @Scheduled(cron = "${app.scheduler.vip-expiration-check.cron:0 0 1 * * ?}")
    public void processExpiredVipsScheduled() {
        logger.info("执行定时任务：检查并处理过期的VIP (cron: {})...", vipExpirationCheckCron);
        try {
            userVipService.checkAndProcessExpiredVips();
            logger.info("定时任务：过期VIP检查处理完成。");
        } catch (Exception e) {
            logger.error("定时任务：处理过期VIP时发生错误。", e);
        }
    }
}