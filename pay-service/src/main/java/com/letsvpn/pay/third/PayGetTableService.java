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
import com.letsvpn.pay.util.PayHttpType;
import com.letsvpn.pay.vo.PayResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Service
@Slf4j
public class PayGetTableService extends BaseThirdService implements IPayThirdRequest, IPayNotifyHandle {

	@Override
	public PayResultData executeReq(MerchantInfo merchantInfo, PayConfigInfo payConfigInfo, PayConfigChannel channel,
									Map<String, String> param, List<PayConfigParameter> payConfigParameters) {
		if (payConfigInfo == null)
			return null;
		Integer payConfigId = payConfigInfo.getId();

		log.debug("-----------------------");
		StringBuffer logText = new StringBuffer();
		logText.append(JSONUtil.toJsonStr(merchantInfo)).append("\n");
		PayCallMethod callmethod = PayCallMethod.valueOf(payConfigInfo.getCallMethod());
		PayResultData result = new PayResultData(callmethod);

		Map<String, String> formData = new HashMap<String, String>();
		ThirdUtil.extracted2(param, payConfigParameters, formData, logText);
		log.debug("---------------------1-");
		for (Entry<String, String> payReqParam : formData.entrySet()) {
			log.debug("{}", payReqParam);
		}
		log.debug("--------------------2--");
		String reqLink = payConfigInfo.getUrl();

		Map<String, Object> formData1 = new HashMap<String, Object>();
		for (Entry<String, String> payReqParam : formData.entrySet()) {
			formData1.put(payReqParam.getKey(), payReqParam.getValue());
		}

		long now = System.currentTimeMillis();
		String resultText = null;
		try {
			log.debug("reqLink:{}", reqLink);

			StringBuffer sb = new StringBuffer();
			for (Entry<String, String> payReqParam : formData.entrySet()) {
				sb.append(payReqParam.getKey() + "=" + payReqParam.getValue() + "&");
			}
			resultText = reqLink + "?" + sb.toString().substring(0, sb.length() - 1);
			log.debug("resultText:---{}", resultText);
			result.setLink(resultText);

			String etype = channel.getExtractType();
			PayConfigChannelExtractType type = PayConfigChannelExtractType.valueOf(etype);
			if (type.compareTo(PayConfigChannelExtractType.url) == 0) {
				HttpRequest hr = HttpRequest.get(result.getLink()).timeout(ThirdUtil.HTTP_REQUEST_TIMEOUT);
				resultText = hr.executeAsync().body();
				log.debug("resultText:--2-", resultText);
				result.setLink(resultText);
			} else if (type.compareTo(PayConfigChannelExtractType.json) == 0) {
				HttpRequest hr = HttpRequest.get(result.getLink()).timeout(ThirdUtil.HTTP_REQUEST_TIMEOUT);
				if (PayHttpType.valueOf("raw").name().equals(payConfigInfo.getHttpType())) {// json格式请求
					hr.header("Accept", "application/json");
				}

				resultText = hr.executeAsync().body();
				JSONObject json = JSONUtil.parseObj(resultText);

				String redirect = getJsonField(channel, json);
				if (StrUtil.isEmpty(redirect)) {
					throw new WanliException(  resultText );
				}
				result.setLink(redirect);
			} else if (type.compareTo(PayConfigChannelExtractType.jsonDecoder) == 0) {
				HttpRequest hr = HttpRequest.get(result.getLink()).timeout(ThirdUtil.HTTP_REQUEST_TIMEOUT);
				resultText = hr.executeAsync().body();
				log.debug("resultText:--4-{}", resultText);
				JSONObject json = JSONUtil.parseObj(resultText);
				String redirect = getJsonField(channel, json);

				log.debug("resultText:--redirect-{}", redirect);
				if (StrUtil.isEmpty(redirect)) {
					throw new WanliException(  resultText );
				}
				result.setLink(URLDecoder.decode(redirect, "utf-8"));
			} else if (type.compareTo(PayConfigChannelExtractType.jsonDecoderHtml) == 0) {
				HttpRequest hr = HttpRequest.get(result.getLink()).timeout(ThirdUtil.HTTP_REQUEST_TIMEOUT);
				resultText = hr.executeAsync().body();
				log.debug("resultText:--6-{}", resultText);
				JSONObject json = JSONUtil.parseObj(resultText);
				String redirect = getJsonField(channel, json);

				log.debug("resultText:--redirect-{}", redirect);
				if (StrUtil.isEmpty(redirect)) {
					throw new WanliException(  resultText );
				}

				result.setMethod(PayCallMethod.form);
				result.setHtml(URLDecoder.decode(redirect, "utf-8"));

			}else if(type.compareTo(PayConfigChannelExtractType.href) == 0){
				result.setLink(resultText);
			}
			// callmethod
		} catch (WanliException e) {
			log.error(e.getMessage(), e);
			orderBuildErrorService.add(merchantInfo.getPlatformId(), payConfigInfo.getId(), channel.getId(),
					channel.getTitle(), e.getMessage(), resultText, this.getClass().getSimpleName());
			if (payConfigInfo.getTest() == 1) {
				throw e;
			} else {
				long reqTime = System.currentTimeMillis() - now;
				extracted(merchantInfo, payConfigInfo, channel, reqLink, formData1, resultText, reqTime, e.getMessage(),
						logText, 1);

				throw new WanliException(5011,"该支付异常1,请换其他支付");
			}
		} catch (Exception e) {
			orderBuildErrorService.add(merchantInfo.getPlatformId(), payConfigInfo.getId(), channel.getId(),
					channel.getTitle(), e.getMessage(), resultText, this.getClass().getSimpleName());
			long reqTime = System.currentTimeMillis() - now;
			extracted(merchantInfo, payConfigInfo, channel, reqLink, formData1, resultText, reqTime, e.getMessage(),
					logText, 1);

			log.error(e.getMessage(), e);
			throw new WanliException(5012,"该支付异常2,请换其他支付");
		}
		long reqTime = System.currentTimeMillis() - now;
		extracted(merchantInfo, payConfigInfo, channel, reqLink, formData1, resultText, reqTime, "", logText, 0);

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
			// agentAcct={agentAcct}&agentOrderId={agentOrderId}&amount={agentOrderId}&payAmount={payAmount}&status={status}&key={__privateKey}
		}
		log.debug("paramsMap{}", paramsMap);
		return true;

	}

}
