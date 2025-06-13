package com.letsvpn.pay.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.letsvpn.pay.entity.PayConfigNotify;

import java.util.List;

/**
 * <p>
 * 支付配置的通知解析规则表 服务类
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
public interface IPayConfigNotifyService extends IService<PayConfigNotify> {
    PayConfigNotify getByPayConfigIdAndType(Integer payConfigId, String type);
    List<PayConfigNotify> getByPayConfigId(Integer payConfigId);
}