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
 * 订单相关的虚拟账户信息表
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_virtual_account")
public class OrderVirtualAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 平台ID
     */
    @TableField("platform_id")
    private Integer platformId;

    /**
     * 游戏ID
     */
    @TableField("game_id")
    private Integer gameId;

    /**
     * 支付配置ID
     */
    @TableField("pay_config_id")
    private Integer payConfigId;

    /**
     * 账户持有人姓名
     */
    @TableField("name")
    private String name;

    /**
     * 主要联系方式 (例如手机号或邮箱)
     */
    @TableField("primary_contact")
    private String primaryContact;

    /**
     * 联系方式类型 (例如: MOBILE, EMAIL)
     */
    @TableField("contact_type")
    private String contactType;

    /**
     * 邮箱地址
     */
    @TableField("email")
    private String email;

    /**
     * 手机号码
     */
    @TableField("mobile")
    private String mobile;

    /**
     * 虚拟账户系统中的ID (若对接外部系统)
     */
    @TableField("virtual_accounts_id")
    private Long virtualAccountsId;

    /**
     * 虚拟银行账号
     */
    @TableField("virtual_account_number")
    private String virtualAccountNumber;

    /**
     * 虚拟账户IFSC代码 (例如印度支付)
     */
    @TableField("virtual_account_ifsc_code")
    private String virtualAccountIfscCode;

    /**
     * 虚拟支付地址 (例如UPI VPA)
     */
    @TableField("vpa")
    private String vpa;

    /**
     * 座机号码
     */
    @TableField("landline_number")
    private String landlineNumber;

    /**
     * 记录创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 记录更新时间
     */
    @TableField("update_time")
    private Date updateTime;

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
     * 账户数量或编号 (具体业务含义待定)
     */
    @TableField("account_num")
    private Integer accountNum;

}