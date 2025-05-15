package com.letsvpn.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.letsvpn.pay.entity.PayIpWhite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 支付IP白名单表 Mapper 接口
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Mapper
public interface PayIpWhiteMapper extends BaseMapper<PayIpWhite> {
    // Example custom method for composite key
    default PayIpWhite selectByCompositeKey(@Param("platformId") Integer platformId, @Param("ipAddress") String ipAddress) {
        return this.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PayIpWhite>()
                .eq("platform_id", platformId)
                .eq("ip_address", ipAddress));
    }
}