package com.letsvpn.pay.third.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.URLEncoder;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.crypto.symmetric.DES;
import cn.hutool.crypto.symmetric.RC4;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.letsvpn.pay.entity.*;
import com.letsvpn.pay.vo.PayReqParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

@Slf4j
public class ThirdUtil {

	public static final int HTTP_REQUEST_TIMEOUT = 10 * 1000;

	public static final int HTTP_REQUEST_LONG_TIMEOUT = 20 * 1000;

	public static void extracted(Map<String, String> param, List<PayReqParam> listParam, Map<String, String> formData,
								 StringBuffer logText) {

		for (PayReqParam payReqParam : listParam) {
			String type = payReqParam.getType();
			if (type == null) {
				type = "";
			}
			// md5Lower：md5算法加密转小写；md5Upper：md5算法加密转大写;sha1:sha1算法加密;lowerMd5:转小写再md5算法加密;upperMd5:转大写再md5算法加密;
			// "md5Lower$：md5算法加密转小写$后面追加加密次数；md5Upper$：md5算法加密转大写$后面追加加密次数
			// md5TwoLower：md5(md5（商户号+金额+订单号+支付类型+回调地址）+key)32位小写；md5TwoUpper：md5(md5（商户号+金额+订单号+支付类型+回调地址）+key)32位大写；
			// json：数据转json字符串;hmacMd5:hmacMd5算法加密；base64：base64加密;
			if ("md5Lower".equals(type) || "md5Upper".equals(type) || "sha1".equals(type) || "lowerMd5".equals(type)
					|| "upperMd5".equals(type) || type.indexOf("md5Lower$") > -1 || type.indexOf("md5Upper$") > -1
					|| "md5TwoLower".equals(type) || "md5TwoUpper".equals(type) || "json".equals(type)
					|| "hmacMd5".equals(type) || "base64".equals(type) || "sha1Upper".equals(type)) {
				List<String> lst = new ArrayList<String>();
				for (String key : payReqParam.getMd5().split(",")) {
					lst.add(param.get(key));
				}
				String value = MessageFormat.format(payReqParam.getParam(), lst.toArray());
				log.debug("value:{}", value);
				logText.append(",value:").append(value).append("\n");
				if ("md5Lower".equals(payReqParam.getType())) {
					value = SecureUtil.md5(value).toLowerCase();
				}
				if ("lowerMd5".equals(payReqParam.getType())) {
					value = SecureUtil.md5(value.toLowerCase());
				}
				if ("md5Upper".equals(payReqParam.getType())) {
					value = SecureUtil.md5(value).toUpperCase();
				}
				if ("upperMd5".equals(payReqParam.getType())) {
					value = SecureUtil.md5(value.toUpperCase());
				}
				if ("sha1".equals(payReqParam.getType())) {
					value = SecureUtil.sha1(value);
				}
				if ("sha1Upper".equals(payReqParam.getType())) {
					value = SecureUtil.sha1(value).toUpperCase();
				}
				if (type.indexOf("md5Lower$") > -1) {
					int num = Integer.parseInt(type.split("\\$")[1]);
					for (int i = 0; i < num; i++) {
						value = SecureUtil.md5(value);
					}
					value = value.toLowerCase();
				}
				if (type.indexOf("md5Upper$") > -1) {
					int num = Integer.parseInt(type.split("\\$")[1]);
					for (int i = 0; i < num; i++) {
						value = SecureUtil.md5(value);
					}
					value = value.toUpperCase();
				}
				if ("md5TwoLower".equals(payReqParam.getType())) {
					value = SecureUtil.md5(value);
					value = SecureUtil.md5(value + param.get("privateKey")).toLowerCase();
				}
				if ("md5TwoUpper".equals(payReqParam.getType())) {
					value = SecureUtil.md5(value);
					value = SecureUtil.md5(value + param.get("privateKey")).toUpperCase();
				}
				if ("hmacMd5".equals(payReqParam.getType())) {
					HMac hMac = SecureUtil.hmacMd5(param.get("privateKey"));
					value = hMac.digestHex(value);
				}
				if ("base64".equals(payReqParam.getType())) {
					value = Base64.encode(value.getBytes());
				}
				log.debug("sign:{}", value);
				logText.append(",sign:").append(value).append("\n");
				formData.put(payReqParam.getKey(), value);

				// -------------------
				// -------2020-1-30---不能直接替换--先判断等于null才能加入----------
				// -------------------
				if (param.get(payReqParam.getKey()) == null) {
					param.put(payReqParam.getKey(), value);
				}

			} else if ("text".equals(payReqParam.getType())) {
				formData.put(payReqParam.getKey(), payReqParam.getParam());
			} else if ("URLEncoder".equals(payReqParam.getType())) {
				formData.put(payReqParam.getKey(),
						new URLEncoder().encode(param.get(payReqParam.getParam()), Charset.forName("UTF-8")));
			} else {
				formData.put(payReqParam.getKey(), param.get(payReqParam.getParam()));
			}
		}
	}

