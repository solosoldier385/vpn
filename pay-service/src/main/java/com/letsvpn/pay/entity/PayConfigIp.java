package com.letsvpn.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("pay_config_ip")
public class PayConfigIp implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableField("pay_config_id")
    private Integer payConfigId;

    @TableField("callback_ip")
    private String callbackIp;

    @TableField("nullify")
    private Integer nullify;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("back_count")
    private Long backCount;


}
