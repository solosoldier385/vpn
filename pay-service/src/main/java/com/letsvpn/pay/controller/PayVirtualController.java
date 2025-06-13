package com.letsvpn.pay.controller;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.letsvpn.pay.entity.*;
import com.letsvpn.pay.exception.WanliException;
import com.letsvpn.pay.mapper.*;
import com.letsvpn.pay.service.core.OrderInfoService;
import com.letsvpn.pay.service.core.PayNotifyService;
import com.letsvpn.pay.third.util.ThirdUtil;
import com.letsvpn.pay.util.PayCallMethod;
import com.letsvpn.pay.util.PayConstant;
import com.letsvpn.pay.vo.PayResultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Aiden
 */
@RestController
@RequestMapping(PayConstant.root_pay_project)
public class PayVirtualController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(PayVirtualController.class);


	@Autowired
	PayNotifyService payNotifyService;

	@Autowired
	OrderInfoService orderInfoService;
	@Autowired
	MerchantInfoMapper merchantInfoMapper;
	@Autowired
	OrderReqRecordMapper orderReqRecordMapper;

	@Autowired
	PayConfigInfoMapper payConfigInfoMapper;

	@Autowired
	PayConfigChannelMapper payConfigChannelMapper;

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	private ResourceLoader resourceLoader;







	@RequestMapping("/qrcode")
	void invite(@RequestParam("a") String a, HttpServletResponse response) throws IOException {
		String content = String.valueOf(SecureUtil.aes(PayConstant.qrcode_key.getBytes()).encrypt(a));
		QrCodeUtil.generate(content, 400, 400, ImgUtil.IMAGE_TYPE_PNG, response.getOutputStream());
	}










	/**
	 * 巴基斯坦Simpaisa
	 * @param platformNo
	 * @param payConfigId
	 * @param paramsMap
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/simpaisaPayForm/{platformNo}/{payConfigId}")
	String greSimpaisaPayForm(@PathVariable int platformNo, @PathVariable int payConfigId,
						 @RequestParam Map<String, String> paramsMap) {


		String userKey = paramsMap.get("userKey");
		logger.info("---{}",userKey);
		if (userKey == null) {
			throw new WanliException(5012, "该支付异常2,请换其他支付");
		}

		OrderInfo orderInfo = orderInfoService.getOrderInfo(userKey);
		if (orderInfo == null) {
			logger.info("userKey is null:{}",userKey);
			throw new WanliException(5012, "该支付异常2,请换其他支付");
		}
		if(StrUtil.isNotBlank(orderInfo.getOtherOrderId())){
			logger.info("userKey:{}",userKey);
			throw new WanliException(5012, "该支付异常2,请换其他支付");
		}
		long now = System.currentTimeMillis();
		StringBuffer logText = new StringBuffer();
		String reqLink1 = "";
		String channelId = paramsMap.get("channelId");
		String channelName = paramsMap.get("channelName");
		String payConfigName = paramsMap.get("payConfigName");
		String resultText1 = "";
		Map<String, Object> formData1;

		String transactionId;
		Map<String, Object> paraInit = null;
		String message ;
		try {

			PayCallMethod callmethod = PayCallMethod.valueOf(PayCallMethod.form.name());
			PayResultData result = new PayResultData(callmethod);

			String reqLink = MapUtil.getStr(paramsMap, "reqLink");
			reqLink1 = reqLink;
			paramsMap.remove("reqLink");
			formData1 = new HashMap<String, Object>();
			formData1.putAll(paramsMap);
			formData1.put("webUrl", paramsMap.get("webUrl"));
			formData1.put("verifyUrl", paramsMap.get("verifyUrl"));
			formData1.put("platformId", platformNo);
			formData1.put("payConfigId", payConfigId);
			formData1.put("channelId", channelId);
			//调用simpaisa 发送OTP接口

			HttpRequest hr = HttpRequest.post(reqLink).timeout(ThirdUtil.HTTP_REQUEST_LONG_TIMEOUT);// 超时，毫秒
			paraInit = new HashMap<>();
			paraInit.put("merchantId", paramsMap.get("merchantId"));
			paraInit.put("operatorId", paramsMap.get("operatorId"));
			paraInit.put("amount", paramsMap.get("amount"));
			paraInit.put("userKey", paramsMap.get("userKey"));
			paraInit.put("transactionType", paramsMap.get("transactionType"));
			paraInit.put("msisdn", paramsMap.get("msisdn"));

			hr = hr.body(new JSONObject(paraInit).toString());
			resultText1 = hr.executeAsync().body();
			logger.info("resultText1=" + resultText1);
//			resultText1 = resultText;
			JSONObject json = JSONUtil.parseObj(resultText1);
			message = json.getStr("message");
			transactionId = json.getStr("transactionId");
			formData1.put("transactionId", transactionId);
		} catch (Exception e) {
			long reqTime = System.currentTimeMillis() - now;
			/*extracted(merchantInfo, payConfigInfo, channel, reqLink, formData1, resultText, reqTime, e.getMessage(),
					logText, 1);*/

			OrderReqRecord record = new OrderReqRecord();
			record.setBodyText(paraInit.toString());
			record.setReqLink(reqLink1);
			record.setCreateTime(new Date());
			record.setPayConfigChannelId(Long.valueOf(channelId));
			record.setPayConfigChannelName(channelName);
			record.setPayConfigId(payConfigId);
			record.setPayConfigName(payConfigName);
			record.setPlatformId(platformNo);
			record.setReqTime(reqTime);
			record.setError(1);
			record.setResultText(resultText1);
			record.setErrorText(e.getMessage());
			record.setLogText(logText.toString());
			orderReqRecordMapper.insert(record);

			throw new WanliException(5012, "该支付异常2,请换其他支付");
		}

		long reqTime = System.currentTimeMillis() - now;
