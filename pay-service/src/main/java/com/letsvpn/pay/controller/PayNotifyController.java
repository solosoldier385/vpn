package com.letsvpn.pay.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.letsvpn.pay.entity.OrderInfo;
import com.letsvpn.pay.mapper.MerchantInfoMapper;
import com.letsvpn.pay.mapper.OrderInfoMapper;
import com.letsvpn.pay.service.core.OrderInfoService;
import com.letsvpn.pay.service.core.PayNotifyService;
import com.letsvpn.pay.third.util.ThirdUtil;
import com.letsvpn.pay.util.PayConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author Aiden
 */
@RestController
@RequestMapping(PayConstant.root_pay_project)
public class PayNotifyController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(PayNotifyController.class);

	@Autowired
	PayNotifyService payNotifyService;


	@RequestMapping("/callback/{platformNo}/{payConfigId}")
	String callback(@PathVariable int platformNo, @PathVariable int payConfigId,
			@RequestParam Map<String, String> paramsMap, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		validate(paramsMap.isEmpty(), "参数不存在!", 1015);
		payNotifyService.create(ip, JSONUtil.toJsonStr(paramsMap), reqUrl, "callback", platformNo, payConfigId);
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return "OK";
	}


	@RequestMapping("/successCallback/{platformNo}/{payConfigId}")
	String successCallback(@PathVariable int platformNo, @PathVariable int payConfigId,
					@RequestParam Map<String, String> paramsMap, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		//validate(paramsMap.isEmpty(), "参数不存在!", 1015);
		payNotifyService.create(ip, JSONUtil.toJsonStr(paramsMap), reqUrl, "successCallback", platformNo, payConfigId);
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return "<div style=\"text-align:center;font-size:4rem;margin:9%;\">Success</div>";
	}


	@RequestMapping("/sdkCallback/{platformNo}/{payConfigId}")
	String sdkCallback(@PathVariable int platformNo, @PathVariable int payConfigId,
						   @RequestParam Map<String, String> paramsMap, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		//validate(paramsMap.isEmpty(), "参数不存在!", 1015);
		payNotifyService.create(ip, JSONUtil.toJsonStr(paramsMap), reqUrl, "successCallback", platformNo, payConfigId);
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return "<html>\n" +
				"<head>\n" +
				"\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
				"\t<meta charset=\"utf-8\">\n" +
				"</head>\n" +
				"\n" +
				"<body>\n" +
//				"<video width=\"100%\" controls=\"\" autoplay=\"\" name=\"media\"><source src=\"https://www.youtube.com/watch?v=r3So-oOY5zM\" type=\"video/mp4\"></video>\n" +
//				"<video width=\"100%\" controls=\"\" autoplay=\"\" name=\"media\"><source src=\"https://xjp14oss.dferiuku78.com/solo/video.mp4\" type=\"video/mp4\"></video>\n" +

				"<video width=\"100%\" controls=\"\" autoplay=\"\" name=\"media\"><source src=\"https://xjp14oss.dferiuku78.com/solo/video_2023-08-13_13-32-37.mp4\" type=\"video/mp4\"></video>\n" +


				"<div style=\"\">\n" +
				"<img width=\"100%\" src=\"https://xjp14oss.dferiuku78.com/solo/payhelp1.jpg\">\n" +
				"<img width=\"100%\" src=\"https://xjp14oss.dferiuku78.com/solo/payhelp2.jpg\">\n" +
				"\n" +
				"    বিকাশ দ্বারা সফলভাবে রিচার্জ করা অর্ডার নম্বরটি অনুগ্রহ করে অনুলিপি করুন এবং তারপর গেমটিতে জমা দেওয়া অর্ডার নম্বরের অবস্থানে প্রবেশ করুন।\n" +
				"</div>\n" +
				"</body>\n" +
				"</html>";
	}


	@RequestMapping("/trueCallback/{platformNo}/{payConfigId}")
	String trueCallback(@PathVariable int platformNo, @PathVariable int payConfigId,
						   @RequestParam Map<String, String> paramsMap, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		//validate(paramsMap.isEmpty(), "参数不存在!", 1015);
		payNotifyService.create(ip, JSONUtil.toJsonStr(paramsMap), reqUrl, "successCallback", platformNo, payConfigId);
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return "True";
	}


	@RequestMapping("/cancelCallback/{platformNo}/{payConfigId}")
	String cancelCallback(@PathVariable int platformNo, @PathVariable int payConfigId,
						   @RequestParam Map<String, String> paramsMap, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		//validate(paramsMap.isEmpty(), "参数不存在!", 1015);
		payNotifyService.create(ip, JSONUtil.toJsonStr(paramsMap), reqUrl, "cancelCallback", platformNo, payConfigId);
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return "<div style=\"text-align:center;font-size:4rem;margin:9%;\">Cancelled</div>";
	}

	/**
	 * 印度支付form表单自定义返回
	 * @param payConfigId
	 * @param paramsMap
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/formback/{platformNo}/{payConfigId}")
	String formback(@PathVariable int platformNo, @PathVariable int payConfigId,
					@RequestParam Map<String, String> paramsMap, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		validate(paramsMap.isEmpty(), "参数不存在!", 1015);
		payNotifyService.create(ip, JSONUtil.toJsonStr(paramsMap), reqUrl, "formback", platformNo, payConfigId);
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
//		if(payConfigId == 2578) {//bankopen 特殊处理
//			if (StrUtil.isEmpty(paramsMap.get("layer_pay_token_id"))) {
//				return "<div style=\"text-align:center;font-size:4rem;margin:9%;\">Fail</div>";
//			}
//		}
//		if(payConfigId == 2380 || payConfigId == 2583 || payConfigId == 2712) {//easebuzz 特殊处理
//			if (!StrUtil.equals(paramsMap.get("status"), "success")) {
//				return "<div style=\"text-align:center;font-size:4rem;margin:9%;\">Fail</div>";
//			}
//		}
//		if(payConfigId == 2278) {//Razorpay 特殊处理
//			if (StrUtil.isEmpty(paramsMap.get("razorpay_payment_id"))) {
//				return "<div style=\"text-align:center;font-size:4rem;margin:9%;\">Fail</div>";
//			}
//		}
		if(payConfigId == 3501) {//easebuzz 特殊处理
			if (!StrUtil.equals(paramsMap.get("status"), "success")) {
				return "<div style=\"text-align:center;font-size:4rem;margin:9%;\">Fail</div>";
			}
		}
		if(payConfigId == 2945){//msspay表单请求特殊处理
	    	if(StrUtil.isEmpty(paramsMap.get("upiId"))) {
				return "<div style=\"text-align:center;font-size:4rem;margin:9%;\">upiId must be enter</div>";
			}

	    	String paramStr = SecureUtil.aes(PayConstant.qrcode_key.getBytes()).decryptStr(paramsMap.get("a"));
	    	JSONObject paramObj = null;
	    	if(StrUtil.isNotEmpty(paramStr)){
					paramObj = JSONUtil.parseObj(paramStr);
			}

			long currentTime = Long.valueOf(paramObj.getStr("currentTime"));
			// 只能处理最近10分钟的订单
			if((System.currentTimeMillis() - 1000L * 60 * 10) > currentTime){
				return "<div style=\"text-align:center;font-size:4rem;margin:9%;\">Only the last 10 minutes of orders can be processed</div>";
			}

	    	String apiToken = paramObj.getStr("apiToken");
	    	String reqLink = paramObj.getStr("reqLink");
	    	Map<String ,Object> reqForm = new HashMap<>();
			reqForm.put("mid", paramObj.getStr("mid"));
			reqForm.put("mTxnId", paramObj.getStr("mTxnId"));
			reqForm.put("amount", paramObj.getStr("amount"));
			reqForm.put("merchantVpa", paramsMap.get("upiId"));
			String resultText = HttpRequest.post(reqLink).body(new JSONObject(reqForm).toString()).header("authToken", apiToken).timeout(ThirdUtil.HTTP_REQUEST_TIMEOUT).executeAsync().body();
			logger.info("MssPay-resultText:{}", resultText);

			if(StrUtil.isNotEmpty(resultText)){
				JSONObject jsonObject = JSONUtil.parseObj(resultText);
				if(!StrUtil.equals(jsonObject.getStr("status"), "Success") || !StrUtil.equals(jsonObject.getStr("code"), "S00")){
					return "<div style=\"text-align:center;font-size:4rem;margin:9%;\">Fail</div>";
				}
			}else{
				return "<div style=\"text-align:center;font-size:4rem;margin:9%;\">Fail</div>";
			}

		}



		if(payConfigId == 3221) {//paytm 客户端返回特殊处理
			if(!StrUtil.equals(paramsMap.get("STATUS"), "TXN_SUCCESS") || !StrUtil.equals(paramsMap.get("RESPCODE"), "01")){
				return "<div style=\"text-align:center;font-size:4rem;margin:9%;\">Fail</div>";
			}
		}

		if(payConfigId == 3355 || payConfigId == 3588) {//Payflash,onvo 客户端返回特殊处理
			if(!StrUtil.equals(paramsMap.get("response_code"), "0")){
				return "<div style=\"text-align:center;font-size:4rem;margin:9%;\">Fail</div>";
			}
		}

		if(payConfigId == 3400) {//PhiPay 客户端返回特殊处理
			if(!StrUtil.equals(paramsMap.get("responseCode"), "0000")){
				return "<div style=\"text-align:center;font-size:4rem;margin:9%;\">Fail</div>";
			}
		}

		return "<div style=\"text-align:center;font-size:4rem;margin:9%;\">Success</div>";
	}

	/**
	 * post/get form数据格式回调
	 *
	 * @param platformNo
	 * @param payConfigId
	 * @param paramsMap
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/notify/{platformNo}/{payConfigId}")
	String notify(@PathVariable int platformNo, @PathVariable int payConfigId,
			@RequestParam Map<String, String> paramsMap, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		validate(paramsMap.isEmpty(), "参数不存在!", 1015);
		payNotifyService.create(ip, JSONUtil.toJsonStr(paramsMap), reqUrl, "notify", platformNo, payConfigId);
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);

		return payNotifyService.notify(ip, paramsMap, getPayPlatformInfoId(platformNo), payConfigId, proxyIps(request));

	}

	/**
	 * json对象格式回调
	 *
	 * @param platformNo
	 * @param payConfigId
	 * @param paramsMap
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/notify2/{platformNo}/{payConfigId}")
	String notify2(@PathVariable int platformNo, @PathVariable int payConfigId,
			@RequestBody Map<String, String> paramsMap, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		validate(paramsMap.isEmpty(), "参数不存在!", 1015);

		if(payConfigId == 3917){//印度Swipelinc 特殊处理
			paramsMap.put("orderNo", paramsMap.get("mid"));
		}

		payNotifyService.create(ip, JSONUtil.toJsonStr(paramsMap), reqUrl, "notify2", platformNo, payConfigId);
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return payNotifyService.notify(ip, paramsMap, getPayPlatformInfoId(platformNo), payConfigId, proxyIps(request));

	}



	/**
	 * json text/plain 格式回调（带请求头）
	 *
	 * @param platformNo
	 * @param payConfigId
	 * @param params
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/notify3Headers/{platformNo}/{payConfigId}")
	String notify3Headers(@PathVariable int platformNo, @PathVariable int payConfigId, @RequestBody String params,
				   HttpServletRequest request, HttpServletResponse response) throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		validate(params.isEmpty(), "参数不存在!", 1015);

		payNotifyService.create(ip, JSONUtil.toJsonStr(params), reqUrl, "notify3Headers", platformNo, payConfigId);
		JSONObject paramsObj = JSONUtil.parseObj(params);
		Map<String, String> paramsMap = new HashMap<>();
		extracted(paramsObj, paramsMap);

		Enumeration<String> enumheader = request.getHeaderNames();
		while (enumheader.hasMoreElements()){
			String key = enumheader.nextElement().toString();
			paramsMap.put(key,request.getHeader(key));
		}
		paramsMap.put("payConfigId",payConfigId+"");
		paramsMap.put("platformNo", platformNo + "");

		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return payNotifyService.notify(ip, paramsMap, getPayPlatformInfoId(platformNo), payConfigId, proxyIps(request));

	}


	/**
	 * json text/plain 格式回调（带请求头,遍历jsonarray） 3850 印尼wowpay
	 *
	 * @param platformNo
	 * @param payConfigId
	 * @param params
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/notify3Headers2/{platformNo}/{payConfigId}")
	String notify3Headers2(@PathVariable int platformNo, @PathVariable int payConfigId, @RequestBody String params,
						  HttpServletRequest request, HttpServletResponse response) throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		validate(params.isEmpty(), "参数不存在!", 1015);

		payNotifyService.create(ip, JSONUtil.toJsonStr(params), reqUrl, "notify3Headers2", platformNo, payConfigId);
		JSONObject paramsObj = JSONUtil.parseObj(params);
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("str",JSONUtil.toJsonStr(params));
		extracted(paramsObj, paramsMap);
		Enumeration<String> enumheader = request.getHeaderNames();
		while (enumheader.hasMoreElements()){
			String key = enumheader.nextElement().toString();
			paramsMap.put(key,request.getHeader(key));
		}
		paramsMap.put("payConfigId",payConfigId+"");
		paramsMap.put("platformNo", platformNo + "");

		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return payNotifyService.notify(ip, paramsMap, getPayPlatformInfoId(platformNo), payConfigId, proxyIps(request));

	}



	/**
	 * json text/plain 格式回调（带请求头,用于原生body验签）
	 *
	 * @param platformNo
	 * @param payConfigId
	 * @param params
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/notify4Headers/{platformNo}/{payConfigId}")
	String notify4Headers(@PathVariable int platformNo, @PathVariable int payConfigId, @RequestBody String params,
						  HttpServletRequest request, HttpServletResponse response) throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		validate(params.isEmpty(), "参数不存在!", 1015);

		payNotifyService.create(ip, JSONUtil.toJsonStr(params), reqUrl, "notify4Headers", platformNo, payConfigId);
		JSONObject paramsObj = JSONUtil.parseObj(params);
		Map<String, String> paramsMap = new HashMap<>();
		extracted(paramsObj, paramsMap);

		Enumeration<String> enumheader = request.getHeaderNames();
		while (enumheader.hasMoreElements()){
			String key = enumheader.nextElement().toString();
			paramsMap.put(key,request.getHeader(key));
		}
		paramsMap.put("payConfigId",payConfigId+"");
		paramsMap.put("platformNo", platformNo + "");
		paramsMap.put("body",params);

		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return payNotifyService.notify(ip, paramsMap, getPayPlatformInfoId(platformNo), payConfigId, proxyIps(request));

	}





	/**
	 * json text/plain 格式回调
	 *
	 * @param platformNo
	 * @param payConfigId
	 * @param params
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/notify3/{platformNo}/{payConfigId}")
	String notify3(@PathVariable int platformNo, @PathVariable int payConfigId, @RequestBody String params,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		validate(params.isEmpty(), "参数不存在!", 1015);
		payNotifyService.create(ip, JSONUtil.toJsonStr(params), reqUrl, "notify3", platformNo, payConfigId);
		JSONObject paramsObj = JSONUtil.parseObj(params);
		Map<String, String> paramsMap = new HashMap<>();
		extracted(paramsObj, paramsMap);

		if(payConfigId==3873 || payConfigId==3939){  // 印度GoPay
			String orderId = MapUtil.getStr(paramsMap, "extTransactionId", "");

			if (orderId != null && orderId.startsWith("P")) {// 如果订单号是P开头,不是我们系统的订单,发送给小王那边处理.
				// post body 请求方式
				// String notifyUrlXW =
				String notifyUrlXW = "http://baxitoppay2ifpayin23.myrummyclub.in/api/Kopaypayin2ebwfafahook/serverpayin";// 小王的接收地址
				HttpRequest hr = HttpRequest.post(notifyUrlXW).timeout(20 * 1000);// 20秒超时，单位:毫秒
				hr = hr.body(new JSONObject(paramsMap).toString());
				logger.info("paramsMap:{}", paramsMap);
				String resultTextXW = hr.executeAsync().body();
				logger.info("resultTextXW:{}", resultTextXW);
				return resultTextXW;
			}
		}

		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return payNotifyService.notify(ip, paramsMap, getPayPlatformInfoId(platformNo), payConfigId, proxyIps(request));

	}



	/**
	 * 3940  印度Upay
	 *
	 * @param platformNo
	 * @param payConfigId
	 * @param params
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/notifyUpay/{platformNo}/{payConfigId}")
	String notifyUpay(@PathVariable int platformNo, @PathVariable int payConfigId, @RequestBody String params,
				   HttpServletRequest request, HttpServletResponse response) throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		validate(params.isEmpty(), "参数不存在!", 1015);
		payNotifyService.create(ip, JSONUtil.toJsonStr(params), reqUrl, "notifyUpay", platformNo, payConfigId);
		String str = URLUtil.decode(params);
		//str = URLUtil.decode(str);

		Map<String, String> paramsMap = new HashMap<>();
		String[] fields = str.split("&");
		for (int i = 0; i < fields.length; i++) {
			String[] arr = fields[i].split("=",2);
			paramsMap.put(arr[0],arr[1]);
		}
		//JSONObject paramsObj = JSONUtil.parseObj(params);
		//extracted(paramsObj, paramsMap);
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return payNotifyService.notify(ip, paramsMap, getPayPlatformInfoId(platformNo), payConfigId, proxyIps(request));

	}




	/**
	 * json text/plain UrlDecode 格式回调
	 *
	 * @param platformNo
	 * @param payConfigId
	 * @param params
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/notify3UrlDecode/{platformNo}/{payConfigId}")
	String notify3UrlDecode(@PathVariable int platformNo, @PathVariable int payConfigId, @RequestBody String params,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		validate(params.isEmpty(), "参数不存在!", 1015);
		payNotifyService.create(ip, JSONUtil.toJsonStr(params), reqUrl, "notify3UrlDecode", platformNo, payConfigId);
		String decodeURL = URLDecoder.decode(params, "UTF-8");
		params = decodeURL.substring(decodeURL.indexOf("{"), decodeURL.indexOf("}") + 1);
		JSONObject paramsObj = JSONUtil.parseObj(params);
		Map<String, String> paramsMap = new HashMap<>();
		extracted(paramsObj, paramsMap);
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return payNotifyService.notify(ip, paramsMap, getPayPlatformInfoId(platformNo), payConfigId, proxyIps(request));

	}

	@RequestMapping("/notifyJson/{platformNo}/{payConfigId}")
	String notifyJson(@PathVariable int platformNo, @PathVariable int payConfigId, @RequestBody JSONObject params,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		payNotifyService.create(ip, params.toString(), reqUrl, "notifyJson", platformNo, payConfigId);
		Map<String, String> paramsMap = new HashMap<>();
		extracted(params, paramsMap);
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return payNotifyService.notify(ip, paramsMap, getPayPlatformInfoId(platformNo), payConfigId, proxyIps(request));
	}

	@RequestMapping("/notifyTest")
	String notifyJson(HttpServletRequest request, HttpServletResponse response) throws IOException {
		return "";
	}

	private static void extracted(JSONObject params, Map<String, String> paramsMap) {
		for (Entry<String, Object> iterable_element : params.entrySet()) {
			Object obj = iterable_element.getValue();
			Object objKey = iterable_element.getKey();
			if (!params.isNull(objKey.toString())) {// 判断map key值不为空
				if (obj instanceof String) {
					// json二级字符串 转json对象
					if (obj.toString().indexOf("{") == 0
							&& obj.toString().indexOf("}") == obj.toString().length() - 1) {
						JSONObject jsonob = JSONUtil.parseObj(obj.toString());
						extracted(jsonob, paramsMap);
					} else {
						paramsMap.put(iterable_element.getKey(), obj.toString());
					}
					// mapKey form 转json对象
					if (objKey.toString().indexOf("{") == 0
							&& objKey.toString().indexOf("}") == objKey.toString().length() - 1) {
						JSONObject jsonob = JSONUtil.parseObj(objKey.toString());
						extracted(jsonob, paramsMap);
						paramsMap.remove(objKey);
					}
				} else if (obj instanceof Integer) {
					paramsMap.put(iterable_element.getKey(), obj.toString());
				} else if (obj instanceof Boolean) {
					paramsMap.put(iterable_element.getKey(), obj.toString());
				} else if (obj instanceof BigDecimal) {
					paramsMap.put(iterable_element.getKey(), obj.toString());
				} else if (obj instanceof Long) {
					paramsMap.put(iterable_element.getKey(), obj.toString());
				} else if (obj instanceof Double) {
					paramsMap.put(iterable_element.getKey(), String.format("%.2f", obj));// 大炮支付 double数值回滚
				} else if (obj instanceof JSONArray) {//新增 JSONArray 不做迭代处理 针对rozarPay 2278
					//todo
				}else{
					JSONObject jsonob = (JSONObject) iterable_element.getValue();
					extracted(jsonob, paramsMap);
				}
			}
		}
	}

	/**
	 * xml格式回调
	 *
	 * @param platformNo
	 * @param payConfigId
	 * @param params
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/notify4/{platformNo}/{payConfigId}")
	String notify4(@PathVariable int platformNo, @PathVariable int payConfigId, @RequestBody String params,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		validate(params.isEmpty(), "参数不存在!", 1015);
		payNotifyService.create(ip, JSONUtil.toJsonStr(params), reqUrl, "notify4", platformNo, payConfigId);
		Map<String, Object> paramsObjectMap = XmlUtil.xmlToMap(params);
		Map<String, String> paramsMap = new HashMap<>();
		for (Entry<String, Object> payReqParam : paramsObjectMap.entrySet()) {
			paramsMap.put(payReqParam.getKey(), payReqParam.getValue().toString());
		}
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return payNotifyService.notify(ip, paramsMap, getPayPlatformInfoId(platformNo), payConfigId, proxyIps(request));
	}

	/**
	 * post/get form数据格式回调 转Json
	 *
	 * @param platformNo
	 * @param payConfigId
	 * @param notifyMap
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/notifyFormJson/{platformNo}/{payConfigId}")
	String notifyFormJson(@PathVariable int platformNo, @PathVariable int payConfigId,
			@RequestParam Map<String, String> notifyMap, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		validate(notifyMap.isEmpty(), "参数不存在!", 1015);
		payNotifyService.create(ip, JSONUtil.toJsonStr(notifyMap), reqUrl, "notifyFormJson", platformNo, payConfigId);
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, notifyMap, reqUrl);
		JSONObject jsonObject = JSONUtil.parseObj(notifyMap);
		Map<String, String> paramsMap = new HashMap<>();
		extracted(jsonObject, paramsMap);
		return payNotifyService.notify(ip, paramsMap, getPayPlatformInfoId(platformNo), payConfigId, proxyIps(request));

	}

	/**
	 * json text/plain 格式回调 远华支付 id 2496
	 *
	 * @param platformNo
	 * @param payConfigId
	 * @param params
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/notifyYH/{platformNo}/{payConfigId}")
	String notifyYH(@PathVariable int platformNo, @PathVariable int payConfigId, @RequestBody String params,
				   HttpServletRequest request, HttpServletResponse response) throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		validate(params.isEmpty(), "参数不存在!", 1015);
		payNotifyService.create(ip, JSONUtil.toJsonStr(params), reqUrl, "notifyYH", platformNo, payConfigId);
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("orderID", params);//订单放入加密串 防止判断空拦截
		paramsMap.put("text", params);
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return payNotifyService.notify(ip, paramsMap, getPayPlatformInfoId(platformNo), payConfigId, proxyIps(request));

	}

	/**
	 * json text/plain 格式回调 ippoSdk支付 id 2890
	 *
	 * @param platformNo
	 * @param payConfigId
	 * @param params
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/notifyIppoSdk/{platformNo}/{payConfigId}")
	String notifyIppoSdk(@PathVariable int platformNo, @PathVariable int payConfigId, @RequestBody String params,
				   HttpServletRequest request, HttpServletResponse response) throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		validate(params.isEmpty(), "参数不存在!", 1015);
		payNotifyService.create(ip, JSONUtil.toJsonStr(params), reqUrl, "notify3", platformNo, payConfigId);
		JSONObject paramsObj = JSONUtil.parseObj(params);
		Map<String, String> paramsMap = new HashMap<>();
		extracted(paramsObj, paramsMap);
		paramsMap.put("orderId", params);//订单放入加密串 防止判断空拦截
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return payNotifyService.notify(ip, paramsMap, getPayPlatformInfoId(platformNo), payConfigId, proxyIps(request));

	}

	/**
	 * json对象格式回调 GPAY 支付id:3167 客户端回调
	 *
	 * @param platformNo
	 * @param payConfigId
	 * @param paramsMap
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/notifyGpay/{platformNo}/{payConfigId}")
	String notifyGpay(@PathVariable int platformNo, @PathVariable int payConfigId,
				   @RequestBody Map<String, String> paramsMap, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		validate(paramsMap.isEmpty(), "参数不存在!", 1015);

		payNotifyService.create(ip, JSONUtil.toJsonStr(paramsMap), reqUrl, "notifyGpay", platformNo, payConfigId);
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return payNotifyService.notify(ip, paramsMap, getPayPlatformInfoId(platformNo), payConfigId, proxyIps(request));

	}

	/**
	 * jsonArray text/plain 格式回调 safexpay支付 id 3173
	 *
	 * @param platformNo
	 * @param payConfigId
	 * @param params
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/notifySafex/{platformNo}/{payConfigId}")
	String notifySafex(@PathVariable int platformNo, @PathVariable int payConfigId, @RequestBody String params,
						 HttpServletRequest request, HttpServletResponse response) throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		validate(params.isEmpty(), "参数不存在!", 1015);
		payNotifyService.create(ip, JSONUtil.toJsonStr(params), reqUrl, "notifySafex", platformNo, payConfigId);
		JSONArray paramsArr = JSONUtil.parseArray(params).getJSONObject(0).getJSONObject("Data").getJSONArray("SaleRP");
		JSONObject paramsObj = paramsArr.getJSONObject(0);
		Map<String, String> paramsMap = new HashMap<>();
		extracted(paramsObj, paramsMap);
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return payNotifyService.notify(ip, paramsMap, getPayPlatformInfoId(platformNo), payConfigId, proxyIps(request));

	}

	/**
	 * json text/plain 格式回调 notify3Node 多层级遍历
	 *{"data":{"id":"19696441999"}}, map = {data.id=19696441999}
	 * @param platformNo
	 * @param payConfigId
	 * @param params
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/notify3Node/{platformNo}/{payConfigId}")
	String notify3Node(@PathVariable int platformNo, @PathVariable int payConfigId, @RequestBody String params,
				   HttpServletRequest request, HttpServletResponse response) throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		validate(params.isEmpty(), "参数不存在!", 1015);
		payNotifyService.create(ip, JSONUtil.toJsonStr(params), reqUrl, "notify3", platformNo, payConfigId);
		JSONObject paramsObj = JSONUtil.parseObj(params);
		Map<String, String> paramsMap = new HashMap<>();
		extractedNode(paramsObj, paramsMap, "");
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return payNotifyService.notify(ip, paramsMap, getPayPlatformInfoId(platformNo), payConfigId, proxyIps(request));

	}

	private static void extractedNode(JSONObject params, Map<String, String> paramsMap, String parentKey) {
		for (Entry<String, Object> iterable_element : params.entrySet()) {
			Object obj = iterable_element.getValue();
			Object objKey = iterable_element.getKey();
			String mapKey = iterable_element.getKey();
			if(StrUtil.isNotEmpty(parentKey)){
                mapKey = parentKey + "." + mapKey;
			}
			if (!params.isNull(objKey.toString())) {// 判断map key值不为空
				if (obj instanceof String) {
					// json二级字符串 转json对象
					if (obj.toString().indexOf("{") == 0
							&& obj.toString().indexOf("}") == obj.toString().length() - 1) {
						JSONObject jsonob = JSONUtil.parseObj(obj.toString());
						extractedNode(jsonob, paramsMap, mapKey);
					} else {
						paramsMap.put(mapKey, obj.toString());
					}
					// mapKey form 转json对象
					if (objKey.toString().indexOf("{") == 0
							&& objKey.toString().indexOf("}") == objKey.toString().length() - 1) {
						JSONObject jsonob = JSONUtil.parseObj(objKey.toString());
						extractedNode(jsonob, paramsMap, mapKey);
						paramsMap.remove(objKey);
					}
				} else if (obj instanceof Integer) {
					paramsMap.put(mapKey, obj.toString());
				} else if (obj instanceof Boolean) {
					paramsMap.put(mapKey, obj.toString());
				} else if (obj instanceof Double) {
					paramsMap.put(mapKey, String.format("%.2f", obj));// 大炮支付 double数值回滚
				}else if (obj instanceof Long) {//新增 Long类型处理 针对墨西哥 3376 墨西哥mercadopago支付
					paramsMap.put(mapKey, obj.toString());
				} else if (obj instanceof JSONArray) {//新增 JSONArray 不做迭代处理
					//todo
				}else{
					JSONObject jsonob = (JSONObject) iterable_element.getValue();
					extractedNode(jsonob, paramsMap, mapKey);
				}
			}
		}
	}

	/**
	 * Astropay json对象格式回调
	 *
	 * @param platformNo
	 * @param payConfigId
	 * @param paramsMap
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/notifyAstro/{platformNo}/{payConfigId}")
	String notifyAstro(@PathVariable int platformNo, @PathVariable int payConfigId,
				   @RequestBody Map<String, String> paramsMap, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		validate(paramsMap.isEmpty(), "参数不存在!", 1015);
		String Signature = request.getHeader("Signature");// Astropay 签名
		if (StrUtil.isNotEmpty(Signature)) {// 回调签名存储在 Signature里
			paramsMap.put("Signature", Signature);
		}

		payNotifyService.create(ip, JSONUtil.toJsonStr(paramsMap), reqUrl, "notifyAstro", platformNo, payConfigId);
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return payNotifyService.notify(ip, paramsMap, getPayPlatformInfoId(platformNo), payConfigId, proxyIps(request));

	}



	@Autowired
	OrderInfoService orderInfoService;

	/**
	 * PayFair form数据格式回调
	 * @param paramsMap
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/notify/273/3720")
	String notifyPayFair(@RequestParam Map<String, String> paramsMap, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		Integer payConfigId = 3720;
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		String orderId = paramsMap.get("order_id");
		payNotifyService.create(ip, JSONUtil.toJsonStr(paramsMap), reqUrl, "notify", 273, payConfigId);

		if(orderId != null && orderId.startsWith("P")){
			Map<String, Object> xiaowangMap = new TreeMap<>();
			xiaowangMap.putAll(paramsMap);
			HttpRequest hr = HttpRequest.post("https://payfairpayind32we2332hk.myrummytime.com/api/Payfairpayin2342hokfm/server").form(xiaowangMap).timeout(20 * 1000);
			String resultText = hr.executeAsync().body();
			if(StrUtil.isNotEmpty(resultText) && StrUtil.equals(resultText, "ok")){
				return resultText;
			}
		}

		OrderInfo orderInfo = orderInfoService.getOrderInfo(orderId);
		if(orderInfo!=null){
			payConfigId = orderInfo.getPayConfigId();
		}
		validate(paramsMap.isEmpty(), "参数不存在!", 1015);

		logger.info("platformNo:{},ip:{},{},URL:{}", 273, ip, paramsMap, reqUrl);

		return payNotifyService.notify(ip, paramsMap, getPayPlatformInfoId(273), payConfigId, proxyIps(request));

	}


	/**
	 * 巴基斯坦Walee
	 *
	 * @param platformNo
	 * @param payConfigId
	 * @param paramsMap
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/notifyWalee/{platformNo}/{payConfigId}")
	String notifyWalee(@PathVariable int platformNo, @PathVariable int payConfigId,
					   @RequestBody Map<String, String> paramsMap, HttpServletRequest request, HttpServletResponse response)
			throws IOException {


		String loanId = paramsMap.get("loanId");
		if (!StringUtils.isEmpty(loanId)) {
			//walee 代付转发
			String payoutNotify = "http://180.150.129.144:9030/api/js/result_waleePayXYtx";
			Map<String, String> param = new HashMap<>();
			param.put("status", paramsMap.get("status"));
			param.put("loanId", paramsMap.get("loanId"));
			param.put("amount", paramsMap.get("amount"));
			param.put("orderId", paramsMap.get("orderId"));
			param.put("actionDate", paramsMap.get("actionDate"));

			HttpRequest hr = HttpRequest.post(payoutNotify).timeout(ThirdUtil.HTTP_REQUEST_TIMEOUT);// 超时，毫秒
			hr = hr.body(new JSONObject(param).toString());
			String resultText = hr.executeAsync().body();
			return resultText;
		}
		String ip = getIp(request);
		String reqUrl = request.getRequestURL().toString();
		validate(paramsMap.isEmpty(), "参数不存在!", 1015);

		String signature = request.getHeader("X-RESP-SIGNATURE");// 用于3725 印度tarspay签名
		if (StrUtil.isNotEmpty(signature)) {// 回调签名存储在 signature
			paramsMap.put("X-RESP-SIGNATURE", signature);
		}

		payNotifyService.create(ip, JSONUtil.toJsonStr(paramsMap), reqUrl, "notify2", platformNo, payConfigId);
		logger.info("platformNo:{},ip:{},{},URL:{}", platformNo, ip, paramsMap, reqUrl);
		return payNotifyService.notify(ip, paramsMap, getPayPlatformInfoId(platformNo), payConfigId, proxyIps(request));

	}











	@RequestMapping("/notifyOnepay")
	String notifyOnepay(@RequestBody Map<String, String> paramsMap) {
		String payoutNotify = "http://203.60.66.159:9030/api/js/result_OnePayXYtx";
		// 创建一个HashMap存储请求中的键值对
		Map<String, Object> map = new HashMap<>();
		map.put("data", MapUtil.getStr(paramsMap,"data",""));
		map.put("merchantNo", MapUtil.getStr(paramsMap,"merchantNo",""));

		// 将map转换为JSON字符串
		String jsonStr = JSONUtil.toJsonStr(map);

		// 使用Hutool发送HTTP POST请求
		HttpResponse hr = HttpRequest.post(payoutNotify).body(jsonStr).header("Content-Type", "application/json").execute();

		if (hr.isOk()) {
			return "success";
		} else {
			return "fail";
		}

	}


	@RequestMapping("/notifyOnepay1007")
	String notifyOnepay1007(@RequestBody Map<String, String> paramsMap) {
		String payoutNotify = "http://203.60.66.159:9030/api/js/result_OnePayXYtx1007";
		// 创建一个HashMap存储请求中的键值对
		Map<String, Object> map = new HashMap<>();
		map.put("data", MapUtil.getStr(paramsMap,"data",""));
		map.put("merchantNo", MapUtil.getStr(paramsMap,"merchantNo",""));

		// 将map转换为JSON字符串
		String jsonStr = JSONUtil.toJsonStr(map);

		// 使用Hutool发送HTTP POST请求
		HttpResponse hr = HttpRequest.post(payoutNotify).body(jsonStr).header("Content-Type", "application/json").execute();

		if (hr.isOk()) {
			return "success";
		} else {
			return "fail";
		}

	}

	public static void main(String[] args) {
		Map<String,String> tree = new TreeMap<>();
		tree.put("pid", "1");
		tree.put("type", "2");
		tree.put("out_trade_no", "2");
		tree.put("notify_url", "2");
		tree.put("name", "2");
		tree.put("money", "2");
		tree.put("clientip", "2");
		tree.put("return_url", "2");


		StringBuilder sb = new StringBuilder();
		tree.forEach((key, value) -> sb.append(key).append("=").append(value).append("&"));

		String a = sb.toString();
		System.out.println(a);
		System.out.println(System.currentTimeMillis());



	}

}
