package com.letsvpn.pay.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.letsvpn.pay.entity.PayConfigInfo;
import com.letsvpn.pay.entity.PayPlatformInfo;
import com.letsvpn.pay.service.core.PayReqCreateService;
import com.letsvpn.pay.util.KeyValue;
import com.letsvpn.pay.util.PayCallMethod;
import com.letsvpn.pay.util.PayConstant;
import com.letsvpn.pay.util.PayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Aiden
 */
@RestController
@RequestMapping(PayConstant.root_pay_project)
public class PayReqCreateController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(PayReqCreateController.class);

	@Autowired
	PayReqCreateService payReqCreateService;

	@Value("${wanli.sign.check:1}")
	int check;

	@RequestMapping("/create/{platformNo}/{payConfigId}")
	Object createOrder(@PathVariable int platformNo, @PathVariable int payConfigId,
			@RequestParam Map<String, Object> paramsMap, HttpServletRequest request, HttpServletResponse response) {

		PayPlatformInfo platformInfo = getPayPlatformInfoId(platformNo);
		PayConfigInfo payConfigInfo = getPayConfigInfo(payConfigId);
		String ip = getIp(request);
		KeyValue<PayCallMethod, String> result = payReqCreateService.create(platformInfo, payConfigInfo, paramsMap, ip);

		// {"amount":["50.00"],"exchange_rate":["1.00"],"sign":["db3e6c8c57e719e3e84d59d379aa1d55"],"ingot_number":["50"],"platform_product_id":["1"],"number":["50.00"],"rndkey":["Ao7pg94UCl7eemCpNj7Y"],"platform_order_id":["3040870118206230528"],"user_id":["606541"],"clientip":["87.200.59.170"],"pay_type":["1"],"app_id":["o9JxJU3ekvOvtUsdvFqIjqKJ"],"cny_rate":["1.00"],"timestamp":["1599570335"]}

		if (result.getKey().compareTo(PayCallMethod.form) == 0) {
			return result.getValue();
		} else if (result.getKey().compareTo(PayCallMethod.redirect) == 0) {
			String url = result.getValue();
			if (StrUtil.isEmpty(url)) {
//				return "获得跳转地址,请更换其他支付方式";
				return PayUtil.extLocale(request.getLocale(), 1017);
			} else {
				response.setStatus(301);
				// hsResponse.setHeader("Location","/blog/");
				response.setHeader("Location", url);

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
}
