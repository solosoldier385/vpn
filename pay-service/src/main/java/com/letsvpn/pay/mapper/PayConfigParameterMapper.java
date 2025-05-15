package com.letsvpn.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.letsvpn.pay.entity.PayConfigParameter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 支付配置的参数详情表 Mapper 接口
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Mapper
public interface PayConfigParameterMapper extends BaseMapper<PayConfigParameter> {
    // Example custom method for composite key
    default PayConfigParameter selectByCompositeKey(@Param("payConfigId") Integer payConfigId, @Param("payIndex") Integer payIndex) {
        return this.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PayConfigParameter>()
                .eq("pay_config_id", payConfigId)
                .eq("pay_index", payIndex));
    }
}