package com.letsvpn.pay.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.pay.entity.PayConfigParameter;
import com.letsvpn.pay.mapper.PayConfigParameterMapper;
import com.letsvpn.pay.service.IPayConfigParameterService;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * <p>
 * 支付配置的参数详情表 服务实现类
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Service
public class PayConfigParameterServiceImpl extends ServiceImpl<PayConfigParameterMapper, PayConfigParameter> implements IPayConfigParameterService {

    @Override
    public PayConfigParameter getByPayConfigIdAndPayIndex(Integer payConfigId, Integer payIndex) {
        if (payConfigId == null || payIndex == null) {
            return null;
        }
        return getOne(new QueryWrapper<PayConfigParameter>()
                .eq("pay_config_id", payConfigId)
                .eq("pay_index", payIndex));
    }

    @Override
    public List<PayConfigParameter> getByPayConfigId(Integer payConfigId) {
        if (payConfigId == null) {
            return null; // Or an empty list
        }
        return list(new QueryWrapper<PayConfigParameter>().eq("pay_config_id", payConfigId));
    }
}