package com.letsvpn.pay.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.pay.entity.PayConfigChannel;
import com.letsvpn.pay.mapper.PayConfigChannelMapper;
import com.letsvpn.pay.service.IPayConfigChannelService;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * <p>
 * 支付配置的渠道信息表 服务实现类
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Service
public class PayConfigChannelServiceImpl extends ServiceImpl<PayConfigChannelMapper, PayConfigChannel> implements IPayConfigChannelService {

    @Override
    public List<PayConfigChannel> getByPayConfigId(Integer payConfigId) {
        if (payConfigId == null) {
            return null; // Or an empty list
        }
        return list(new QueryWrapper<PayConfigChannel>().eq("pay_config_id", payConfigId));
    }
}