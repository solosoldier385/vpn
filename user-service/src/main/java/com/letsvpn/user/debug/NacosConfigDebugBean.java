package com.letsvpn.user.debug;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct; // 或

@Component
public class NacosConfigDebugBean {

    @Value("${spring.cloud.nacos.server-addr:NOT_FOUND}") // 添加默认值以防报错，方便观察
    private String nacosServerAddr;

    @Value("${spring.cloud.nacos.config.namespace:NOT_FOUND}")
    private String nacosNamespace;

    @PostConstruct
    public void init() {
        System.out.println("DEBUG: Nacos Server Address from @Value: " + nacosServerAddr);
        System.out.println("DEBUG: Nacos Namespace from @Value: " + nacosNamespace);
    }
}