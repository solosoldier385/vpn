package com.letsvpn.user.task;

import com.letsvpn.user.service.WireGuardConfigService;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WireguardKeyPoolTask {

    @Autowired
    private WireGuardConfigService wireGuardConfigService;

    @Value("${wireguard.key-pool.batch-size:100}")
    private int batchSize;

    @Value("${wireguard.key-pool.min-threshold:20}")
    private int minThreshold;

    /**
     * 每周一凌晨4点执行
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 4 ? * MON")
    public void replenishKeyPool() {
        log.info("开始执行WireGuard密钥池补充任务");
        try {
            //wireGuardConfigService.generateKeyPairsForAllNodes(batchSize);
            log.info("WireGuard密钥池补充任务完成");
        } catch (Exception e) {
            log.error("WireGuard密钥池补充任务执行失败", e);
        }
    }

    /**
     * 手动调用补充密钥池
     */
    public void manualReplenishKeyPool() {
        log.info("手动执行WireGuard密钥池补充任务");
        try {
            wireGuardConfigService.generateKeyPairsForAllNodes(batchSize);
            log.info("手动WireGuard密钥池补充任务完成");
        } catch (Exception e) {
            log.error("手动WireGuard密钥池补充任务执行失败", e);
            throw e; // 重新抛出异常，让调用者知道执行失败
        }
    }
} 