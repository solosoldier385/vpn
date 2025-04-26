package com.letsvpn.user.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank; // 引入 JSR 303 验证注解
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

@Data
public class NodeRegistrationRequest {

    @NotBlank(message = "节点名称不能为空")
    private String name; // 例如 "Tokyo Node 1"

    @NotBlank(message = "节点公网 IP 不能为空")
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$", message = "IP 地址格式不正确")
    private String ip; // 节点服务器的公网 IP

    @NotNull(message = "WireGuard 监听端口不能为空")
    @Min(value = 1, message = "端口号必须大于 0")
    private Integer port; // WireGuard 服务监听的端口

    @NotNull(message = "所需用户等级不能为空")
    private Integer levelRequired; // 访问该节点需要的最低用户等级

    @NotNull(message = "是否免费标记不能为空")
    private Boolean isFree; // 是否为免费节点

    @NotBlank(message = "WireGuard 内部网络地址不能为空")
    @Pattern(regexp = "^.+\\/(?:[1-9]|[12][0-9]|3[0-2])$", message = "WG 网络地址必须是 CIDR 格式，例如 10.0.1.1/24")
    private String wgAddress; // 分配给该节点 WireGuard 接口的内部网络地址 (CIDR)

    @NotBlank(message = "WireGuard 服务端公钥不能为空")
    private String wgPublicKey; // 该节点 WireGuard 服务器的公钥

    @NotBlank(message = "WireGuard 客户端私钥不能为空")
    private String wgPrivateKey; // 该节点 WireGuard 服务器的公钥

    @NotBlank(message = "DNS 配置不能为空")
    private String wgDns; // 推送给客户端的 DNS 服务器地址

    // 注意：这里不包含 wgPrivateKey，私钥不应通过 API 传输
    // 其他可选字段，如 location, description 等可以按需添加
    private String location; // 例如 "Tokyo"
}