	public static void extracted2(Map<String, String> param, List<PayConfigParameter> payConfigParameters,
			Map<String, String> formData, StringBuffer logText) {

		for (PayConfigParameter payReqParam : payConfigParameters) {
//			if (payReqParam.getPush() != 1) {
//				continue;
//			}
			String key = payReqParam.getPayName();
			String payValue = payReqParam.getPayValue();
			String type = payReqParam.getType();
			getSign(param, formData, logText, "", key, payValue, type);
		}
	}

	// 表单提交
	public static String printlnForm(boolean test, String url, Map<String, String> formData, StringBuffer logText) {

		StringBuffer sb = new StringBuffer();
		sb.append("<html><body>\n<form action='").append(url.trim()).append("' method='post' name='formPost'>");
		for (Entry<String, String> obj : formData.entrySet()) {
			sb.append("\n<input type='hidden' name='").append(obj.getKey().trim()).append("' value='")
					.append(obj.getValue().trim() + "'>");
		}

		if (test) {

			sb.append("\n <input type='submit' value='TEST Submit'>");

		}
		sb.append("</form>");
		if (!test) {
			sb.append("\n<script>document.forms['formPost'].submit();</script>");
		} else {
			for (Entry<String, String> obj : formData.entrySet()) {
				sb.append("\n<p>").append(obj.getKey().trim()).append(" = ")
						.append(obj.getValue().trim() + "</p>");
			}

			sb.append("\n<br>test model:</br>"+url.trim());
			sb.append("\n</br><textarea style='width: 100%;height: 123px;' >" );
			sb.append(logText);
			sb.append("</textarea>\n");

		}
		sb.append("\n</body></html>");
		return sb.toString();
	}

