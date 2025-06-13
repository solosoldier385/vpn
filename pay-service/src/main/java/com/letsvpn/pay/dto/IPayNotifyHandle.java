package com.letsvpn.pay.dto;



import com.letsvpn.pay.entity.MerchantInfo;
import com.letsvpn.pay.entity.PayConfigParameter;

import java.util.List;
import java.util.Map;

//回调参数提取
public interface IPayNotifyHandle {

	/**
	 * 提取参数
	 * 
	 * @param paramMap
	 * @param merchantInfo
	 */
	public void paramExtraction(Map<String, String> paramMap, MerchantInfo merchantInfo);

	/**
	 * 验签
	 * 
	 * @param paramsMap
	 * @param payConfigParameters
	 * @param payConfigParameters
	 * @return true 通过,false 不通过
	 */
	public boolean signCheck(Map<String, String> paramsMap, List<PayConfigParameter> payConfigParameters,
			StringBuffer logText);
}
