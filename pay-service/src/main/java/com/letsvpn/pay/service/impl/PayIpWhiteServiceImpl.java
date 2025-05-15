package com.letsvpn.pay.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.pay.entity.PayIpWhite;
import com.letsvpn.pay.mapper.PayIpWhiteMapper;
import com.letsvpn.pay.service.IPayIpWhiteService;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * <p>
 * 支付IP白名单表 服务实现类
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Service
public class PayIpWhiteServiceImpl extends ServiceImpl<PayIpWhiteMapper, PayIpWhite> implements IPayIpWhiteService {

    @Override
    public PayIpWhite getByPlatformIdAndIpAddress(Integer platformId, String ipAddress) {
        if (platformId == null || ipAddress == null || ipAddress.trim().isEmpty()) {
            return null;
        }
        return getOne(new QueryWrapper<PayIpWhite>()
                .eq("platform_id", platformId)
                .eq("ip_address", ipAddress));
    }

    @Override
    public List<PayIpWhite> getByPlatformId(Integer platformId) {
        if (platformId == null) {
            return null; // Or an empty list
        }
        return list(new QueryWrapper<PayIpWhite>().eq("platform_id", platformId));
    }

    @Override
    public boolean isIpWhitelisted(Integer platformId, String ipAddress) {
        PayIpWhite entry = getByPlatformIdAndIpAddress(platformId, ipAddress);
        return entry != null && entry.getStatus() != null && entry.getStatus() == 1; // Assuming 1 is active
    }
}