//		extracted(merchantInfo, payConfigInfo, channel, reqLink, formData1, resultText, reqTime, "", logText, 0);

		OrderReqRecord record = new OrderReqRecord();
		record.setBodyText(paraInit.toString());
		record.setReqLink(reqLink1);
		record.setCreateTime(new Date());
		record.setPayConfigChannelId(Long.valueOf(channelId));
		record.setPayConfigChannelName(channelName);
		record.setPayConfigId(payConfigId);
		record.setPayConfigName(payConfigName);
		record.setPlatformId(platformNo);
		record.setReqTime(reqTime);
		record.setError(0);
		record.setResultText(resultText1);
		record.setErrorText("");
		record.setLogText(logText.toString());
		orderReqRecordMapper.insert(record);

		if(StrUtil.isNotEmpty(transactionId)){
			OrderInfo updateOrder = new OrderInfo();
			updateOrder.setId(orderInfo.getId());
			updateOrder.setOtherOrderId(transactionId);
			orderInfoService.updateByPrimaryKey(updateOrder);
		}

		if (!"Success".equals(message)) {
			//响应错误页面
			return "\n" +
					"<html lang=\"en\">\n" +
					"\n" +
					"<head>\n" +
					"\t<meta charset=\"UTF-8\">\n" +
					"\t<meta name=\"viewport\"\n" +
					"\t\tcontent=\"width=device-width,initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover\">\n" +
					"\t<title>Information</title>\n" +
					"</head>\n" +
					"<style>\n" +
					"@import url('https://fonts.googleapis.com/css?family=Nunito:400,600,700');\n" +
					"$nunito-font: 'Nunito', sans-serif;\n" +
					"\n" +
					"// mixins\n" +
					"@mixin breakpoint($point) {\n" +
					"    @if $point==mobile {\n" +
					"        @media (max-width: 480px) and (min-width: 320px) {\n" +
					"            @content ;\n" +
					"        }\n" +
					"    }\n" +
					"}\n" +
					"\n" +
					"// keyrames\n" +
					"@keyframes floating {\n" +
					"    from { transform: translateY(0px); }\n" +
					"    65%  { transform: translateY(15px); }\n" +
					"    to   { transform: translateY(-0px); }\n" +
					"}\n" +
					"\n" +
					"html {\n" +
					"  height: 100%;\n" +
					"}\n" +
					"\n" +
					"body{\n" +
					"  background-image: url('https://assets.codepen.io/1538474/star.svg'),linear-gradient(to bottom, #05007A, #4D007D);\n" +
					"  height: 100%;\n" +
					"  margin: 0;\n" +
					"  background-attachment: fixed;\n" +
					"  overflow: hidden;\n" +
					"}\n" +
					"\n" +
					".mars{\n" +
					"  left:0;\n" +
					"  right:0;\n" +
					"  bottom:0;\n" +
					"  position:absolute;\n" +
					"  height: 27vmin;\n" +
					"  background: url('https://assets.codepen.io/1538474/mars.svg') no-repeat bottom center;\n" +
					"  background-size: cover;\n" +
					"}\n" +
					"\n" +
					".logo-404{\n" +
					"  position: absolute;\n" +
					"  margin-left: auto;\n" +
					"  margin-right: auto;\n" +
					"  left: 0;\n" +
					"  right: 0;\n" +
					"  top: 16vmin;\n" +
					"  width: 30vmin;\n" +
					"\n" +
					"  @include breakpoint(mobile){\n" +
					"    top: 45vmin;\n" +
					"  }\n" +
					"}\n" +
					"\n" +
					".meteor{\n" +
					"  position: absolute;\n" +
					"  right: 2vmin;\n" +
					"  top: 16vmin;\n" +
					"}\n" +
					"\n" +
					".title{\n" +
					"  color: white;\n" +
					"  font-family: $nunito-font;\n" +
					"  font-weight: 600;\n" +
					"  text-align: center;\n" +
					"  font-size: 5vmin;\n" +
					"  margin-top: 31vmin;\n" +
					"\n" +
					"  @include breakpoint(mobile){\n" +
					"    margin-top: 65vmin;\n" +
					"  }\n" +
					"}\n" +
					"\n" +
					".subtitle{\n" +
					"  color: white;\n" +
					"  font-family: $nunito-font;\n" +
					"  font-weight: 400;\n" +
					"  text-align: center;\n" +
					"  font-size: 3.5vmin;\n" +
					"  margin-top: -1vmin;\n" +
					"  margin-bottom: 9vmin;\n" +
					"}\n" +
					"\n" +
					".btn-back{\n" +
					"  border: 1px solid white;\n" +
					"  color: white;\n" +
					"  height: 5vmin;\n" +
					"  padding: 12px;\n" +
					"  font-family: $nunito-font;\n" +
					"  text-decoration: none;\n" +
					"\tborder-radius: 5px;\n" +
					"\n" +
					"  &:hover{\n" +
					"    background: white;\n" +
					"    color: #4D007D;\n" +
					"  }\n" +
					"\n" +
					"  @include breakpoint(mobile){\n" +
					"    font-size: 3.5vmin;\n" +
					"  }\n" +
					"}\n" +
					"\n" +
					".astronaut{\n" +
					"  position: absolute;\n" +
					"  top: 18vmin;\n" +
					"  left: 10vmin;\n" +
					"  height: 30vmin;\n" +
					"\tanimation: floating 3s infinite ease-in-out;\n" +
					"\n" +
					"  @include breakpoint(mobile){\n" +
					"    top: 2vmin;\n" +
					"  }\n" +
					"}\n" +
					"\n" +
					".spaceship{\n" +
					"  position: absolute;\n" +
					"  bottom: 15vmin;\n" +
					"  right: 24vmin;\n" +
					"\n" +
					"  @include breakpoint(mobile){\n" +
					"    width: 45vmin;\n" +
					"    bottom: 18vmin;\n" +
					"  }\n" +
					"}\n" +
					"</style>\n" +
					"<body>\n" +
					"\t<div class=\"mars\"></div>\n" +
					"\t<img src=\"https://assets.codepen.io/1538474/404.svg\" class=\"logo-404\" />\n" +
					"\t<img src=\"https://assets.codepen.io/1538474/meteor.svg\" class=\"meteor\" />\n" +
					"\t<p class=\"title\">Oh no!!</p>\n" +
					"\t<p class=\"subtitle\">\n" +
					"\t\t" + message + ".\n" +
					"\t</p>\n" +
					"\t<div align=\"center\">\n" +
					"\t\t<a class=\"btn-back\" href=\"#\">Back to previous page</a>\n" +
					"\t</div>\n" +
					"\t<img src=\"https://assets.codepen.io/1538474/astronaut.svg\" class=\"astronaut\" />\n" +
					"\t<img src=\"https://assets.codepen.io/1538474/spaceship.svg\" class=\"spaceship\" />\n" +
					"</body>\n" +
					"\n" +
					"</html>\n";
		}else{
//			return simpaisaPayService.verifyHTML(formData1);
			return null;
		}

	}














	protected void extracted(MerchantInfo merchantInfo, PayConfigInfo payConfigInfo, PayConfigChannel channel,
							 String reqLink, Map<String, Object> formData1, String resultText, long reqTime, String errorText,
							 StringBuffer logText, Integer error) {
		OrderReqRecord record = new OrderReqRecord();
		record.setBodyText(formData1.toString());
		record.setReqLink(reqLink);
		record.setCreateTime(new Date());
		record.setPayConfigChannelId(channel.getId());
		record.setPayConfigChannelName(channel.getTitle());
		record.setPayConfigId(payConfigInfo.getId());
		record.setPayConfigName(payConfigInfo.getTitle());
		record.setPlatformId(merchantInfo.getPlatformId());
		record.setReqTime(reqTime);
		record.setError(error);
		record.setResultText(resultText);
		record.setErrorText(errorText);
		record.setLogText(logText.toString());
		orderReqRecordMapper.insert(record);
	}







	@Autowired
	OrderInfoMapper orderInfoMapper;













	/**
	 * 订单号查询
	 *
	 * @param paramsMap
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/queryOrderByOrderId")
	String queryOrderByOrderId(@RequestParam Map<String, String> paramsMap, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		validate(paramsMap.isEmpty(), "参数不存在!", 1015);

		String orderId = paramsMap.get("orderId");
		OrderInfo orderInfo = orderInfoService.getOrderInfo(orderId);

		if(orderInfo!=null){
			PayPlatformInfo platformInfo = extPayPlatformInfo.getPayPlatformInfoId(orderInfo.getPlatformId());
			String pf = platformInfo.getPlatformNo();
			Integer userId = orderInfo.getUserId();
			Integer gameId = orderInfo.getGameId();
			return new JSONObject().set("code", "0").set("pf", pf).set("userId", userId)
					.set("gameId", gameId).toString();
		}

		return new JSONObject().set("code", "-1").set("msg", "no order data!").toString();
	}






}
