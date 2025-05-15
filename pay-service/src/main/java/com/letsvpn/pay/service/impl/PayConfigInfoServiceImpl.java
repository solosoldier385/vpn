package com.letsvpn.pay.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.pay.entity.PayConfigInfo;
import com.letsvpn.pay.mapper.PayConfigInfoMapper;
import com.letsvpn.pay.service.IPayConfigInfoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 支付配置信息表 服务实现类
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Service
public class PayConfigInfoServiceImpl extends ServiceImpl<PayConfigInfoMapper, PayConfigInfo> implements IPayConfigInfoService {

    @Override
    public PayConfigInfo getByShortCode(String shortCode) {
        if (shortCode == null || shortCode.trim().isEmpty()) {
            return null;
        }
        return getOne(new QueryWrapper<PayConfigInfo>().eq("short_code", shortCode));
    }
}