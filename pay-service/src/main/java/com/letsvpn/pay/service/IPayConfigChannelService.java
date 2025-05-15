package com.letsvpn.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.letsvpn.pay.entity.PayConfigChannel;

import java.util.List;

/**
 * <p>
 * 支付配置的渠道信息表 服务类
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
public interface IPayConfigChannelService extends IService<PayConfigChannel> {
    List<PayConfigChannel> getByPayConfigId(Integer payConfigId);
}