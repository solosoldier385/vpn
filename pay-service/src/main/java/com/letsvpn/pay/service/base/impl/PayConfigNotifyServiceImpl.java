package com.letsvpn.pay.service.base.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.pay.entity.PayConfigNotify;
import com.letsvpn.pay.mapper.PayConfigNotifyMapper;
import com.letsvpn.pay.service.base.IPayConfigNotifyService;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * <p>
 * 支付配置的通知解析规则表 服务实现类
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Service
public class PayConfigNotifyServiceImpl extends ServiceImpl<PayConfigNotifyMapper, PayConfigNotify> implements IPayConfigNotifyService {

    @Override
    public PayConfigNotify getByPayConfigIdAndType(Integer payConfigId, String type) {
        if (payConfigId == null || type == null || type.trim().isEmpty()) {
            return null;
        }
        return getOne(new QueryWrapper<PayConfigNotify>()
                .eq("pay_config_id", payConfigId)
                .eq("type", type));
    }

    @Override
    public List<PayConfigNotify> getByPayConfigId(Integer payConfigId) {
        if (payConfigId == null) {
            return null; // Or an empty list
        }
        return list(new QueryWrapper<PayConfigNotify>().eq("pay_config_id", payConfigId));
    }
}