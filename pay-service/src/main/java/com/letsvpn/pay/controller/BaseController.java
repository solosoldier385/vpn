package com.letsvpn.pay.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;

import com.letsvpn.pay.entity.PayConfigInfo;
import com.letsvpn.pay.entity.PayPlatformInfo;
import com.letsvpn.pay.mapper.ext.ExtPayPlatformInfoMapper;
import com.letsvpn.pay.util.BaseWanli;
import com.letsvpn.pay.util.PayUtil;
import com.letsvpn.pay.util.SignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Aiden
 */

public class BaseController extends BaseWanli {

	@Autowired
	ExtPayPlatformInfoMapper extPayPlatformInfo;

	@Value("${wanli.sign.check:1}")
	int check;
//	@Value("${wanli.ipdepth:3}")
//	int ipdepth;

	protected PayPlatformInfo validateSign(Map<String, String> paramsMap) {
		String platformNo = MapUtil.getStr(paramsMap, "pf");
		PayPlatformInfo info = getPayPlatformInfo(platformNo);
		String sign = MapUtil.getStr(paramsMap, "sign");
		PayUtil.validate(StrUtil.isEmpty(info.getSecretKey()), "该账号的秘钥未配置", 1012);
		String key = info.getSecretKey();
		if (check == 1) {
			long time = MapUtil.getLong(paramsMap, "time", 0L);
			if (time > 0) {
				validate(time < System.currentTimeMillis() - 1000 * 60 * 20, "请求已过期", 1013);
			}
			PayUtil.validate(!SignUtil.verify(paramsMap, key, sign), "验证失败", 1014);
		}
		return info;
	}

	protected PayPlatformInfo getPayPlatformInfo(String platformNo) {
		PayPlatformInfo info = extPayPlatformInfo.getPayPlatformInfo(platformNo);
		PayUtil.validateParam(info == null, 1010, String.valueOf(platformNo));
		PayUtil.validate(info.getNullify() != 0, "该账号已经禁用", 1011);
		return info;
	}

	protected PayPlatformInfo getPayPlatformInfoId(int platformNo) {
		PayPlatformInfo info = null;
//		if (NumberUtil.isInteger(platformNo)) {
		info = extPayPlatformInfo.getPayPlatformInfoId(platformNo);
//		} else {
//			info = extPayPlatformInfo.getPayPlatformInfo(platformNo);
//		}

		PayUtil.validateParam(info == null, 1010, String.valueOf(platformNo));
		PayUtil.validate(info.getNullify() != 0, "该账号已经禁用", 1011);
		return info;
	}

	protected PayConfigInfo getPayConfigInfo(int id) {
		PayConfigInfo info = null;
		info = extPayPlatformInfo.getPayConfigInfo(id);
		PayUtil.validate(info == null, "该支付不存在", 8004, String.valueOf(id));
		PayUtil.validate(info.getNullify() != 0, "该账号已经禁用", 8009, String.valueOf(id));
		return info;
	}

	protected String getIp(HttpServletRequest request) {

		// X-Forwarded-For
		return ServletUtil.getClientIP(request);
	}

	protected List<String> proxyIps(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded == null)
			return new ArrayList<String>();
		int i = forwarded.indexOf(",");
		if (i == -1)
			return null;
		forwarded = forwarded.substring(i + 1);
		return StrUtil.split(forwarded.replaceAll(" ", ""), ",".charAt(0));
	}

	protected List<String> proxyIps2(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded == null) {
			ArrayList<String> list = new ArrayList<String>();
			list.add(getIp(request));
			return list;
		}
		return StrUtil.split(forwarded.replaceAll(" ", ""), ",".charAt(0));
	}

	protected void validate(boolean flag, String msg, int code) {
		PayUtil.validate(flag, msg, code);
	}

	protected void validateParam(boolean flag, int code, String... param) {
		PayUtil.validateParam(flag, code, param);
	}
//	protected void validate(boolean flag, String msg) {
//		PayUtil.validate(flag, msg);
//	}

}
