package com.letsvpn.pay.service.base.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.pay.entity.PayPlatformInfo;
import com.letsvpn.pay.mapper.PayPlatformInfoMapper;
import com.letsvpn.pay.service.base.IPayPlatformInfoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 支付平台信息表 服务实现类
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Service
public class PayPlatformInfoServiceImpl extends ServiceImpl<PayPlatformInfoMapper, PayPlatformInfo> implements IPayPlatformInfoService {
    // You can inject other services or mappers here if needed

    @Override
    public PayPlatformInfo getByPlatformNo(String platformNo) {
        if (platformNo == null || platformNo.trim().isEmpty()) {
            return null;
        }
        return getOne(new QueryWrapper<PayPlatformInfo>().eq("platform_no", platformNo));
    }
    // Implement custom service methods here
}