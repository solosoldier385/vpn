package com.letsvpn.user.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class AddUserNodesRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    // 可以考虑加入其他参数，例如VIP等级（如果不同等级的VIP对应不同的节点策略）
    // private Integer vipLevel;
}