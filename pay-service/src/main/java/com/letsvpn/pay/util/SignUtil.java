package com.letsvpn.pay.util;

import cn.hutool.crypto.SecureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class SignUtil {

	public static StringBuffer subString(Map<String, String> body) {
		Map<String, Object> map = new TreeMap<String, Object>(body);
		StringBuffer sff = new StringBuffer();
		for (Entry<String, Object> data : map.entrySet()) {
			sff.append("&").append(data.getKey()).append("=").append(data.getValue());
		}
		if (sff.indexOf("&") == 0) {
			sff.delete(0, 1);
		}
		return sff;
	}

	public static String signMap(Map<String, String> body, String key) {
		Map<String, String> map = new TreeMap<String, String>(body);
		map.remove("sign");
		StringBuffer sff = subString(map);
		return SecureUtil.md5(sff.toString() + key);
	}

	private static final Logger logger = LoggerFactory.getLogger(SignUtil.class);

	public static boolean verify(Map<String, String> body, String key, String sign) {
		/*String cid = MapUtils.getString(body, "cid");
		String easypaisa = "80275";
		String jazzcash = "80276";
		if("81107".equals(cid)||"81109".equals(cid)||"81111".equals(cid)||"81113".equals(cid)){
			body.put("cid", easypaisa);
		}
		if("81108".equals(cid)||"81110".equals(cid)||"81112".equals(cid)||"81114".equals(cid)){
			body.put("cid", jazzcash);
		}*/
		String md5 = signMap(body, key);
		boolean result = md5.equalsIgnoreCase(sign);
		if (!result) {
			logger.info("verify.md5:{}", md5);
		}
//		body.put("cid", cid);
		return result;
	}

}