	private static String getSign(Map<String, String> paramsMap, Map<String, String> notyfyData, StringBuffer logText,
			String encryptStr, String key, String payValue, String type) {
		if (type == null) {
			type = "";
		}
		// md5Lower：md5算法加密转小写；md5Upper：md5算法加密转大写;sha1:sha1算法加密;lowerMd5:转小写再md5算法加密;upperMd5:转大写再md5算法加密;
		// "md5Lower$：md5算法加密转小写$后面追加加密次数；md5Upper$：md5算法加密转大写$后面追加加密次数
		// md5TwoLower：md5(md5（商户号+金额+订单号+支付类型+回调地址）+key)32位小写；md5TwoUpper：md5(md5（商户号+金额+订单号+支付类型+回调地址）+key)32位大写；
		// json：数据转json字符串;hmacMd5:hmacMd5算法加密；base64:base64算法加密;rsa:加密;md5KeyLower:md5({商户key};md5({amount};{out_trade_no};{account_id};{callback_url}))
		// noSign：签名不进行处理，只获取签名串;base64decrypt:json串Base64解密;hmacSHA256:hmacSHA256加密
		// rc4ToHex:新增rc4加密算法
		if ("md5Lower".equals(type) || "md5Upper".equals(type) || "sha1".equals(type) || "lowerMd5".equals(type)
				|| "upperMd5".equals(type) || type.indexOf("md5Lower$") > -1 || type.indexOf("md5Upper$") > -1
				|| "md5TwoLower".equals(type) || "md5TwoUpper".equals(type) || "json".equals(type)
				|| "hmacMd5".equals(type) || "base64".equals(type) || "sha1Upper".equals(type) || "rsa".equals(type)
				|| "jsonBase64".equals(type) || "md5KeyLower".equals(type) || "noSign".equals(type)
				|| "base64decrypt".equals(type) || "hmacSHA256Upper".equals(type) || "hmacSHA256Lower".equals(type)
				|| "md5TwoLowertwo".equals(type) || "sha256Upper".equals(type) || "sha256Lower".equals(type)
				|| "desMd5".equals(type) || "md5ThreeLower".equals(type) || "md5ThreeUpper".equals(type)
				|| "base64Md5Upper".equals(type) || "rc4ToHex".equals(type) || "sha512Upper".equals(type)
				|| "sha512Lower".equals(type) || "hmacSHA256".equals(type) || "hmacSHA256Hex".equals(type)
				|| "hmacSHA1".equals(type) || "lowerSHA256Upper".equals(type) || "hmacSHA384Upper".equals(type)
				|| "hmacSHA384Lower".equals(type)||"SHA256WithRSA".equals(type)) {

 			for (Entry<String, String> map : paramsMap.entrySet()) {
				log.debug(map.getKey() + "-----" + map.getValue());
				payValue = payValue.replaceAll("\\{" + map.getKey() + "}", map.getValue());
			}
			log.debug("notifyParam:{}", payValue);
			logText.append(",value:").append(payValue).append("\n");
			if ("md5Lower".equals(type)) {
				payValue = SecureUtil.md5(payValue).toLowerCase();
			}
			if ("lowerMd5".equals(type)) {
				payValue = SecureUtil.md5(payValue.toLowerCase());
			}
			if ("md5Upper".equals(type)) {
				payValue = SecureUtil.md5(payValue).toUpperCase();
			}
			if ("upperMd5".equals(type)) {
				payValue = SecureUtil.md5(payValue.toUpperCase());
			}
			if ("sha1".equals(type)) {
				payValue = SecureUtil.sha1(payValue);
			}

			if ("sha1Upper".equals(type)) {
				payValue = SecureUtil.sha1(payValue).toUpperCase();
			}
			if (type.indexOf("md5Lower$") > -1) {
				int num = Integer.parseInt(type.split("\\$")[1]);
				for (int i = 0; i < num; i++) {
					payValue = SecureUtil.md5(payValue);
				}
				payValue = payValue.toLowerCase();
			}
			if (type.indexOf("md5Upper$") > -1) {
				int num = Integer.parseInt(type.split("\\$")[1]);
				for (int i = 0; i < num; i++) {
					payValue = SecureUtil.md5(payValue);
				}
				payValue = payValue.toUpperCase();
			}
			if ("md5TwoLower".equals(type)) {
				payValue = SecureUtil.md5(payValue);
				payValue = SecureUtil.md5(payValue + paramsMap.get("privateKey")).toLowerCase();
			}
			if ("md5TwoUpper".equals(type)) {
				payValue = SecureUtil.md5(payValue);
				payValue = SecureUtil.md5(payValue + paramsMap.get("privateKey")).toUpperCase();
			}
			if ("md5ThreeLower".equals(type)) {
				payValue = SecureUtil.md5(SecureUtil.md5(paramsMap.get("privateKey")) + SecureUtil.md5(payValue))
						.toLowerCase();
			}
			if ("md5ThreeUpper".equals(type)) {
				payValue = SecureUtil.md5(SecureUtil.md5(paramsMap.get("privateKey")) + SecureUtil.md5(payValue))
						.toUpperCase();
			}
			if ("json".equals(type)) {
//				paramsMap.put(key, payValue);
				if (paramsMap.get(key) == null) {
					paramsMap.put(key, payValue);
				}
			}
			if ("hmacMd5".equals(type)) {
				HMac hMac = SecureUtil.hmacMd5(paramsMap.get("privateKey"));
				payValue = hMac.digestHex(payValue);
			}
			if ("base64".equals(type)) {
				payValue = Base64.encode(payValue.getBytes());
			}
			if ("rsa".equals(type)) {
				RSA rsa = new RSA(paramsMap.get("privateKey1"), null);
				byte[] enStr = rsa.encrypt(payValue.getBytes(), KeyType.PrivateKey);
				payValue = new String(enStr);
			}
			if("SHA256WithRSA".equals(type)){
//				payValue = SHA256WithRSAUtils.buildRSAEncryptByPrivateKey(payValue,paramsMap.get("privateKey1"));

                try {
                    PrivateKey privateKey = getPrivateKey(paramsMap.get("privateKey1"));
                    payValue = sign(payValue, privateKey);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
			if ("jsonBase64".equals(type)) {
				payValue = Base64.encode(payValue.getBytes());
//				paramsMap.put(key, payValue);
				if (paramsMap.get(key) == null) {
					paramsMap.put(key, payValue);
				}

			}
			if ("md5KeyLower".equals(type)) {
				payValue = SecureUtil.md5(payValue);
				payValue = SecureUtil.md5(paramsMap.get("privateKey").toLowerCase() + ";" + payValue.toLowerCase())
						.toLowerCase();
			}
			if ("base64decrypt".equals(type)) {
				byte[] decode = Base64.decode(payValue.getBytes());
				String content = null;
				try {
					content = new String(decode, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				JSONObject parseObj = JSONUtil.parseObj(content);
				paramsMap.put(key, content);
				payValue = content;
				for (Entry<String, Object> jsonObj : parseObj.entrySet()) {
					if (parseObj.isNull(jsonObj.getKey())) {
						paramsMap.put(jsonObj.getKey(), "");
						notyfyData.put(jsonObj.getKey(), "");
					} else {
						paramsMap.put(jsonObj.getKey(), jsonObj.getValue().toString());
						notyfyData.put(jsonObj.getKey(), jsonObj.getValue().toString());
					}
				}
			}
			if ("hmacSHA256Upper".equals(type)) {
				HMac hMac = new HMac(HmacAlgorithm.HmacSHA256, StrUtil.utf8Bytes(paramsMap.get("privateKey")));
				payValue = hMac.digestHex(payValue).toUpperCase();
			}
			if ("hmacSHA256Lower".equals(type)) {
				HMac hMac = new HMac(HmacAlgorithm.HmacSHA256, StrUtil.utf8Bytes(paramsMap.get("privateKey")));
				payValue = hMac.digestHex(payValue).toLowerCase();
			}
			if ("hmacSHA384Lower".equals(type)) {
				HMac hMac = new HMac(HmacAlgorithm.HmacSHA384, StrUtil.utf8Bytes(paramsMap.get("privateKey")));
				payValue = hMac.digestHex(payValue).toLowerCase();
			}
			if ("hmacSHA384Upper".equals(type)) {
				HMac hMac = new HMac(HmacAlgorithm.HmacSHA384, StrUtil.utf8Bytes(paramsMap.get("privateKey")));
				payValue = hMac.digestHex(payValue).toUpperCase();
			}
			if ("hmacSHA256".equals(type)) {
				try {
					Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
					String secretKey = paramsMap.get("privateKey");
					SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
					sha256_HMAC.init(secret_key);
					payValue = org.apache.commons.codec.binary.Base64
							.encodeBase64String(sha256_HMAC.doFinal(payValue.getBytes()));
				} catch (InvalidKeyException e) {
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			}
			if ("hmacSHA256Hex".equals(type)) {
				try {
					Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
					String secretKey = paramsMap.get("privateKey2");
					SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
					sha256_HMAC.init(secret_key);
					payValue = Hex.encodeHexString(sha256_HMAC.doFinal(payValue.getBytes()));
				} catch (InvalidKeyException e) {
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			}
			if ("hmacSHA1".equals(type)) {
				try {
					Mac sha256_HMAC = Mac.getInstance("HmacSHA1");
					String secretKey = paramsMap.get("privateKey1");
					SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
					sha256_HMAC.init(secret_key);
					byte[] rawHmac = sha256_HMAC.doFinal(payValue.getBytes());
					payValue = Base64.encode(rawHmac);
				} catch (InvalidKeyException e) {
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			}
			if ("md5TwoLowertwo".equals(type)) {
				payValue = SecureUtil.md5(payValue);
				payValue = SecureUtil.md5(paramsMap.get("privateKey").toLowerCase() + payValue).toLowerCase();
			}
			if ("sha256Upper".equals(type)) {
				payValue = SecureUtil.sha256(payValue).toUpperCase();
			}
			if ("lowerSHA256Upper".equals(type)) {
				payValue = SecureUtil.sha256(payValue.toLowerCase()).toUpperCase();
			}
			if ("sha256Lower".equals(type)) {
				payValue = SecureUtil.sha256(payValue).toLowerCase();
			}
			if ("sha512Upper".equals(type)) {
				try {
					MessageDigest messageDigest = MessageDigest.getInstance("SHA-512"); // 创建SHA512类型的加密对象
					messageDigest.update(payValue.getBytes());
					byte[] bytes = messageDigest.digest();
					StringBuffer strHexString = new StringBuffer();
					for (int i = 0; i < bytes.length; i++) {
						String hex = Integer.toHexString(0xff & bytes[i]);
						if (hex.length() == 1) {
							strHexString.append('0');
						}
						strHexString.append(hex);
					}
					payValue = strHexString.toString().toUpperCase();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
					return null;
				}
			}
			if ("sha512Lower".equals(type)) {
				try {
					MessageDigest messageDigest = MessageDigest.getInstance("SHA-512"); // 创建SHA512类型的加密对象
					messageDigest.update(payValue.getBytes());
					byte[] bytes = messageDigest.digest();
					StringBuffer strHexString = new StringBuffer();
					for (int i = 0; i < bytes.length; i++) {
						String hex = Integer.toHexString(0xff & bytes[i]);
						if (hex.length() == 1) {
							strHexString.append('0');
						}
						strHexString.append(hex);
					}
					payValue = strHexString.toString().toLowerCase();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
					return null;
				}
			}
			if ("desMd5".equals(type)) {
				DES des = new DES(paramsMap.get("privateKey").getBytes());
				payValue = des.encryptBase64(payValue);
				payValue = SecureUtil.md5(payValue);
			}
			if ("base64Md5Upper".equals(type)) {
				payValue = Base64.encode(SecureUtil.md5(payValue)).toUpperCase();
			}
			if ("rc4ToHex".equals(type)) {
				RC4 rc4 = SecureUtil.rc4(paramsMap.get("privateKey"));
				payValue = rc4.encryptHex(payValue.getBytes());
			}

			encryptStr = payValue;
			log.debug("sign:{}", payValue);
			logText.append(",sign:").append(payValue).append("\n");
			notyfyData.put(key, payValue);

			// -------------------
			// -------2020-1-30---不能直接替换--先判断等于null才能加入----------
			// -------------------
			if (paramsMap.get(key) == null) {
				paramsMap.put(key, payValue);
			}
		} else if ("text".equals(type)) {
			notyfyData.put(key, payValue);
		} else if ("URLEncoder".equals(type)) {
			notyfyData.put(key, new URLEncoder().encode(paramsMap.get(payValue), Charset.forName("UTF-8")));
		} else if (type.startsWith("random")) {
			String countStr = type.replace("random", "");
			try {
				int count = Integer.valueOf(countStr);
				String randomString = RandomUtil.randomString(count);
				notyfyData.put(key, randomString);
				paramsMap.put(key, randomString);
			} catch (Exception e) {
				notyfyData.put(key, paramsMap.get(payValue));
			}
		} else {
			notyfyData.put(key, paramsMap.get(payValue));
		}
		return encryptStr;
	}

	/**
	 * 提取json变量
	 *
	 * @param channel
	 * @param json
	 * @return
	 */
	public static String getJsonField(PayConfigChannel channel, JSONObject json) {
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
		return redirect;
	}

//	public static void main(String[] args) {
//		String str = "{\"action\":\"success\",\"orderNo\":\"200312220947MK101d1jt\",\"amount\":500.00}";
//		String encode = Base64.encode(str);
//		System.out.println(encode);
//	}

	public static String urlEncodeUTF8(String s) {
		try {
			return java.net.URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	public static String urlEncodeUTF8(Map<?, ?> map) {
		StringBuilder sb = new StringBuilder();
		for (Entry<?, ?> entry : map.entrySet()) {
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(String.format("%s=%s", urlEncodeUTF8(entry.getKey().toString()),
					urlEncodeUTF8(entry.getValue().toString())));
		}
		return sb.toString();
	}

	public static PrivateKey getPrivateKey(String base64PrivateKey) throws Exception {

		byte[] keyBytes = java.util.Base64.getDecoder().decode(base64PrivateKey);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(keySpec);
	}

	// 执行SHA256withRSA签名
	public static String sign(String content, PrivateKey privateKey) throws Exception {
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(privateKey);
		signature.update(content.getBytes(StandardCharsets.UTF_8));
		byte[] signed = signature.sign();
		return java.util.Base64.getEncoder().encodeToString(signed);
	}

	/**
	 * 印度电话号码更换规则:第一位不变，2位和3位互换，4和5位互换，以此类推，最后一位 + 3 , 9+3 = 2 , 1+3 = 4
	 *
	 * @param phoneNum
	 * @return
	 */
	public static String changeNewPhoneNumber(String phoneNum) {
		if (StrUtil.isNotEmpty(phoneNum) && phoneNum.length() == 10) {
			String newPhoneNum = "";
			String firstNumStr = phoneNum.substring(0, 1);// 电话号码首位
			String lastNumStr = phoneNum.substring(phoneNum.length() - 1);// 电话号位最后一位
			Integer lastNum = (new BigDecimal(lastNumStr).add(new BigDecimal(3))).intValue();// 最后一位+3
			String newLastStr = lastNum >= 10 ? lastNum.toString().substring(lastNum.toString().length() - 1)
					: lastNum.toString();// 最后一位+3,比如:9+3 = 2 1+3 = 4

			String newMidStr = "";
			String firstMidStr = "";
			String secondMidStr = "";
			for (int i = 0; i < phoneNum.length() / 2 - 1; i++) {
				firstMidStr = phoneNum.substring((i * 2) + 1, (i * 2) + 2);
				secondMidStr = phoneNum.substring((i * 2) + 2, (i * 2) + 3);

				newMidStr += secondMidStr + firstMidStr;
			}
			newPhoneNum = firstNumStr + newMidStr + newLastStr;

			return newPhoneNum;
		}

		return phoneNum;
	}

	/**
	 * 获取印度时区时间 格式yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String getIndiaTime(Date now){
		SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
		TimeZone timeZoneSH = TimeZone.getTimeZone("Asia/Kolkata");
		outputFormat.setTimeZone(timeZoneSH);
		return  outputFormat.format(now);
	}

	/**
	 * 填充各类金额参数到参数库param备用
	 * @param amount
	 * @param orderId
	 * @param param
	 */
	public static void fillAmountToParam(String amount, String orderId, Map<String, String> param) {
		param.put("orderId", orderId); // 订单号
		param.put("amount", amount); // 充值元
		param.put("amount1", new BigDecimal(amount).setScale(1, BigDecimal.ROUND_DOWN).toPlainString()); // 充值元 +.0
		param.put("amount2", new BigDecimal(amount).setScale(2, BigDecimal.ROUND_DOWN).toPlainString()); // 充值元 +.00
		param.put("amount4", new BigDecimal(amount).setScale(4, BigDecimal.ROUND_DOWN).toPlainString()); // 充值元 +.00
		param.put("amountFen", new BigDecimal(amount).multiply(BigDecimal.valueOf(100)).toString()); // 分
		param.put("amountLi", new BigDecimal(amount).multiply(BigDecimal.valueOf(1000)).toString()); // 厘

	}

	/**
	 * 填充各类回调参数到参数库param备用
	 * @param info
	 * @param payConfigInfo
	 * @param param
	 */
	public static void fillNotifyToParam(PayPlatformInfo info, PayConfigInfo payConfigInfo, Map<String, String> param) {
		Integer platformId = info.getPlatformId();
		Integer payConfigId = payConfigInfo.getId();

		String host = payConfigInfo.getWeUrl();

		if (StrUtil.isBlank(host)) {
			host = info.getWeUrl();
		}

		// 注意回调地址不能 增加了回调的
		String callbackUrl = host + "/api/pay/callback/" + platformId + "/" + payConfigId;
		String successCallbackUrl = host + "/api/pay/successCallback/" + platformId + "/" + payConfigId;
		String cancelCallbackUrl = host + "/api/pay/cancelCallback/" + platformId + "/" + payConfigId;
		String formbackUrl = host + "/api/pay/formback/" + platformId + "/" + payConfigId;
		String notifyUrl = host + "/api/pay/notify/" + platformId + "/" + payConfigId;
		String notifyUrl2 = host + "/api/pay/notify2/" + platformId + "/" + payConfigId;
		String notifyUrl3Headers = host + "/api/pay/notify3Headers/" + platformId + "/" + payConfigId;
		String notifyUrl3 = host + "/api/pay/notify3/" + platformId + "/" + payConfigId;
		String notifyUrl4 = host + "/api/pay/notify4/" + platformId + "/" + payConfigId;
		String notifyJson = host + "/api/pay/notifyJson/" + platformId + "/" + payConfigId;
		String notifyFormJson = host + "/api/pay/notifyFormJson/" + platformId + "/" + payConfigId;
		String notify3UrlDecode = host + "/api/pay/notify3UrlDecode/" + platformId + "/" + payConfigId;
		String notifyUrl3Node = host + "/api/pay/notify3Node/" + platformId + "/" + payConfigId;

		param.put("callbackUrl", callbackUrl);// 请求地址
		param.put("successCallbackUrl", successCallbackUrl);// 成功同步回调地址
		param.put("cancelCallbackUrl", cancelCallbackUrl);// 交易取消同步回调地址
		param.put("formbackUrl", formbackUrl);// 印度支付form表单自定义返回
		param.put("notifyUrl", notifyUrl); // 回调地址 get/post form
		param.put("notifyUrl2", notifyUrl2); // 回调地址2 json对象
		param.put("notifyUrl3Headers", notifyUrl3Headers); // 回调地址3 json/textplain(带请求头的)
		param.put("notifyUrl3", notifyUrl3); // 回调地址3 json/textplain
		param.put("notifyUrl4", notifyUrl4); // 回调地址4 xml对象
		param.put("notifyJson", notifyJson); // 回调地址
		param.put("notifyFormJson", notifyFormJson); // 回调地址 get/post form多级json对象
		param.put("notify3UrlDecode", notify3UrlDecode); // 回调地址3 json/textplain UrlDecode



		URLEncoder urle = new URLEncoder();
		param.put("callbackUrlE", urle.encode(callbackUrl, Charset.forName("UTF-8")));// 请求地址
		param.put("notifyUrlE", urle.encode(notifyUrl, Charset.forName("UTF-8"))); // 回调地址
		param.put("notifyUrl2E", urle.encode(notifyUrl2, Charset.forName("UTF-8"))); // 回调地址2
		param.put("notifyUrlElower", urle.encode(notifyUrl, Charset.forName("UTF-8")).toLowerCase()); // 回调地址 //
		// urlencode////
		// 转小写
		param.put("notifyUrl2Elower", urle.encode(notifyUrl2, Charset.forName("UTF-8")).toLowerCase()); // 回调地址2////
		// urlencode 转小写
		param.put("notifyUrlEupper", urle.encode(notifyUrl, Charset.forName("UTF-8")).toUpperCase());// 回调地址//
		// urlencode//转大写
		param.put("notify3Node", notifyUrl3Node);// json text/plain 格式回调 notify3Node 多层级遍历

		// ［:］替换成%3A [/]替换成%2F
		callbackUrl = callbackUrl.replaceAll(":", "%3A");
		callbackUrl = callbackUrl.replaceAll("/", "%2F");
		param.put("callbackUrlS", callbackUrl); // 回调地址3
		notifyUrl = notifyUrl.replaceAll(":", "%3A");
		notifyUrl = notifyUrl.replaceAll("/", "%2F");
		param.put("notifyUrlS", notifyUrl); // 回调地址3
		param.put("notifyUrlSLower", notifyUrl.toLowerCase()); // 回调地址3
		notifyUrl2 = notifyUrl2.replaceAll(":", "%3A");
		notifyUrl2 = notifyUrl2.replaceAll("/", "%2F");
		notifyUrl3 = notifyUrl3.replaceAll(":", "%3A");
		notifyUrl3 = notifyUrl3.replaceAll("/", "%2F");
		notifyFormJson = notifyFormJson.replaceAll(":", "%3A");
		notifyFormJson = notifyFormJson.replaceAll("/", "%2F");
		param.put("notifyFormJsonS", notifyFormJson); // 回调地址3
		param.put("notifyUrl3S", notifyUrl3); // 回调地址3
		param.put("notifyUrl2S", notifyUrl2); // 回调地址3
		param.put("notifyUrl2SLower", notifyUrl2.toLowerCase()); // 回调地址3

		notifyUrl = notifyUrl.replaceAll(".", "%2E");
		notifyUrl2 = notifyUrl2.replaceAll(".", "%2E");
		param.put("notifyUrlSlower", notifyUrl.toLowerCase()); // 回调地址 urlencode 转小写
		param.put("notifyUrl2Slower", notifyUrl2.toLowerCase()); // 回调地址2 urlencode 转小写

	}

	/**
	 * 填充各类日期参数到参数库param备用
	 * @param now
	 * @param param
	 */
	public static void fillTimeToParam(Date now, Map<String, String> param) {
		param.put("timestamp", now.getTime() + "");
		param.put("timestamp2", (now.getTime() / 1000) + ""); // 时间 到秒
		// 时间戳加八小时秒 2243id 9G支付
		long eight = 8 * 60 * 60;
		param.put("timestamp3", (now.getTime() / 1000) + eight + "");
		// 时间格式 2019-10-25 16:21:44
		param.put("createTime", DateUtil.formatDateTime(now));
		// 时间格式 20191025162144123 yyyyMMddHHmmssSSS
		param.put("createTime2", DateUtil.format(now, DatePattern.PURE_DATETIME_MS_PATTERN));
		// 时间格式 20191025162144 yyyyMMddHHmmss
		param.put("createTime3", DateUtil.format(now, DatePattern.PURE_DATETIME_PATTERN));
		// 时间格式 20191025 yyyyMMdd
		param.put("createTime4", DateUtil.format(now, DatePattern.PURE_DATE_PATTERN));
		// 时间格式 2019-10-25 16:21
		param.put("createTime5", DateUtil.format(now, DatePattern.NORM_DATETIME_MINUTE_PATTERN));
		// 时间格式 162103 HHmmss
		param.put("createTime6", DateUtil.format(now, DatePattern.PURE_TIME_PATTERN));
		// 时间格式 yyyy-MM-dd
		param.put("createTime7", DateUtil.format(now, DatePattern.NORM_DATE_PATTERN));
		// 到期时间格式 yyyy-MM-dd HH:mm:ss
		param.put("orderExpire", DateUtil.format(DateUtil.offsetMinute(now, 10), DatePattern.NORM_DATETIME_PATTERN));

		// 到期时间格式 20191025162144123 yyyyMMddHHmmssSSS
		param.put("orderExpire2",
				DateUtil.format(DateUtil.offsetMinute(now, 10), DatePattern.PURE_DATETIME_MS_PATTERN));

		// 到期时间格式 20191025162144 yyyyMMddHHmmss
		param.put("orderExpire3", DateUtil.format(DateUtil.offsetMinute(now, 10), DatePattern.PURE_DATETIME_PATTERN));

		// 带有上午下午标识的时间格式 AM PM
		param.put("createTime7", new SimpleDateFormat("yyyy-MM-dd hh:mm:ssa").format(new Date()).replace("上午","AM").replace("下午", "PM"));


	}

	/**
	 * 填充各类密钥参数到参数库param备用
	 * @param merchantInfo
	 * @param param
	 */
	public static void fillPrivateKeyToParam(MerchantInfo merchantInfo, Map<String, String> param) {
		param.put("appId", merchantInfo.getAppId()); // 商户id
		param.put("privateKey", merchantInfo.getPrivateKey());// 秘钥
		param.put("privateKey1", merchantInfo.getPrivateKey1());// 秘钥
		param.put("privateKey2", merchantInfo.getPrivateKey2());// 秘钥
		param.put("privateKey3", merchantInfo.getPrivateKey3());// 秘钥
		param.put("privateKey4", merchantInfo.getPrivateKey4());// 秘钥
	}

//	public static void main(String[] args) {
//		 log.info("{}---------{}","8780217497", changeNewPhoneNumber("8780217497"));
//		 //8872071940
//		log.info("{}---------{}","9780708450", changeNewPhoneNumber("9780708450"));
//		//9877080543
//		log.info("{}---------{}","9348976001", changeNewPhoneNumber("9348976001"));
//		//9439867004
//
//	}
}
