package com.letsvpn.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.letsvpn.pay.entity.MerchantInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 商户（针对特定支付配置和平台）的密钥及应用信息表 Mapper 接口
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Mapper
public interface MerchantInfoMapper extends BaseMapper<MerchantInfo> {
    // For composite keys, BaseMapper methods like selectById(Serializable id) won't work directly.
    // You need to define custom methods or use QueryWrapper in service.
    // Example custom method:
    default MerchantInfo selectByCompositeKey(@Param("payConfigId") Integer payConfigId, @Param("platformId") Integer platformId) {
        return this.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MerchantInfo>()
                .eq("pay_config_id", payConfigId)
                .eq("platform_id", platformId));
    }

    default int deleteByCompositeKey(@Param("payConfigId") Integer payConfigId, @Param("platformId") Integer platformId) {
         return this.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MerchantInfo>()
                .eq("pay_config_id", payConfigId)
                .eq("platform_id", platformId));
    }
}