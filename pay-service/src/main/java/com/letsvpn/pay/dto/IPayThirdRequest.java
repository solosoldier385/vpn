package com.letsvpn.pay.dto;



import com.letsvpn.pay.entity.MerchantInfo;
import com.letsvpn.pay.entity.PayConfigChannel;
import com.letsvpn.pay.entity.PayConfigInfo;
import com.letsvpn.pay.entity.PayConfigParameter;
import com.letsvpn.pay.vo.PayResultData;

import java.util.List;
import java.util.Map;

public interface IPayThirdRequest {

	PayResultData executeReq(MerchantInfo merchantInfo, PayConfigInfo payConfigInfo, PayConfigChannel channel,
							 Map<String, String> param, List<PayConfigParameter> payConfigParameters);

//	PayQueryData executeQuery(MerchantInfo merchantInfo, PayConfigInfo payConfigInfo, PayConfigQuery query,
//			Map<String, String> param);

//	int queryProcessing(Long id, String orderId, String result, PayConfigQuery configQuery);
}
