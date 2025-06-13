package com.letsvpn.pay.service.base.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.pay.entity.MerchantInfo;
import com.letsvpn.pay.mapper.MerchantInfoMapper;
import com.letsvpn.pay.service.base.IMerchantInfoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商户（针对特定支付配置和平台）的密钥及应用信息表 服务实现类
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Service
public class MerchantInfoServiceImpl extends ServiceImpl<MerchantInfoMapper, MerchantInfo> implements IMerchantInfoService {

    @Override
    public MerchantInfo getByPayConfigIdAndPlatformId(Integer payConfigId, Integer platformId) {
        if (payConfigId == null || platformId == null) {
            return null;
        }
        // return baseMapper.selectByCompositeKey(payConfigId, platformId); // If using custom mapper method
        return getOne(new QueryWrapper<MerchantInfo>()
                .eq("pay_config_id", payConfigId)
                .eq("platform_id", platformId));
    }

    @Override
    public boolean deleteByPayConfigIdAndPlatformId(Integer payConfigId, Integer platformId) {
        if (payConfigId == null || platformId == null) {
            return false;
        }
        // return baseMapper.deleteByCompositeKey(payConfigId, platformId) > 0; // If using custom mapper method
        return remove(new QueryWrapper<MerchantInfo>()
                .eq("pay_config_id", payConfigId)
                .eq("platform_id", platformId));
    }
}