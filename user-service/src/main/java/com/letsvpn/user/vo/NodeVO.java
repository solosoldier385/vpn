package com.letsvpn.user.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "节点信息视图对象 (过滤敏感字段)")
public class NodeVO {

    @Schema(description = "节点ID", example = "1")
    private Long id;

    @Schema(description = "节点名称", example = "香港CN2节点-01")
    private String name;

    @Schema(description = "节点要求的最低VIP等级代码 (0=免费)", example = "0")
    private Integer levelRequired;

    @Schema(description = "节点是否为免费节点", example = "true")
    private Boolean isFree;

    @Schema(description = "节点状态 (0=正常)", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "DNS服务器地址", example = "8.8.8.8")
    private String wgDns;
} 