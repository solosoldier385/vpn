package com.letsvpn.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("node")
public class Node {
    private Long id;
    private String name;
    private String ip;
    private Integer port;
    private Integer levelRequired;
    private Boolean isFree;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // --- 新增字段 ---
    // 驼峰命名通常能被 MyBatis Plus 自动映射到下划线字段
    private String wgAddress;
    private String wgPublicKey; // 服务端公钥
    private String wgPrivateKey;
    private String wgDns;
}
