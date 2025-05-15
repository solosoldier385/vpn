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
 * 订单构建/处理错误记录表
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_build_error")
public class OrderBuildError implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * MDC跟踪ID (用于日志链路追踪)
     */
    @TableField("mdc_id")
    private String mdcId;

    /**
     * 平台ID
     */
    @TableField("platform_id")
    private Integer platformId;

    /**
     * 支付配置ID
     */
    @TableField("pay_config_id")
    private Integer payConfigId;

    /**
     * 支付配置渠道ID
     */
    @TableField("pay_config_channel_id")
    private Long payConfigChannelId;

    /**
     * 渠道名称
     */
    @TableField("channel_name")
    private String channelName;

    /**
     * 错误发生时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 发生错误的类名
     */
    @TableField("class_name")
    private String className;

    /**
     * 错误详情/堆栈信息
     */
    @TableField("error_text")
    private String errorText;

    /**
     * 相关的结果或上下文文本
     */
    @TableField("result_text")
    private String resultText;


    public String getMdcId() { return mdcId; }
    public OrderBuildError setMdcId(String mdcId) { this.mdcId = mdcId == null ? null : mdcId.trim(); return this; }
    public String getChannelName() { return channelName; }
    public OrderBuildError setChannelName(String channelName) { this.channelName = channelName == null ? null : channelName.trim(); return this; }
    public String getClassName() { return className; }
    public OrderBuildError setClassName(String className) { this.className = className == null ? null : className.trim(); return this; }
    public String getErrorText() { return errorText; }
    public OrderBuildError setErrorText(String errorText) { this.errorText = errorText == null ? null : errorText.trim(); return this; }
    public String getResultText() { return resultText; }
    public OrderBuildError setResultText(String resultText) { this.resultText = resultText == null ? null : resultText.trim(); return this; }

}