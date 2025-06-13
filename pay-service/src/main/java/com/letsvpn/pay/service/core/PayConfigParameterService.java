package com.letsvpn.pay.service.core;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.pay.entity.PayConfigParameter;
import com.letsvpn.pay.mapper.PayConfigParameterMapper;
import com.letsvpn.pay.util.PayConfigEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class PayConfigParameterService extends BaseService {
	@Autowired
	PayConfigParameterMapper payConfigParameterMapper;

//	public List<PayConfigParameter> listPayConfigParameterError(Integer payConfigId, long channel,
//			PayConfigEnum payConfigEnum) {
//		List<PayConfigParameter> list = listPayConfigParameter(payConfigId, channel, payConfigEnum);
//		if (list.isEmpty()) {
//			throw new WanliException(new KeyValue<Integer, String>(5001, "该支付异常,没有配置参数."));
//		}
//		return list;
//
//	}

	public List<PayConfigParameter> listPayConfigParameter(Integer payConfigId, long channel,
														   PayConfigEnum payConfigEnum) {
		QueryWrapper<PayConfigParameter> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("pay_config_id", payConfigId);
		queryWrapper.in("pay_config_channel_id", Arrays.asList(0L, channel));
		queryWrapper.eq("config_enum", payConfigEnum.name());
		return payConfigParameterMapper.selectList(queryWrapper);

	}

}
