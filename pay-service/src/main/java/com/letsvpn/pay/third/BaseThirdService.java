package com.letsvpn.pay.third;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.letsvpn.pay.entity.MerchantInfo;
import com.letsvpn.pay.entity.OrderReqRecord;
import com.letsvpn.pay.entity.PayConfigChannel;

import com.letsvpn.pay.entity.PayConfigInfo;
import com.letsvpn.pay.exception.WanliException;
import com.letsvpn.pay.mapper.OrderReqRecordMapper;
import com.letsvpn.pay.service.core.OrderBuildErrorService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

public class BaseThirdService {
	@Autowired
	OrderBuildErrorService orderBuildErrorService;
	@Autowired
	OrderReqRecordMapper orderReqRecordMapper;

	protected void add(Integer platformId, Integer payConfigId, Long payConfigChannelId, String channelName,
			String errorText, String resultText, String className) {

		orderBuildErrorService.add(platformId, payConfigId, payConfigChannelId, channelName, errorText, resultText,
				className);

	}

	protected void extracted(MerchantInfo merchantInfo, PayConfigInfo payConfigInfo, PayConfigChannel channel,
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

	protected void extracted2(MerchantInfo merchantInfo, PayConfigInfo payConfigInfo, PayConfigChannel channel,
			String reqLink, Map<String, String> formData1, String resultText, long reqTime, String errorText,
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

	/**
	 * 提取json变量
	 *
	 * @param channel
	 * @param json
	 * @return
	 */
	protected String getJsonField(PayConfigChannel channel, JSONObject json) {
		String[] field = channel.getExtractField().split("\\.");
		String redirect = "";
		if (StrUtil.isNotEmpty(channel.getExtractPrefix())) {
			redirect = channel.getExtractPrefix();
		}
		if (field.length == 1) {
			redirect += json.getStr(field[0]);
		} else if (field.length == 2) {
			redirect += json.getJSONObject(field[0]).getStr(field[1]);
		} else if (field.length == 3) {
			redirect += json.getJSONObject(field[0]).getJSONObject(field[1]).getStr(field[2]);
		} else if (field.length == 4) {
			redirect += json.getJSONObject(field[0]).getJSONObject(field[1]).getJSONObject(field[2]).getStr(field[3]);
		} else if (field.length == 5) {
			redirect += json.getJSONObject(field[0]).getJSONObject(field[1]).getJSONObject(field[2])
					.getJSONObject(field[3]).getStr(field[4]);
		} else {
			redirect = "";
		}

		if (StrUtil.equals(redirect, "null")) {
			redirect = "";
		}
		return redirect;
	}

	/**
	 * KYC 验证表单amount金额
	 * @param param
	 * @param amount
	 */
	protected void validateAmount(Map<String, String> param, String amount) {
		String amountDex = param.get("__amountDex");//解密下单金额
		if(!StrUtil.equals(amountDex, amount)){
			throw new WanliException(404, "amount not matched");
		}
	}
}
