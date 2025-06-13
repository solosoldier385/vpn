package com.letsvpn.pay.util;

public class RedisConstant {

	public static final String redis_root = "payproject";
	public static final String redis_root_telegram = redis_root + ":telegram";
	public static final String redis_root_telegram_error = redis_root_telegram + ":error";
	public static final String redis_root_telegram_test = redis_root_telegram + ":test";

	public static final String SYSTEM_NAME = "platform_system:";

	/**登录人信息key前缀*/
	public static final String LOGIN_KEY = RedisConstant.SYSTEM_NAME + "LOGIN:";

	/**pbank补单锁消息*/
	public final static String PUSH_LOCK_MESSAGE = "pbankUtrPush in progress!";

	public static String orderlock(String orderId) {
		return redis_root + ":pay_notify:" + orderId;
	}

	public static String utrlock(String utr) {
		return redis_root + ":pay_query:" + utr;
	}

	public static String getKey(String key) {
		return RedisConstant.SYSTEM_NAME + key;
	}

	public static String getLoginKey(String userAccount) {
		return RedisConstant.LOGIN_KEY + userAccount;
	}

//	public static final String simpaisa_userkey_15min = "simpaisa_userkey_15min";

//	public static final String simpaisa_userkey_otp_15min = "simpaisa_userkey_otp_15min";
}
