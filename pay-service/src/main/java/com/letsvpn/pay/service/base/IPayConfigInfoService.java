package com.letsvpn.pay.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.letsvpn.pay.entity.PayConfigInfo;

/**
 * <p>
 * 支付配置信息表 服务类
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
public interface IPayConfigInfoService extends IService<PayConfigInfo> {
    PayConfigInfo getByShortCode(String shortCode);
}