package com.letsvpn.pay.third;

import cn.hutool.core.codec.Base64;
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
import com.letsvpn.pay.service.core.OrderBuildErrorService;
import com.letsvpn.pay.service.core.OrderInfoService;
import com.letsvpn.pay.third.util.ThirdUtil;
import com.letsvpn.pay.util.PayCallMethod;
import com.letsvpn.pay.util.PayConfigChannelExtractType;
import com.letsvpn.pay.util.PayHttpType;
import com.letsvpn.pay.vo.PayResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public abstract class BaseBodyTableService extends BaseThirdService implements IPayThirdRequest, IPayNotifyHandle {

	@Autowired
	OrderInfoService orderInfoService;
	// @Autowired
//	PayConfigParameterService payConfigParameterService;
//	@Autowired
//	TelegramService telegramService;
	@Autowired
	OrderBuildErrorService orderBuildErrorService;

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

		log.debug("-----------------------");
		StringBuffer logText = new StringBuffer();
		logText.append(JSONUtil.toJsonStr(merchantInfo)).append("\n");
		PayCallMethod callmethod = PayCallMethod.valueOf(payConfigInfo.getCallMethod());
		PayResultData result = new PayResultData(callmethod);

		Map<String, String> formData = new HashMap<String, String>();
		extracted(param, payConfigParameters, logText, formData, merchantInfo);
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
			if (StrUtil.isEmpty(payConfigInfo.getHttpType())) {
				payConfigInfo.setHttpType(PayHttpType.form_data.name());
			}
			PayHttpType httpType = PayHttpType.valueOf(payConfigInfo.getHttpType());

			HttpRequest hr = HttpRequest.post(reqLink).timeout(ThirdUtil.HTTP_REQUEST_TIMEOUT);// 超时，毫秒

			if (PayHttpType.form_data.equals(httpType)) {// formData格式请求
				hr = hr.form(formData1);
			} else if (PayHttpType.raw.equals(httpType)) {// json格式请求
				hr = hr.body(new JSONObject(formData1).toString());
			} else if (PayHttpType.xml.equals(httpType)) {// xml格式请求
				JSONObject jsonObject = new JSONObject(formData1);
				String xmlStr = JSONUtil.toXmlStr(jsonObject);
				StringBuffer sb = new StringBuffer();
				sb.append("<xml>").append(xmlStr).append("</xml>");
				log.info("xml{}", sb.toString());
				hr = hr.body(sb.toString());
			} else {
				throw new WanliException(5000, "httpType配置错误", "httpType");
			}

			resultText = hr.executeAsync().body();
			log.debug("resultText:{}", resultText);
			String etype = channel.getExtractType();
			if (StrUtil.isEmpty(etype)) {
				etype = PayConfigChannelExtractType.html.name();
			}
			PayConfigChannelExtractType type = PayConfigChannelExtractType.valueOf(etype);
			if (type.compareTo(PayConfigChannelExtractType.html) == 0) {
				result.setHtml(resultText);
			} else if (type.compareTo(PayConfigChannelExtractType.url) == 0) {
				result.setLink(resultText);
			} else if (type.compareTo(PayConfigChannelExtractType.json) == 0) {
				JSONObject json = JSONUtil.parseObj(resultText);
				String redirect = getJsonField(channel, json);
				if (StrUtil.isEmpty(redirect)) {
					throw new WanliException(resultText);
				}
				result.setLink(redirect);
			} else if (type.compareTo(PayConfigChannelExtractType.jsonToHtml) == 0) {
				JSONObject json = JSONUtil.parseObj(resultText);
				String redirect = getJsonField(channel, json);
				if (StrUtil.isEmpty(redirect)) {
					throw new WanliException(resultText);
				}
				result.setMethod(PayCallMethod.form);
				result.setHtml(redirect);
			} else if (type.compareTo(PayConfigChannelExtractType.xml) == 0) {
				resultText = resultText.replaceAll("<xml>", "");
				resultText = resultText.replaceAll("</xml>", "");
				JSONObject json = JSONUtil.xmlToJson(resultText);
				String redirect = getJsonField(channel, json);
				if (StrUtil.isEmpty(redirect)) {
					throw new WanliException(resultText);
				}
				result.setLink(redirect);
			} else if (type.compareTo(PayConfigChannelExtractType.jsonDecoder) == 0) {
				JSONObject json = JSONUtil.parseObj(resultText);
				String redirect = getJsonField(channel, json);
				if (StrUtil.isEmpty(redirect)) {
					throw new WanliException(resultText);
				}
				result.setLink(URLDecoder.decode(redirect, "utf-8"));
			} else if (type.compareTo(PayConfigChannelExtractType.href) == 0) {
				Pattern pattern_a = Pattern
						.compile("<a[^>]*HREF=(\\\"([^\\\"]*)\\\"|\\'([^\\']*)\\'|([^\\\\s>]*))[^>]*>(.*?)");
				Matcher matcher_a = pattern_a.matcher(resultText);
				String redirect = "";
				while (matcher_a.find()) {
					for (int i = 0; i < matcher_a.groupCount(); i++) {
						if (StrUtil.isNotEmpty(matcher_a.group(i))) {
							log.info("href:{}", matcher_a.group(i));
							redirect = matcher_a.group(i);
						}
					}
				}

				if (StrUtil.isEmpty(redirect)) {
					throw new WanliException(resultText);
				}
				result.setLink(redirect);
			} else if (type.compareTo(PayConfigChannelExtractType.base64Decoder) == 0) {
				JSONObject json = JSONUtil.parseObj(resultText);
				String redirect = getJsonField(channel, json);
				if (StrUtil.isEmpty(redirect)) {
					throw new WanliException(resultText);
				}
				result.setLink(Base64.decodeStr(redirect, "utf-8"));
			}

			// callmethod
		} catch (WanliException e) {
			log.error(e.getMessage(), e);
			orderBuildErrorService.add(merchantInfo.getPlatformId(), payConfigId, channel.getId(), channel.getTitle(),
					e.getMessage(), resultText, this.getClass().getSimpleName());

			if (payConfigInfo.getTest() == 1) {
				throw e;
			} else {
				long reqTime = System.currentTimeMillis() - now;
				extracted(merchantInfo, payConfigInfo, channel, reqLink, formData1, resultText, reqTime, e.getMessage(),
						logText, 1);
//				String text = channel.getId() + ":" + channel.getTitle() + " 平台ID:" + merchantInfo.getPlatformId()
//						+ " 网关:" + reqLink + " 错误:" + e.getMessage();
//				telegramService.sendMsg(RedisConstant.redis_root_telegram_error, text);
				throw new WanliException(5011, "该支付异常1,请换其他支付");
			}
		} catch (Exception e) {
			orderBuildErrorService.add(merchantInfo.getPlatformId(), payConfigId, channel.getId(), channel.getTitle(),
					e.getMessage(), resultText, this.getClass().getSimpleName());
			long reqTime = System.currentTimeMillis() - now;
			extracted(merchantInfo, payConfigInfo, channel, reqLink, formData1, resultText, reqTime, e.getMessage(),
					logText, 1);
//			String text = channel.getId() + ":" + channel.getTitle() + " 平台ID:" + merchantInfo.getPlatformId() + " 网关:"
//					+ reqLink + " 错误:" + e.getMessage();
//			telegramService.sendMsg(RedisConstant.redis_root_telegram_error, text);
			log.error(e.getMessage(), e);
			throw new WanliException(5012, "该支付异常2,请换其他支付");
		}
		long reqTime = System.currentTimeMillis() - now;
		extracted(merchantInfo, payConfigInfo, channel, reqLink, formData1, resultText, reqTime, "", logText, 0);

		return result;
	}

	public abstract void extracted(Map<String, String> param, List<PayConfigParameter> payConfigParameters,
			StringBuffer logText, Map<String, String> formData, MerchantInfo merchantInfo);

}
