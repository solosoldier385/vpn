package com.letsvpn.pay.interceptor;

import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.json.JSONUtil;

import com.letsvpn.pay.util.PayConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.UUID;

/**
 * @author Aiden 2019-06-16 20:25:51
 */
public class LogInterceptor implements HandlerInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(LogInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		MDC.put(PayConstant.MDC_KEY_REQ_ID, UUID.randomUUID().toString().replaceAll("-", ""));
		MDC.put(PayConstant.MDC_KEY_REQ_TIME, System.currentTimeMillis() + "");
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		Enumeration<String> hs = request.getHeaderNames();
		StringBuffer sb = new StringBuffer();
		if (hs != null) {
			while (hs.hasMoreElements()) {
				String name = hs.nextElement();
				sb.append(name).append(" -> ").append(request.getHeader(name)).append(" ; ");
			}
		}
		logger.info("time: {} {} {} qs:{} pm:{},hader:{}",
				(System.currentTimeMillis() - Long.valueOf(MDC.get(PayConstant.MDC_KEY_REQ_TIME))),
				ServletUtil.getClientIP(request), request.getRequestURL(), request.getQueryString(),
				JSONUtil.toJsonStr(request.getParameterMap()), sb);
	}

}
