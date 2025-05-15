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
 * 订单请求记录表
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_req_record")
public class OrderReqRecord implements Serializable {

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
     * 记录创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 支付配置ID
     */
    @TableField("pay_config_id")
    private Integer payConfigId;

    /**
     * 支付配置名称
     */
    @TableField("pay_config_name")
    private String payConfigName;

    /**
     * 支付配置渠道ID
     */
    @TableField("pay_config_channel_id")
    private Long payConfigChannelId;

    /**
     * 支付配置渠道名称
     */
    @TableField("pay_config_channel_name")
    private String payConfigChannelName;

    /**
     * 请求链接
     */
    @TableField("req_link")
    private String reqLink;

    /**
     * 请求耗时(毫秒)或请求时间戳
     */
    @TableField("req_time")
    private Long reqTime;

    /**
     * 请求体内容
     */
    @TableField("body_text")
    private String bodyText;

    /**
     * 响应结果内容
     */
    @TableField("result_text")
    private String resultText;

    /**
     * 错误信息内容
     */
    @TableField("error_text")
    private String errorText;

    /**
     * 日志文本内容
     */
    @TableField("log_text")
    private String logText;

    /**
     * 错误标识 (例如: 0表示无错误, 1或错误码表示有错误)
     */
    @TableField("error")
    private Integer error;


    public String getPayConfigName() { return payConfigName; }
    public OrderReqRecord setPayConfigName(String payConfigName) { this.payConfigName = payConfigName == null ? null : payConfigName.trim(); return this; }
    public String getPayConfigChannelName() { return payConfigChannelName; }
    public OrderReqRecord setPayConfigChannelName(String payConfigChannelName) { this.payConfigChannelName = payConfigChannelName == null ? null : payConfigChannelName.trim(); return this; }
    public String getReqLink() { return reqLink; }
    public OrderReqRecord setReqLink(String reqLink) { this.reqLink = reqLink == null ? null : reqLink.trim(); return this; }
    public String getBodyText() { return bodyText; }
    public OrderReqRecord setBodyText(String bodyText) { this.bodyText = bodyText == null ? null : bodyText.trim(); return this; }
    public String getResultText() { return resultText; }
    public OrderReqRecord setResultText(String resultText) { this.resultText = resultText == null ? null : resultText.trim(); return this; }
    public String getErrorText() { return errorText; }
    public OrderReqRecord setErrorText(String errorText) { this.errorText = errorText == null ? null : errorText.trim(); return this; }
    public String getLogText() { return logText; }
    public OrderReqRecord setLogText(String logText) { this.logText = logText == null ? null : logText.trim(); return this; }

}