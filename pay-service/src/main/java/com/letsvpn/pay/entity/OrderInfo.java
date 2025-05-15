package com.letsvpn.pay.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 订单信息表
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_info")
public class OrderInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    @TableField("order_id")
    private String orderId;

    /**
     * 平台ID
     */
    @TableField("platform_id")
    private Integer platformId;

    /**
     * 前端订单号/商户订单号
     */
    @TableField("front_id")
    private String frontId;

    /**
     * 支付配置ID
     */
    @TableField("pay_config_id")
    private Integer payConfigId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 游戏ID
     */
    @TableField("game_id")
    private Integer gameId;

    /**
     * 订单状态
     */
    @TableField("status")
    private Integer status;

    /**
     * 请求金额
     */
    @TableField("req_amount")
    private BigDecimal reqAmount;

    /**
     * 实际支付金额
     */
    @TableField("real_amount")
    private BigDecimal realAmount;

    /**
     * 支付时间
     */
    @TableField("pay_time")
    private Date payTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 创建状态
     */
    @TableField("create_status")
    private Integer createStatus;

    /**
     * 创建IP
     */
    @TableField("create_ip")
    private String createIp;

    /**
     * 通知状态
     */
    @TableField("notice_status")
    private Integer noticeStatus;

    /**
     * 通知时间
     */
    @TableField("notice_time")
    private Date noticeTime;

    /**
     * 支付配置渠道ID
     */
    @TableField("pay_config_channel_id")
    private Long payConfigChannelId;

    /**
     * 其他订单号 (例如上游渠道订单号)
     */
    @TableField("other_order_id")
    private String otherOrderId;

    /**
     * 在线ID (具体含义需根据业务确定)
     */
    @TableField("on_line_id")
    private Long onLineId;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 扩展字段1
     */
    @TableField("extend1")
    private String extend1;

    /**
     * 扩展字段2
     */
    @TableField("extend2")
    private String extend2;

    /**
     * 扩展字段3
     */
    @TableField("extend3")
    private String extend3;

    /**
     * 同步状态
     */
    @TableField("sync_status")
    private Integer syncStatus;

    /**
     * 结算金额
     */
    @TableField("settle_amount")
    private BigDecimal settleAmount;

    /**
     * UPI (统一支付接口) 相关信息
     */
    @TableField("upi")
    private String upi;


    public String getOrderId() { return orderId; }
    public OrderInfo setOrderId(String orderId) { this.orderId = orderId == null ? null : orderId.trim(); return this; }
    public String getFrontId() { return frontId; }
    public OrderInfo setFrontId(String frontId) { this.frontId = frontId == null ? null : frontId.trim(); return this; }
    public String getCreateIp() { return createIp; }
    public OrderInfo setCreateIp(String createIp) { this.createIp = createIp == null ? null : createIp.trim(); return this; }
    public String getOtherOrderId() { return otherOrderId; }
    public OrderInfo setOtherOrderId(String otherOrderId) { this.otherOrderId = otherOrderId == null ? null : otherOrderId.trim(); return this; }
    public String getRemark() { return remark; }
    public OrderInfo setRemark(String remark) { this.remark = remark == null ? null : remark.trim(); return this; }
    public String getExtend1() { return extend1; }
    public OrderInfo setExtend1(String extend1) { this.extend1 = extend1 == null ? null : extend1.trim(); return this; }
    public String getExtend2() { return extend2; }
    public OrderInfo setExtend2(String extend2) { this.extend2 = extend2 == null ? null : extend2.trim(); return this; }
    public String getExtend3() { return extend3; }
    public OrderInfo setExtend3(String extend3) { this.extend3 = extend3 == null ? null : extend3.trim(); return this; }
    public String getUpi() { return upi; }
    public OrderInfo setUpi(String upi) { this.upi = upi == null ? null : upi.trim(); return this; }

}