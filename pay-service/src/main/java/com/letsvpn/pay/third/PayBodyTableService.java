package com.letsvpn.pay.third;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.letsvpn.pay.entity.MerchantInfo;
import com.letsvpn.pay.entity.PayConfigParameter;
import com.letsvpn.pay.third.util.ThirdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PayBodyTableService extends BaseBodyTableService {

	@Override
	public void extracted(Map<String, String> param, List<PayConfigParameter> payConfigParameters, StringBuffer logText,
						  Map<String, String> formData, MerchantInfo merchantInfo) {
		ThirdUtil.extracted2(param, payConfigParameters, formData, logText);

	}

	@Override
	public void paramExtraction(Map<String, String> paramMap, MerchantInfo merchantInfo) {

	}

	@Override
	public boolean signCheck(Map<String, String> paramsMap, List<PayConfigParameter> payConfigParameters,
			StringBuffer logText) {
		Map<String, String> formData = new HashMap<String, String>();
//		StringBuffer logText = new StringBuffer();
		ThirdUtil.extracted2(paramsMap, payConfigParameters, formData, logText);
		logText.append(formData).append("\n");

		for (PayConfigParameter payConfigParameter : payConfigParameters) {
			log.debug("payConfigParameters{}", JSONUtil.toJsonStr(payConfigParameter));
			String payName = payConfigParameter.getPayName();
			String paramValue = paramsMap.get(payName);
			log.debug("payName:{}, paramValue:{}", payName, paramValue);
			log.debug("formData:{}", formData);
			logText.append(payName).append(":").append(paramValue).append("\n");

			if (!MapUtil.getStr(formData, payName, "").equalsIgnoreCase(paramValue)) {
				return false;
			}
			// agentAcct={agentAcct}&agentOrderId={agentOrderId}&amount={agentOrderId}&payAmount={payAmount}&status={status}&key={__privateKey}
		}
		log.debug("paramsMap{}", paramsMap);
		return true;
	}

}
