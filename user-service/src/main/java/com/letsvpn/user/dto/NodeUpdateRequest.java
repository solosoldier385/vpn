package com.letsvpn.user.dto;

import lombok.Data;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class NodeUpdateRequest {

    @Size(min = 1, max = 255, message = "节点名称长度必须在1到255个字符之间")
    private String name;

    // 正则表达式来自一个常见的IP校验，确保格式正确
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$", message = "无效的IP地址格式")
    private String ip;

    @Min(value = 1, message = "端口号必须至少为1")
    @Max(value = 65535, message = "端口号必须至多为65535")
    private Integer port;

    @Min(value = 0, message = "等级要求不能为负数")
    private Integer levelRequired;

    private Boolean isFree;

    @Min(value = 0, message = "状态值无效") // 假设0是正常，其他值代表不同状态
    private Integer status;

    // WireGuard 相关地址，例如 "10.0.0.1/24"
    @Size(min = 7, max = 43, message = "WireGuard 地址格式无效") // 例如 "0.0.0.0/0" 到 "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff/128"
    private String wgAddress;

    // Base64编码的WireGuard公钥，通常是44个字符长
    @Size(min = 44, max = 44, message = "WireGuard 公钥格式无效")
    private String wgPublicKey;

    // DNS服务器地址，可以是逗号分隔的多个地址
    @Size(max = 255, message = "DNS 配置过长")
    private String wgDns;

    // 注意：wgPrivateKey（私钥）通常不应该通过通用更新接口来修改，以保证安全。
    // createdAt 和 updatedAt 字段由系统自动管理。
}