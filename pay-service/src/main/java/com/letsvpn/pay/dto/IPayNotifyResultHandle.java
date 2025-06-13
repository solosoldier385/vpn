package com.letsvpn.pay.dto;



import com.letsvpn.pay.entity.MerchantInfo;
import com.letsvpn.pay.util.PayResultType;

import java.util.Map;

public interface IPayNotifyResultHandle {

	public String result(PayResultType payResultType, Map<String, String> paramsMap, MerchantInfo merchantInfo);

}
