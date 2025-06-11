package com.letsvpn.user;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.letsvpn")
@EnableDiscoveryClient
@MapperScan({"com.letsvpn.common.data.mapper","com.letsvpn.user.mapper"}) // ✅ 扫描 common-data 模块里的 mapper 接口
@EnableFeignClients
@EnableScheduling // 启用定时任务
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
