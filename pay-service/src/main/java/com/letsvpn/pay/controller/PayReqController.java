package com.letsvpn.pay.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.common.core.util.AuthContextHolder;
import com.letsvpn.pay.service.core.PayReqService;
import com.letsvpn.pay.util.KeyValue;
import com.letsvpn.pay.util.PayCallMethod;
import com.letsvpn.pay.util.PayConstant;
import com.letsvpn.pay.util.PayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.letsvpn.pay.entity.PayConfigChannel;
import com.letsvpn.pay.service.base.IPayConfigChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import com.letsvpn.common.core.response.R;

/**
 * @author Aiden
 */
@Tag(name = "支付渠道", description = "支付渠道相关接口")
@RestController
@RequestMapping("/pay")
public class PayReqController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(PayReqController.class);
	@Autowired
	PayReqService payReqService;

	@Value("${wanli.sign.check:1}")
	int check;

	@Autowired
	private IPayConfigChannelService payConfigChannelService;

	@GetMapping("/req")
	Object req(@RequestParam Map<String, String> paramsMap, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		Long userId = AuthContextHolder.getRequiredUserId();
		long time = MapUtil.getLong(paramsMap, "time", 0L);
		if (check == 1) {

			// 过去的时间
			// 错误代码010-请求已过期
			validate(time < System.currentTimeMillis() - 1000 * 60 * 20, "请求已过期", 1013);

			validateParam(time > System.currentTimeMillis() + 1000 * 60 * 20, 1002, "time");

		}
		String ip = getIp(request);
		logger.info("IP:{},REQ:{},URL:{},uri:{},qs:{}", ip, paramsMap, request.getRequestURL(), request.getRequestURI(),
				request.getQueryString());

		KeyValue<PayCallMethod, String> result = payReqService.req(paramsMap, validateSign(paramsMap), ip,
				request.getRequestURL(), request.getRequestURI(), request.getQueryString(), response,userId);

		if (result.getKey().compareTo(PayCallMethod.form) == 0) {
			return result.getValue();
		} else if (result.getKey().compareTo(PayCallMethod.redirect) == 0) {
			String url = result.getValue();
			if (StrUtil.isEmpty(url)) {
//				return "获得跳转地址,请更换其他支付方式";
				return PayUtil.extLocale(request.getLocale(), 1017);
			} else {
//				response.setStatus(301);
//				response.setHeader("Location", url);
				response.sendRedirect(url);
				return null;
			}
		} else if (result.getKey().compareTo(PayCallMethod.sdk) == 0) {
			String url = result.getValue();
			JSONObject jsonObject = new JSONObject(true);
			if (StrUtil.isEmpty(url)) {
				jsonObject.put("code", "-1");
//				jsonObject.put("data", "未获得数据,请更换其他支付方式");
				jsonObject.put("data", PayUtil.extLocale(request.getLocale(), 1018));
			} else {
				jsonObject.put("code", "0");
				jsonObject.put("data", url);
			}
			return jsonObject;
		} else {
			return PayUtil.extLocale(request.getLocale(), 1016);
		}
	}

	/**
	 * 获取所有支付渠道信息
	 * @return 所有 pay_config_channel 列表
	 */
	@Operation(summary = "获取所有支付渠道", description = "获取所有可用的支付渠道信息。无需认证，客户端可直接调用。")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "获取成功，返回渠道列表"),
			@ApiResponse(responseCode = "500", description = "服务器内部错误")
	})
	@GetMapping("/channel/list")
	public R<List<PayConfigChannel>> listPayChannels() {
		try {
			QueryWrapper<PayConfigChannel> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("status",1);
			List<PayConfigChannel> list = payConfigChannelService.list(queryWrapper);
			return R.success(list);
		} catch (Exception e) {
			logger.error("获取支付渠道列表失败", e);
			return R.fail("获取支付渠道列表失败: " + e.getMessage());
		}
	}
}
