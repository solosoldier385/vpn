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
 * 支付配置的渠道信息表
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("pay_config_channel")
public class PayConfigChannel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的支付配置ID (外键指向 pay_config_info.id)
     */
    @TableField("pay_config_id")
    private Integer payConfigId;

    /**
     * 渠道标题/名称
     */
    @TableField("title")
    private String title;

    /**
     * 渠道特定的JSON格式配置参数
     */
    @TableField("json_param")
    private String jsonParam;

    /**
     * 记录创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 共享ID或权重ID (具体业务含义待定)
     */
    @TableField("share_id")
    private Integer shareId;

    /**
     * 提取类型 (例如: JSONPath, REGEX, XPath)
     */
    @TableField("extract_type")
    private String extractType;

    /**
     * 提取字段的路径或表达式 (例如: $.data.token)
     */
    @TableField("extract_field")
    private String extractField;

    /**
     * 提取值的前缀
     */
    @TableField("extract_prefix")
    private String extractPrefix;

    @TableField("status")
    private Integer status;

}