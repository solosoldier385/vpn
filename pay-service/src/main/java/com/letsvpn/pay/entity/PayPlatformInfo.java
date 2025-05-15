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
 * 支付平台信息表
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("pay_platform_info")
public class PayPlatformInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 平台唯一ID (主键)
     */
    @TableId(value = "platform_id", type = IdType.AUTO)
    private Integer platformId;

    /**
     * 平台编号/代码 (业务唯一标识)
     */
    @TableField("platform_no")
    private String platformNo;

    /**
     * 平台名称/标题
     */
    @TableField("title")
    private String title;

    /**
     * 平台主域名
     */
    @TableField("domain")
    private String domain;

    /**
     * 平台密钥 (用于签名或API认证, 请注意加密存储)
     */
    @TableField("secret_key")
    private String secretKey;

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
     * 记录创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 平台默认回调URL (接收支付结果通知等)
     */
    @TableField("callback_url")
    private String callbackUrl;

    /**
     * 特定用途URL (例如微信相关或其他)
     */
    @TableField("we_url")
    private String weUrl;


    public String getPlatformNo() {
        return platformNo;
    }

    public PayPlatformInfo setPlatformNo(String platformNo) {
        this.platformNo = platformNo == null ? null : platformNo.trim();
        return this;
    }

    public String getTitle() {
        return title;
    }

    public PayPlatformInfo setTitle(String title) {
        this.title = title == null ? null : title.trim();
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public PayPlatformInfo setDomain(String domain) {
        this.domain = domain == null ? null : domain.trim();
        return this;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public PayPlatformInfo setSecretKey(String secretKey) {
        this.secretKey = secretKey == null ? null : secretKey.trim();
        return this;
    }
    
    public String getCallbackUrl() {
        return callbackUrl;
    }

    public PayPlatformInfo setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl == null ? null : callbackUrl.trim();
        return this;
    }

    public String getWeUrl() {
        return weUrl;
    }

    public PayPlatformInfo setWeUrl(String weUrl) {
        this.weUrl = weUrl == null ? null : weUrl.trim();
        return this;
    }
}