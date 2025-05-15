package com.letsvpn.pay.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
// For composite keys, we don't use @TableId on individual fields in the entity.
// MyBatis-Plus BaseMapper methods like getById will not work directly.
// You'll use QueryWrapper or define custom methods in the mapper.
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 商户（针对特定支付配置和平台）的密钥及应用信息表
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("merchant_info")
public class MerchantInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联的支付配置ID (主键部分, 通常外键指向 pay_config_info.id)
     */
    @TableField("pay_config_id") // Also part of composite PK
    private Integer payConfigId;

    /**
     * 关联的平台ID (主键部分, 通常外键指向 platform_info.id)
     */
    @TableField("platform_id") // Also part of composite PK
    private Integer platformId;

    /**
     * 应用ID (App ID)
     */
    @TableField("app_id")
    private String appId;

    /**
     * 商户主私钥 (通常用于签名和API认证)
     */
    @TableField("private_key")
    private String privateKey;

    /**
     * 记录创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 商户备用私钥1或特定用途私钥
     */
    @TableField("private_key1")
    private String privateKey1;

    /**
     * 商户备用私钥2或特定用途私钥
     */
    @TableField("private_key2")
    private String privateKey2;

    /**
     * 商户备用私钥3或特定用途私钥
     */
    @TableField("private_key3")
    private String privateKey3;

    /**
     * 商户备用私钥4或特定用途私钥
     */
    @TableField("private_key4")
    private String privateKey4;

}