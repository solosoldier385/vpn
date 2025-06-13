package com.letsvpn.pay.third;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.letsvpn.pay.dto.IPayNotifyHandle;
import com.letsvpn.pay.dto.IPayThirdRequest;
import com.letsvpn.pay.entity.*;
import com.letsvpn.pay.exception.WanliException;
import com.letsvpn.pay.mapper.OrderReqRecordMapper;
import com.letsvpn.pay.service.core.OrderBuildErrorService;
import com.letsvpn.pay.service.core.OrderInfoService;
import com.letsvpn.pay.third.util.ThirdUtil;
import com.letsvpn.pay.util.PayCallMethod;
import com.letsvpn.pay.util.PayConfigChannelExtractType;
import com.letsvpn.pay.vo.PayReqParam;
import com.letsvpn.pay.vo.PayResultData;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Service
@Slf4j
public class PayBodyPostService implements IPayThirdRequest, IPayNotifyHandle {
	@Autowired
	OrderInfoService orderInfoService;
	@Autowired
	OrderReqRecordMapper orderReqRecordMapper;
//	@Autowired
//	TelegramService telegramService;

	@Autowired
	OrderBuildErrorService orderBuildErrorService;

	@Override
	public PayResultData executeReq(MerchantInfo merchantInfo, PayConfigInfo payConfigInfo, PayConfigChannel channel,
									Map<String, String> param, List<PayConfigParameter> payConfigParameters) {
		if (payConfigInfo == null)
			return null;
		log.debug("-----------------------");
		StringBuffer logText = new StringBuffer();
		logText.append(JSONUtil.toJsonStr(merchantInfo)).append("\n");
		PayCallMethod callmethod = PayCallMethod.valueOf(payConfigInfo.getCallMethod());
		PayResultData result = new PayResultData(callmethod);
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
		Map<String, String> formData = new HashMap<String, String>();
		ThirdUtil.extracted(param, listParam, formData, logText);
		log.debug("---------------------1-");
		for (Entry<String, String> payReqParam : formData.entrySet()) {
			log.debug("{}", payReqParam);
		}
		log.debug("--------------------2--");
//		result.setFormData(formData);
		String reqLink = payConfigInfo.getUrl();
//		result.setRedirect(link);
//		result.setTest(false);
		if (payConfigInfo.getTest() == 1) {
//			result.setTest(true);
		}

		Map<String, Object> formData1 = new HashMap<String, Object>();
		for (Entry<String, String> payReqParam : formData.entrySet()) {
			formData1.put(payReqParam.getKey(), payReqParam.getValue());
		}

		long now = System.currentTimeMillis();
		String resultText = null;
		try {
			log.debug("reqLink:{}", reqLink);
			resultText = HttpRequest.post(reqLink).form(formData1).timeout(ThirdUtil.HTTP_REQUEST_TIMEOUT)// 超时，毫秒
					.executeAsync().body();
			log.debug("resultText:{}", resultText);
			String etype = channel.getExtractType();
			if (StrUtil.isEmpty(etype)) {
				etype = PayConfigChannelExtractType.html.name();
			}
			PayConfigChannelExtractType type = PayConfigChannelExtractType.valueOf(etype);
			if (type.compareTo(PayConfigChannelExtractType.html) == 0) {
				result.setHtml(resultText);
			} else if (type.compareTo(PayConfigChannelExtractType.json) == 0) {
				JSONObject json = JSONUtil.parseObj(resultText);
				String[] field = channel.getExtractField().split("\\.");
				String redirect = null;
				if (field.length == 1) {
					redirect = json.getStr(field[0]);
				} else if (field.length == 2) {
					redirect = json.getJSONObject(field[0]).getStr(field[1]);
				} else if (field.length == 3) {
					redirect = json.getJSONObject(field[0]).getJSONObject(field[1]).getStr(field[2]);
				} else {
					redirect = "null?";
				}
				result.setLink(redirect);
			} else {

			}

			// callmethod
		} catch (Exception e) {
			long reqTime = System.currentTimeMillis() - now;
			extracted(merchantInfo, payConfigInfo, channel, reqLink, formData1, resultText, reqTime, e.getMessage(),
					logText, 1);
			String text = channel.getId() + ":" + channel.getTitle() + " 平台ID:" + merchantInfo.getPlatformId() + " 网关:"
					+ reqLink + " 错误:" + e.getMessage();
			log.error(text, e);
			orderBuildErrorService.add(merchantInfo.getPlatformId(), payConfigInfo.getId(), channel.getId(),
					channel.getTitle(), e.getMessage(), resultText, this.getClass().getSimpleName());
//			telegramService.sendMsg(RedisConstant.redis_root_telegram_error, text);
			throw new WanliException(5011, "该支付异常1,请换其他支付");
		}
		long reqTime = System.currentTimeMillis() - now;
		extracted(merchantInfo, payConfigInfo, channel, reqLink, formData1, resultText, reqTime, "", logText, 0);

		return result;
	}

	private void extracted(MerchantInfo merchantInfo, PayConfigInfo payConfigInfo, PayConfigChannel channel,
			String reqLink, Map<String, Object> formData1, String resultText, long reqTime, String errorText,
			StringBuffer logText, Integer error) {
		OrderReqRecord record = new OrderReqRecord();
		record.setBodyText(formData1.toString());
		record.setReqLink(reqLink);
		record.setCreateTime(new Date());
		record.setPayConfigChannelId(channel.getId());
		record.setPayConfigChannelName(channel.getTitle());
		record.setPayConfigId(payConfigInfo.getId());
		record.setPayConfigName(payConfigInfo.getTitle());
		record.setPlatformId(merchantInfo.getPlatformId());
		record.setReqTime(reqTime);
		record.setError(error);
		record.setResultText(resultText);
		record.setErrorText(errorText);
		record.setLogText(logText.toString());
		orderReqRecordMapper.insert(record);
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
