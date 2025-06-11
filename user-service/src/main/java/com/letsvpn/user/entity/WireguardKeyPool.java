package com.letsvpn.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("wireguard_key_pool")
public class WireguardKeyPool {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long nodeId;
    
    private String publicKey;
    
    private String privateKey;
    
    private String address;
    
    private Integer status; // 0-未分配，1-已分配，2-已失效
    
    private Long assignedUserId;
    
    private LocalDateTime assignedAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String remark;
    
    private String uuid;
} 