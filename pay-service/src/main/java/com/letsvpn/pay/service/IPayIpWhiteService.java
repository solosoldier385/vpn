package com.letsvpn.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.letsvpn.pay.entity.PayIpWhite;

import java.util.List;

/**
 * <p>
 * 支付IP白名单表 服务类
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
public interface IPayIpWhiteService extends IService<PayIpWhite> {
    PayIpWhite getByPlatformIdAndIpAddress(Integer platformId, String ipAddress);
    List<PayIpWhite> getByPlatformId(Integer platformId);
    boolean isIpWhitelisted(Integer platformId, String ipAddress);
}