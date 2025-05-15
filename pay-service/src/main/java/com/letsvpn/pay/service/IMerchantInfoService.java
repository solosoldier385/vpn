package com.letsvpn.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.letsvpn.pay.entity.MerchantInfo;

/**
 * <p>
 * 商户（针对特定支付配置和平台）的密钥及应用信息表 服务类
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
public interface IMerchantInfoService extends IService<MerchantInfo> {
    MerchantInfo getByPayConfigIdAndPlatformId(Integer payConfigId, Integer platformId);
    boolean deleteByPayConfigIdAndPlatformId(Integer payConfigId, Integer platformId);
    // Other custom methods
}