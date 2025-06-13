package com.letsvpn.pay.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.letsvpn.pay.exception.WanliException;
import org.slf4j.MDC;

import java.util.Map;

public class BaseWanli {
	protected JSONObject result() {
		return result(0, "ok", null);
	}

	protected JSONObject resultSuccess(String msg) {
		return result(0, msg, null);
	}

	protected JSONObject resultData(Object data) {
		return resultData(data, "ok");
	}

	protected JSONObject resultData(Object data, String msg) {
		return result(0, msg, data);
	}

	protected JSONObject result(int code, String msg, Object data) {
		JSONObject json = new JSONObject();
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", data);
		json.put("id", MDC.get(PayConstant.MDC_KEY_REQ_ID));
		return json;
	}

	protected JSONObject resultError() {
		return resultError("fail");
	}

	protected JSONObject resultError(String msg) {
		return result(-1, msg, null);
	}

	protected void validateEmpty(Map<String, ?> body, String... keys) {
		for (String key : keys) {
			validateParam(body.get(key) == null || StrUtil.isBlank(body.get(key).toString()), 1001, key);
		}
	}

//	protected void validateNull(Map<String, ?> body, String... keys) {
//		for (String key : keys) {
//			validate(body.get(key) == null, key + "不能为空", 1);
//		}
//	}

//	protected void validate(boolean flag, String msg) {
//		validate(flag, msg, 1);
//	}

	protected void validate(boolean flag, String msg, int code) {
		if (flag) {
			throw new WanliException(code, msg);
		}

	}

	protected void validate(boolean flag, String msg, int code, String... param) {
		if (flag) {
			throw new WanliException(code, msg, param);
		}

	}

	protected void validateParam(boolean flag, int code, String... param) {
		if (flag) {
			throw new WanliException(code, "" + code, param);
		}

	}
}
