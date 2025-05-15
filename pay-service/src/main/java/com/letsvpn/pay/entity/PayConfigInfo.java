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
 * 支付配置信息表
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("pay_config_info")
public class PayConfigInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 短代码/唯一标识
     */
    @TableField("short_code")
    private String shortCode;

    /**
     * 配置标题/名称
     */
    @TableField("title")
    private String title;

    /**
     * 备注信息
     */
    @TableField("remark")
    private String remark;

    /**
     * 主要请求URL (例如支付网关地址)
     */
    @TableField("url")
    private String url;

    /**
     * 第三方服务名称/标识
     */
    @TableField("third_service")
    private String thirdService;

    /**
     * 调用方法 (例如 HTTP方法 GET/POST, 或API方法名)
     */
    @TableField("call_method")
    private String callMethod;

    /**
     * 请求参数模板或默认参数 (例如 JSON格式)
     */
    @TableField("req_param")
    private String reqParam;

    /**
     * 是否作废/禁用 (0-启用, 1-作废/禁用)
     */
    @TableField("nullify")
    private Integer nullify;

    /**
     * 区域类型/适用地区代码
     */
    @TableField("area_type")
    private Integer areaType;

    /**
     * 是否测试配置 (0-正式, 1-测试)
     */
    @TableField("test")
    private Integer test;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * HTTP类型 (例如 HTTP, HTTPS, 或Content-Type)
     */
    @TableField("http_type")
    private String httpType;

    /**
     * 特定用途URL (例如 微信相关URL, 回调URL等)
     */
    @TableField("we_url")
    private String weUrl;

    /**
     * 请求域名 (用于动态构建URL或校验)
     */
    @TableField("req_domain")
    private String reqDomain;

}