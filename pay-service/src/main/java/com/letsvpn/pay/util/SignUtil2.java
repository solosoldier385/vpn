package com.letsvpn.pay.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class SignUtil2 {

	public static StringBuffer subString(TreeMap<String, ?> body, String ljf) {

		StringBuffer sff = new StringBuffer();
		for (Entry<String, ?> data : body.entrySet()) {

			if (data.getValue().toString().length() > 0)

				sff.append(ljf).append(data.getKey()).append("=").append(data.getValue().toString());
		}
		if (sff.indexOf(ljf) == 0) {
			sff.delete(0, 1);
		}
		return sff;
	}

	public static String signMap(Map<String, ?> body, String key, String signName, String ljf) {
		TreeMap<String, ?> map = new TreeMap<>(body);
		map.remove(signName);
		StringBuffer sff = subString(map, ljf);

		logger.info("verify.sff:{},key:{}", sff, key);
		logger.info("verify:{} ", sff.toString() + key);

		return SecureUtil.md5(sff.toString() + key);
	}

	public static String signMapsha1(Map<String, ?> body, String key, String signName, String ljf) {
		TreeMap<String, ?> map = new TreeMap<>(body);
		map.remove(signName);
		StringBuffer sff = subString(map, ljf);

		logger.info("verify.sff:{},key:{}", sff, key);

		return SecureUtil.sha1(sff.toString() + key);
	}

	private static final Logger logger = LoggerFactory.getLogger(SignUtil2.class);

	public static boolean verify(JSONObject body, String key, String sign, String signName, String ljf) {
		String md5 = signMap(body, key, signName, ljf);
		logger.info("verify.md5:{}", md5);
		return md5.equalsIgnoreCase(sign);
	}

	public static boolean verifysha1(JSONObject body, String key, String sign, String signName, String ljf) {
		String md5 = signMapsha1(body, key, signName, ljf);
		logger.info("verify.md5:{}", md5);
		return md5.equalsIgnoreCase(sign);
	}

}
