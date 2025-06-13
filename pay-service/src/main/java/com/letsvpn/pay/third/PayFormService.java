package com.letsvpn.pay.third;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
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
import com.letsvpn.pay.vo.PayReqParam;
import com.letsvpn.pay.vo.PayResultData;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PayFormService implements IPayThirdRequest, IPayNotifyHandle {
	@Autowired
	OrderInfoService orderInfoService;

	@Autowired
	OrderReqRecordMapper orderReqRecordMapper;

	@Override
	public PayResultData executeReq(MerchantInfo merchantInfo, PayConfigInfo payConfigInfo, PayConfigChannel channel,
									Map<String, String> param, List<PayConfigParameter> payConfigParameters) {
		if (payConfigInfo == null)
			return null;
		PayCallMethod callMethod = PayCallMethod.valueOf(payConfigInfo.getCallMethod());
		PayResultData result = new PayResultData(callMethod);
		String reqParam = payConfigInfo.getReqParam();
		List<PayReqParam> listParam = JSONUtil.toList(JSONUtil.parseArray(reqParam), PayReqParam.class);
		String jsonParam = channel.getJsonParam();
		if (StrUtil.isNotEmpty(jsonParam)) {
			List<PayReqParam> lst = JSONUtil.toList(JSONUtil.parseArray(jsonParam), PayReqParam.class);
			listParam.addAll(lst);

			for (PayReqParam payReqParam : lst) {
				if ("text".equals(payReqParam.getType())) {
					param.put(payReqParam.getKey(), payReqParam.getParam());
				}
			}

		}

		StringBuffer logText = new StringBuffer();
		logText.append(JSONUtil.toJsonStr(merchantInfo));
		Map<String, String> formData = new HashMap<String, String>();
		ThirdUtil.extracted(param, listParam, formData, logText);

		String link = payConfigInfo.getUrl();
		result.setLink(link);
		if (callMethod.compareTo(PayCallMethod.form) == 0) {
			result.setHtml(ThirdUtil.printlnForm(payConfigInfo.getTest() == 1, link, formData, logText));
		}

		return result;
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
