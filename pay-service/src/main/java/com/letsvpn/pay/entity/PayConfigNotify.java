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
 * 支付配置的通知解析规则表
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("pay_config_notify")
public class PayConfigNotify implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联的支付配置ID (外键, 主键部分)
     */
    @TableField("pay_config_id") // Part of composite PK
    private Integer payConfigId;

    /**
     * 通知类型 (例如: PAYMENT_SUCCESS, REFUND, 主键部分)
     */
    @TableField("type") // Part of composite PK
    private String type;

    /**
     * 通知中表示“我方订单号”的参数名
     */
    @TableField("order_param")
    private String orderParam;

    /**
     * 通知中表示“成功状态”的参数名
     */
    @TableField("success_param")
    private String successParam;

    /**
     * 通知中表示“成功”的参数值
     */
    @TableField("success_code")
    private String successCode;

    /**
     * 成功处理通知后，需返回给第三方的内容 (例如: success, OK)
     */
    @TableField("success_result")
    private String successResult;

    /**
     * 通知中表示“金额”的参数名
     */
    @TableField("amount_param")
    private String amountParam;

    /**
     * 金额单位 (例如: CENT 表示分, YUAN 表示元)
     */
    @TableField("amount_unit")
    private String amountUnit;

    /**
     * 通知中表示“第三方或渠道订单号”的参数名
     */
    @TableField("other_order_id_param")
    private String otherOrderIdParam;

    /**
     * 通知中表示“结算金额”的参数名
     */
    @TableField("settle_amount_param")
    private String settleAmountParam;

    /**
     * 此通知配置的创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 通知中表示“失败状态”的参数名
     */
    @TableField("fail_param")
    private String failParam;

    /**
     * 通知中表示“失败”的参数值或错误码
     */
    @TableField("fail_code")
    private String failCode;

    /**
     * 通知中表示“退款状态或金额”的参数名
     */
    @TableField("refund_param")
    private String refundParam;

    /**
     * 通知中表示“退款特定状态”的参数值
     */
    @TableField("refund_code")
    private String refundCode;

    /**
     * 是否需要验签 (0-否, 1-是)
     */
    @TableField("sign_check")
    private Integer signCheck;

    /**
     * 是否需要IP校验 (0-否, 1-是)
     */
    @TableField("ip_check")
    private Integer ipCheck;

    /**
     * 是否需要金额校验 (0-否, 1-是)
     */
    @TableField("amount_check")
    private Integer amountCheck;

    /**
     * 是否需要平台特定校验 (0-否, 1-是)
     */
    @TableField("platform_check")
    private Integer platformCheck;

    /**
     * 通知中表示UPI的参数名
     */
    @TableField("upi_param")
    private String upiParam;

    /**
     * 通知中表示IFSC代码的参数名
     */
    @TableField("ifsc_param")
    private String ifscParam;

}