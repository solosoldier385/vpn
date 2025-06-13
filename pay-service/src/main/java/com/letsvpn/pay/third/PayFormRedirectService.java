package com.letsvpn.pay.third;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.letsvpn.pay.dto.IPayNotifyHandle;
import com.letsvpn.pay.dto.IPayThirdRequest;
import com.letsvpn.pay.entity.MerchantInfo;
import com.letsvpn.pay.entity.PayConfigChannel;
import com.letsvpn.pay.entity.PayConfigInfo;
import com.letsvpn.pay.entity.PayConfigParameter;
import com.letsvpn.pay.exception.WanliException;
import com.letsvpn.pay.third.util.ThirdUtil;
import com.letsvpn.pay.util.PayCallMethod;
import com.letsvpn.pay.util.PayConfigChannelExtractType;
import com.letsvpn.pay.vo.PayResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 表单提交返回json处理服务类
 * 
 * @author AXian
 *
 */
@Service
@Slf4j
public class PayFormRedirectService extends BaseThirdService implements IPayThirdRequest, IPayNotifyHandle {

	@Override
	public PayResultData executeReq(MerchantInfo merchantInfo, PayConfigInfo payConfigInfo, PayConfigChannel channel,
									Map<String, String> param, List<PayConfigParameter> payConfigParameters) {
		if (payConfigInfo == null)
			return null;

		Integer payConfigId = payConfigInfo.getId();

		StringBuffer logText = new StringBuffer();
		PayCallMethod callMethod = PayCallMethod.valueOf(payConfigInfo.getCallMethod());
		PayResultData result = new PayResultData(callMethod);

		Map<String, String> formData = new HashMap<String, String>();
		ThirdUtil.extracted2(param, payConfigParameters, formData, logText);
		Map<String, Object> formData1 = new HashMap<String, Object>();
		for (Entry<String, String> payReqParam : formData.entrySet()) {
			formData1.put(payReqParam.getKey(), payReqParam.getValue());
		}

		String link = payConfigInfo.getUrl();
		long now = System.currentTimeMillis();
		String resultText = "";
		try {
			log.debug("reqLink:{}", link);
			HttpRequest hr = HttpRequest.post(link).timeout(ThirdUtil.HTTP_REQUEST_TIMEOUT);// 超时，毫秒
			hr = hr.form(formData1);
			resultText = hr.executeAsync().body();
			log.debug("resultText:{}", resultText);
			String etype = channel.getExtractType();
			PayConfigChannelExtractType type = PayConfigChannelExtractType.valueOf(etype);
			if (type.compareTo(PayConfigChannelExtractType.json) == 0) {
				JSONObject json = JSONUtil.parseObj(resultText);
				String redirect = ThirdUtil.getJsonField(channel, json);
				result.setLink(redirect);
			}
			if (type.compareTo(PayConfigChannelExtractType.html) == 0) {
				result.setLink(resultText);
			} else if (type.compareTo(PayConfigChannelExtractType.jsonToHtml) == 0) {
				JSONObject json = JSONUtil.parseObj(resultText);
				String redirect = getJsonField(channel, json);
				if (StrUtil.isEmpty(redirect)) {
					throw new WanliException(resultText);
				}
				result.setMethod(PayCallMethod.form);
				result.setHtml(redirect);
			}
		} catch (WanliException e) {
			log.error(e.getMessage(), e);
			orderBuildErrorService.add(merchantInfo.getPlatformId(), payConfigId, channel.getId(), channel.getTitle(),
					e.getMessage(), resultText, this.getClass().getSimpleName());

			if (payConfigInfo.getTest() == 1) {
				throw e;
			} else {
				long reqTime = System.currentTimeMillis() - now;
				extracted(merchantInfo, payConfigInfo, channel, link, formData1, resultText, reqTime, e.getMessage(),
						logText, 1);
				throw new WanliException(5011, "该支付异常1,请换其他支付");
			}
		} catch (Exception e) {
			orderBuildErrorService.add(merchantInfo.getPlatformId(), payConfigId, channel.getId(), channel.getTitle(),
					e.getMessage(), resultText, this.getClass().getSimpleName());
			long reqTime = System.currentTimeMillis() - now;
			extracted(merchantInfo, payConfigInfo, channel, link, formData1, resultText, reqTime, e.getMessage(),
					logText, 1);
			log.error(e.getMessage(), e);
			throw new WanliException(5012, "该支付异常2,请换其他支付");
		}
		extracted(merchantInfo, payConfigInfo, channel, payConfigInfo.getUrl(), formData1, resultText, 0, "", logText,
				0);
		return result;
	}

	@Override
	public void paramExtraction(Map<String, String> paramMap, MerchantInfo merchantInfo) {

	}

	@Override
	public boolean signCheck(Map<String, String> paramsMap, List<PayConfigParameter> payConfigParameters,
			StringBuffer logText) {
		Map<String, String> formData = new HashMap<String, String>();
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
		}
		log.debug("paramsMap{}", paramsMap);
		return true;

	}

}
