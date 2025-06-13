package com.letsvpn.pay.util;

public class PayConstant {

	public static final String root_pay_project = "/pay";
	public static final String root_admin = "/api/payadmin";
	/**拦截器例外地址*/
	public static final String URL_EXCLUDE = root_admin + "/exclude";
	public static final String root_paynotice = "/api/paynotice";
	public static final String lock_value = "pay_project_base";
	public static final String pay_success_list = "pay_success_list";
	public static final String pay_success_list_bak = "pay_success_list_bak";
	public static final String pay_query_list = "pay_query_list";
	public static final String MDC_KEY_REQ_ID = "reqId";
	public static final String api_EXCLUDE = "/api/exclude";
	

	public static final String MDC_KEY_REQ_TIME = "reqTime";
	/**cookie-token名称*/
	public static final String cookie_admin_token = "payadmin";

	/**user-token-失效时间：秒*/
	public static final long USER_TOKEN_OUT_TIME = 60*60*60;

	/**login-user名称*/
	public static final String LOGIN_USER_DATA = "login_user_data";

	/**cookie-token名称*/
	public static final String COOKIE_PLATFORM_TOKEN = "pay_token";
	
	

	public static final String qrcode_key = "xh2ms5oojik8t860i7wsv5w8fon45kk0";

	public static final String cashier_key = "TTFyZDTFD6wJcuwcKz8Qgc+pEZpCkcda";
	
}
