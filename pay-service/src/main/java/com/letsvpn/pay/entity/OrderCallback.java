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
 * 订单回调记录表
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_callback")
public class OrderCallback implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 回调类型 (例如: PAYMENT_SUCCESS, REFUND_NOTICE)
     */
    @TableField("type")
    private String type;

    /**
     * 平台订单号或商户订单号
     */
    @TableField("platform_no")
    private String platformNo;

    /**
     * 支付配置ID
     */
    @TableField("pay_config_id")
    private Integer payConfigId;

    /**
     * 回调请求的完整URL
     */
    @TableField("req_url")
    private String reqUrl;

    /**
     * 回调记录创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 发起回调的IP地址
     */
    @TableField("create_ip")
    private String createIp;

    /**
     * 回调处理状态 (例如: 0-待处理, 1-成功, 2-失败)
     */
    @TableField("status")
    private Integer status;

    /**
     * 回调接收到的参数内容 (例如: JSON, XML或form-data字符串)
     */
    @TableField("param")
    private String param;


    public String getType() { return type; }
    public OrderCallback setType(String type) { this.type = type == null ? null : type.trim(); return this; }
    public String getPlatformNo() { return platformNo; }
    public OrderCallback setPlatformNo(String platformNo) { this.platformNo = platformNo == null ? null : platformNo.trim(); return this; }
    public String getReqUrl() { return reqUrl; }
    public OrderCallback setReqUrl(String reqUrl) { this.reqUrl = reqUrl == null ? null : reqUrl.trim(); return this; }
    public String getCreateIp() { return createIp; }
    public OrderCallback setCreateIp(String createIp) { this.createIp = createIp == null ? null : createIp.trim(); return this; }
    public String getParam() { return param; }
    public OrderCallback setParam(String param) { this.param = param == null ? null : param.trim(); return this; }

}