package com.letsvpn.pay.third;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.letsvpn.pay.dto.IPayNotifyHandle;
import com.letsvpn.pay.dto.IPayThirdRequest;
import com.letsvpn.pay.entity.MerchantInfo;
import com.letsvpn.pay.entity.PayConfigChannel;
import com.letsvpn.pay.entity.PayConfigInfo;
import com.letsvpn.pay.entity.PayConfigParameter;
import com.letsvpn.pay.mapper.OrderReqRecordMapper;
import com.letsvpn.pay.service.core.OrderInfoService;
import com.letsvpn.pay.third.util.ThirdUtil;
import com.letsvpn.pay.util.PayCallMethod;
import com.letsvpn.pay.vo.PayResultData;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Service
@Slf4j
public class PayFormTableService extends BaseThirdService implements IPayThirdRequest, IPayNotifyHandle {
	@Autowired
	OrderInfoService orderInfoService;

	@Autowired
	OrderReqRecordMapper orderReqRecordMapper;
//	@Autowired
//	PayConfigParameterService payConfigParameterService;

	@Override
	public PayResultData executeReq(MerchantInfo merchantInfo, PayConfigInfo payConfigInfo, PayConfigChannel channel,
									Map<String, String> param, List<PayConfigParameter> payConfigParameters) {
		if (payConfigInfo == null)
			return null;

		Integer payConfigId = payConfigInfo.getId();

//		List<PayConfigParameter> payConfigParameters = payConfigParameterService
//				.listPayConfigParameterError(payConfigId, channel.getId(), PayConfigEnum.request);
//		for (PayConfigParameter payConfigParameter : payConfigParameters) {
//			if ("text".equals(payConfigParameter.getType())) {
//				param.put(payConfigParameter.getPayName(), payConfigParameter.getPayValue());
//			}
//		}

		StringBuffer logText = new StringBuffer();
//		logText.append(JSONUtil.toJsonStr(merchantInfo)).append("\n");
		PayCallMethod callMethod = PayCallMethod.valueOf(payConfigInfo.getCallMethod());
		PayResultData result = new PayResultData(callMethod);

		Map<String, String> formData = new HashMap<String, String>();
		ThirdUtil.extracted2(param, payConfigParameters, formData, logText);
		Map<String, Object> formData1 = new HashMap<String, Object>();
		for (Entry<String, String> payReqParam : formData.entrySet()) {
			formData1.put(payReqParam.getKey(), payReqParam.getValue());
		}
		String link = payConfigInfo.getUrl();
		result.setLink(link);
		if (callMethod.compareTo(PayCallMethod.form) == 0) {
			result.setHtml(ThirdUtil.printlnForm(payConfigInfo.getTest() == 1, link, formData, logText));
		}
		extracted(merchantInfo, payConfigInfo, channel, payConfigInfo.getUrl(), formData1, result.getHtml(), 0, "",
				logText, 0);
		return result;
	}

	@Override
	public void paramExtraction(Map<String, String> paramMap, MerchantInfo merchantInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean signCheck(Map<String, String> paramsMap, List<PayConfigParameter> payConfigParameters,
			StringBuffer logText) {
		Map<String, String> formData = new HashMap<String, String>();
//	StringBuffer logText = new StringBuffer();
		ThirdUtil.extracted2(paramsMap, payConfigParameters, formData, logText);
		logText.append(formData).append("\n\n");

		for (PayConfigParameter payConfigParameter : payConfigParameters) {
			log.debug("payConfigParameters{}", JSONUtil.toJsonStr(payConfigParameter));
			String payName = payConfigParameter.getPayName();
			String paramValue = paramsMap.get(payName);
			log.debug("payName:{}, paramValue:{}", payName, paramValue);
			log.debug("formData:{}", formData);

			logText.append(payName).append(":").append(paramValue).append("\n\n");

			if (!MapUtil.getStr(formData, payName, "").equalsIgnoreCase(paramValue)) {
				return false;
			}
			// agentAcct={agentAcct}&agentOrderId={agentOrderId}&amount={agentOrderId}&payAmount={payAmount}&status={status}&key={__privateKey}
		}
		log.debug("paramsMap{}", paramsMap);
		return true;

	}

}
