package com.letsvpn.pay.service.core;


import com.letsvpn.pay.entity.OrderBuildError;
import com.letsvpn.pay.mapper.OrderBuildErrorMapper;
import com.letsvpn.pay.util.PayConstant;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class OrderBuildErrorService extends BaseService {
	@Autowired
	OrderBuildErrorMapper orderBuildErrorMapper;

	public void add(Integer platformId, Integer payConfigId, Long payConfigChannelId, String channelName,
			String errorText, String resultText, String className) {

		OrderBuildError record = new OrderBuildError();
		record.setMdcId(MDC.get(PayConstant.MDC_KEY_REQ_ID));
		record.setCreateTime(new Date());
		record.setErrorText(errorText);
		record.setPayConfigChannelId(payConfigChannelId);
		record.setChannelName(channelName);
		record.setPayConfigId(payConfigId);
		record.setPlatformId(platformId);
		record.setClassName(className);
		record.setResultText(resultText);
		orderBuildErrorMapper.insert(record);
	}

}
