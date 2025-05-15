package com.letsvpn.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.letsvpn.pay.entity.PayConfigNotify;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 支付配置的通知解析规则表 Mapper 接口
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Mapper
public interface PayConfigNotifyMapper extends BaseMapper<PayConfigNotify> {
    // Example custom method for composite key
    default PayConfigNotify selectByCompositeKey(@Param("payConfigId") Integer payConfigId, @Param("type") String type) {
        return this.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PayConfigNotify>()
                .eq("pay_config_id", payConfigId)
                .eq("type", type));
    }
}