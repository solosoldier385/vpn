package com.letsvpn.pay.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.letsvpn.pay.entity.PayPlatformInfo;

/**
 * <p>
 * 支付平台信息表 服务类
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
public interface IPayPlatformInfoService extends IService<PayPlatformInfo> {
    // You can add custom service methods here if needed
    PayPlatformInfo getByPlatformNo(String platformNo);
}