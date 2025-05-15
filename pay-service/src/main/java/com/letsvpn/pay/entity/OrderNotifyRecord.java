package com.letsvpn.pay.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 订单通知记录表
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_notify_record")
public class OrderNotifyRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 平台ID
     */
    @TableField("platform_id")
    private Integer platformId;

    /**
     * 通知记录创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 接收通知的请求来源IP
     */
    @TableField("create_ip")
    private String createIp;

    /**
     * 支付配置ID
     */
    @TableField("pay_config_id")
    private Integer payConfigId;

    /**
     * 签名校验结果或相关信息
     */
    @TableField("sign_check")
    private String signCheck;

    /**
     * 接收到的通知参数Map序列化字符串 (例如: JSON)
     */
    @TableField("params_map")
    private String paramsMap;

    /**
     * 支付配置相关参数序列化字符串 (例如: JSON)
     */
    @TableField("pay_config_parameters")
    private String payConfigParameters;

    /**
     * 相关日志文本
     */
    @TableField("log_text")
    private String logText;

    /**
     * 处理通知的类名
     */
    @TableField("class_name")
    private String className;

}