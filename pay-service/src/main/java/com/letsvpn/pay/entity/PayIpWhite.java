package com.letsvpn.pay.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 支付IP白名单表
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("pay_ip_white")
public class PayIpWhite implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联的平台ID (主键部分, 通常外键指向 platform_info.id)
     */
    @TableField("platform_id") // Part of composite PK
    private Integer platformId;

    /**
     * IP地址 (IPv4 或 IPv6, 主键部分)
     */
    @TableField("ip_address") // Part of composite PK
    private String ipAddress;

    /**
     * 白名单记录创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 状态 (0-无效/禁用, 1-有效/启用)
     */
    @TableField("status")
    private Integer status;

}