package com.letsvpn.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.letsvpn.pay.entity.PayConfigParameter;

import java.util.List;

/**
 * <p>
 * 支付配置的参数详情表 服务类
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
public interface IPayConfigParameterService extends IService<PayConfigParameter> {
    PayConfigParameter getByPayConfigIdAndPayIndex(Integer payConfigId, Integer payIndex);
    List<PayConfigParameter> getByPayConfigId(Integer payConfigId);
}