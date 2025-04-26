package com.letsvpn.common.data.entity; // 放在 common-data 包下

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Builder; // 可选，方便构建对象
import lombok.NoArgsConstructor; // 可选
import lombok.AllArgsConstructor; // 可选
import java.time.LocalDateTime;

@Data
@Builder // 可选
@NoArgsConstructor // 可选
@AllArgsConstructor // 可选
@TableName("user_node_config")
public class UserNodeConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long nodeId;

    // 对应 wg_peer_public_key
    private String wgPeerPublicKey; // 服务端为用户生成的公钥

    // 对应 wg_allowed_ips
    private String wgAllowedIps; // 分配给用户的内网 IP (CIDR)

    // 对应 is_active
    private Boolean isActive;

    // 对应 created_at (MyBatis Plus 可自动填充)
    private LocalDateTime createdAt;

    // 对应 updated_at (MyBatis Plus 可自动填充)
    private LocalDateTime updatedAt;
}