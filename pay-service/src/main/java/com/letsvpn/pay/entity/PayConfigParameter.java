package com.letsvpn.pay.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 支付配置的参数详情表
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("pay_config_parameter")
public class PayConfigParameter implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联的支付配置ID (主键部分, 外键指向 pay_config_info.id)
     */
    @TableField("pay_config_id") // Part of composite PK
    private Integer payConfigId;

    /**
     * 参数在此配置中的顺序或索引号 (主键部分)
     */
    @TableField("pay_index") // Part of composite PK
    private Integer payIndex;

    /**
     * 关联的支付渠道ID (可选, 外键指向 pay_config_channel.id)
     */
    @TableField("pay_config_channel_id")
    private Long payConfigChannelId;

    /**
     * 参数的业务名称/标识符 (例如: merchant_id, api_key, secret_key)
     */
    @TableField("pay_name")
    private String payName;

    /**
     * 参数的实际值 (可能需要加密存储)
     */
    @TableField("pay_value")
    private String payValue;

    /**
     * 参数的数据类型或用途分类 (例如: STRING, TEXT, INTEGER, BOOLEAN, FILE_PATH, SECRET)
     */
    @TableField("type")
    private String type;

    /**
     * 参数的可选枚举值或相关配置说明 (例如JSON数组或特定格式字符串)
     */
    @TableField("config_enum")
    private String configEnum;

}