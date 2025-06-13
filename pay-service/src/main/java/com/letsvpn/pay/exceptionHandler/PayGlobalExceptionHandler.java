package com.letsvpn.pay.exceptionHandler;



import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.json.JSONObject;
import com.letsvpn.pay.exception.WanliException;
import com.letsvpn.pay.util.PayConstant;
import com.letsvpn.pay.util.PayUtil;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Aiden 2019-06-16 20:25:51 全局异常处理类
 */
@ControllerAdvice
class PayGlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(PayGlobalExceptionHandler.class);

	@ExceptionHandler(value = Exception.class)
	@ResponseBody
	public JSONObject defaultErrorHandler(HttpServletRequest request, Exception e) throws Exception {

		JSONObject json = new JSONObject();
		json.put("code", -1);
		// json.put("e", e);
//		json.put("url", request.getRequestURL());
		json.put("id", MDC.get(PayConstant.MDC_KEY_REQ_ID));
		json.put("msg", "fail");
		logger.error("ip:{},url{},map:{},msg:{}", ServletUtil.getClientIP(request), request.getRequestURL(),
				ServletUtil.getParamMap(request), e.getMessage(), e);
		return json;
	}

	@ExceptionHandler(value = WanliException.class)
	@ResponseBody
	public JSONObject wanliErrorHandler(HttpServletRequest request, WanliException e) throws Exception {
		JSONObject json = new JSONObject();
		json.put("code", e.getCode());
		json.put("id", MDC.get(PayConstant.MDC_KEY_REQ_ID));
		if (e.getCode() == 404) {
			json.put("msg", PayUtil.extLocale(request.getLocale(), e.getCode(), e.getMessage()));
		} else {
			json.put("msg", PayUtil.extLocale(request.getLocale(), e.getCode(), e.getParam()));

		}
		logger.info("wanli, {} ,{},{},{}", ServletUtil.getClientIP(request), request.getRequestURL(),
				ServletUtil.getParamMap(request), e.getMessage());
		return json;
	}

}