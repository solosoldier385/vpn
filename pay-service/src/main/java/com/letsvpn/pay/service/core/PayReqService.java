package com.letsvpn.pay.service.core;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.URLEncoder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.pay.dto.IPayThirdRequest;
import com.letsvpn.pay.entity.*;
import com.letsvpn.pay.mapper.MerchantInfoMapper;
import com.letsvpn.pay.mapper.OrderInfoMapper;
import com.letsvpn.pay.mapper.PayConfigChannelMapper;
import com.letsvpn.pay.mapper.PayConfigInfoMapper;
import com.letsvpn.pay.mapper.ext.ExtPayPlatformInfoMapper;
import com.letsvpn.pay.util.KeyValue;
import com.letsvpn.pay.util.PayCallMethod;
import com.letsvpn.pay.util.PayConfigEnum;
import com.letsvpn.pay.vo.PayResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PayReqService extends BaseService {

	@Autowired
	ExtPayPlatformInfoMapper extPayPlatformInfo;
	@Autowired
	PayConfigChannelMapper payConfigChannelMapper;
	@Autowired
	PayConfigInfoMapper payConfigInfoMapper;
	@Autowired
	MerchantInfoMapper merchantInfoMapper;
	@Autowired
	ApplicationContext applicationContext;
	// @Autowired
//	PayFormService payFormService;
	@Autowired
	RedisService redisService;

	@Autowired
	OrderInfoMapper orderInfoMapper;

	private static Integer pos = 0;

	@Autowired
	PayConfigParameterService payConfigParameterService;
//	private Map<String, String> settings(String platformNo, int payConfigId) {
//
//		PaySettingExample example = new PaySettingExample();
//		example.createCriteria().andKeyNameIn(Arrays.asList("callbackUrl", "notifyUrl"));
//		List<PaySetting> list = paySettingMapper.selectByExample(example);
//		return list.stream().collect(
//				Collectors.toMap(PaySetting::getKeyName, a -> a.getKeyString() + platformNo + "/" + payConfigId));
//	}
//
//	private String length(String str, int length) {
//		String recordIdStr = "000000000" + str;
//		return recordIdStr.substring(recordIdStr.length() - length);
//	}

	public KeyValue<PayCallMethod, String> req(Map<String, String> paramsMap, PayPlatformInfo info, String ip,
											   StringBuffer requestURL, String requestURI, String queryString, HttpServletResponse response,Long uid) {
		log.info("{}", JSONUtil.toJsonStr(info));
//		String platformNo = MapUtils.getString(paramsMap, "pf");
		String amount = MapUtil.getStr(paramsMap, "amount");
		// String sign = MapUtils.getString(paramsMap, "sign");
		String frontId = MapUtil.getStr(paramsMap, "fid");
		String extend1 = MapUtil.getStr(paramsMap, "extend1", "");
		String extend2 = MapUtil.getStr(paramsMap, "extend2", "");
		String extend3 = MapUtil.getStr(paramsMap, "extend3", "");
//		try {
//			extend1 = URLDecoder.decode(extend1, "UTF-8");
//			extend2 = URLDecoder.decode(extend2, "UTF-8");
//		} catch (Exception e) {
//			log.error("{} {} {}", extend1, extend2, e.getMessage(), e);
//		}
		Long channelId = MapUtil.getLong(paramsMap, "cid");
		Integer userId = Math.toIntExact(uid);
		Integer gameId = Integer.valueOf("111111");

		PayConfigChannel channel = getPayConfigChannel(channelId);
		Integer payConfigId = channel.getPayConfigId();
		PayConfigInfo payConfigInfo = getPayConfigInfo(payConfigId);

		String reqDomain = payConfigInfo.getReqDomain();
		if (StrUtil.isNotBlank(reqDomain)) {

			log.info("requestURL:{}, reqDomain:{}", requestURL, reqDomain);
			if (requestURL.indexOf(reqDomain.substring(reqDomain.indexOf("://"))) == -1) {
				String url = reqDomain + requestURI + "?" + queryString;
				log.info("url:{}", url);
				response.setHeader("referer", reqDomain);
				return new KeyValue<PayCallMethod, String>(PayCallMethod.redirect, url);
			}

		}

		// 该通道暂时关闭
		validate(payConfigInfo.getNullify() != 0, "该支付通道已经关闭" + channelId, 8008, channelId + "");
		Integer platformId = info.getPlatformId();
		MerchantInfo merchantInfo = getMerchantInfo(info, payConfigId, payConfigInfo, platformId);

		IPayThirdRequest req = findPayThird(payConfigInfo);

		Date now = new Date();

		String chartcode = payConfigInfo.getShortCode();
		String orderId = null;
		if (StrUtil.equals(chartcode, "char15")) {
			orderId = redisService.idLength36(now, 15);
		} else if (StrUtil.equals(chartcode, "CHAR12")) {
			orderId = redisService.idLength362(now, 12).toUpperCase();
		} else {
			orderId = redisService.id4h(payConfigInfo.getShortCode(), platformId.toString(), now);

		}

		OrderInfo order = getOrderInfo(frontId, platformId);
		// 重复提交
		validate(order != null, "重复提交" + frontId, 8007, frontId);
//		if (order != null) {
//			orderId = order.getOrderId();
//		}

		// settings(platformNo, payConfigId)
		Map<String, String> param = new HashMap<String, String>();
		param.put("platformId", platformId.toString());
		param.put("userId", userId.toString());
		param.put("gameId", gameId.toString());// 游戏id
		param.put("createIp", ip); // 用户ip
		param.put("frontId", frontId); // 订单关联id
		param.put("extend1", extend1);
		param.put("extend2", extend2);
		param.put("extend3", extend3);
		extracted1(amount, orderId, param);
		extracted2(info, payConfigInfo, param);
		extracted3(now, param);
		extracted4(merchantInfo, param);
		log.debug("allparam:{}", param);

		List<PayConfigParameter> payConfigParameters = payConfigParameterService.listPayConfigParameter(payConfigId,
				channel.getId(), PayConfigEnum.request);
		for (PayConfigParameter payConfigParameter : payConfigParameters) {
			if ("text".equals(payConfigParameter.getType())) {
				param.put(payConfigParameter.getPayName(), payConfigParameter.getPayValue());
			}
		}
		PayResultData result = req.executeReq(merchantInfo, payConfigInfo, channel, param, payConfigParameters);
		validate(result == null, "没有结果", 8006);

		if (order == null && result.isSaveData()) {
			extend1 = StrUtil.isNotEmpty(result.getExtend1()) ? result.getExtend1() : extend1;
			extend2 = StrUtil.isNotEmpty(result.getExtend2()) ? result.getExtend2() : extend2;
			extend3 = StrUtil.isNotEmpty(result.getExtend3()) ? result.getExtend3() : extend3;
			order = createOrderInfo(ip, amount, frontId, channelId, userId, platformId, now, orderId, payConfigId,
					gameId, result.getThirdOrderId(), extend1, extend2, extend3);
			log.info("order:{}", JSONUtil.toJsonStr(order));
		}
		log.debug("result:{}", JSONUtil.toJsonStr(result));
		if (result.getMethod().compareTo(PayCallMethod.form) == 0) {
			return new KeyValue<PayCallMethod, String>(PayCallMethod.form, result.getHtml());
		} else if (result.getMethod().compareTo(PayCallMethod.redirect) == 0) {
			return new KeyValue<PayCallMethod, String>(PayCallMethod.redirect, result.getLink());
		} else if (result.getMethod().compareTo(PayCallMethod.sdk) == 0) {
			return new KeyValue<PayCallMethod, String>(PayCallMethod.sdk, result.getLink());
		} else {
			validate(true, "解析器不存在", 1016);
		}
		return null;

	}

	public void extracted1(String amount, String orderId, Map<String, String> param) {
		param.put("orderId", orderId); // 订单号
		param.put("amount", amount); // 充值元
		param.put("amount1", new BigDecimal(amount).setScale(1, BigDecimal.ROUND_DOWN).toPlainString()); // 充值元 +.0
		param.put("amount2", new BigDecimal(amount).setScale(2, BigDecimal.ROUND_DOWN).toPlainString()); // 充值元 +.00
		param.put("amount4", new BigDecimal(amount).setScale(4, BigDecimal.ROUND_DOWN).toPlainString()); // 充值元 +.00
		param.put("amountFen", new BigDecimal(amount).multiply(BigDecimal.valueOf(100)).toString()); // 分
		param.put("amountLi", new BigDecimal(amount).multiply(BigDecimal.valueOf(1000)).toString()); // 厘

	}

	public void extracted2(PayPlatformInfo info, PayConfigInfo payConfigInfo, Map<String, String> param) {
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

	public void extracted4(MerchantInfo merchantInfo, Map<String, String> param) {
		param.put("appId", merchantInfo.getAppId()); // 商户id
		param.put("privateKey", merchantInfo.getPrivateKey());// 秘钥
		param.put("privateKey1", merchantInfo.getPrivateKey1());// 秘钥
		param.put("privateKey2", merchantInfo.getPrivateKey2());// 秘钥
		param.put("privateKey3", merchantInfo.getPrivateKey3());// 秘钥
		param.put("privateKey4", merchantInfo.getPrivateKey4());// 秘钥
	}

	public void extracted3(Date now, Map<String, String> param) {
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

		// 到期时间格式 yyyy-MM-dd HH:mm:ss
		param.put("orderExpire", DateUtil.format(DateUtil.offsetMinute(now, 10), DatePattern.NORM_DATETIME_PATTERN));

		// 到期时间格式 20191025162144123 yyyyMMddHHmmssSSS
		param.put("orderExpire2",
				DateUtil.format(DateUtil.offsetMinute(now, 10), DatePattern.PURE_DATETIME_MS_PATTERN));

		// 到期时间格式 20191025162144 yyyyMMddHHmmss
		param.put("orderExpire3", DateUtil.format(DateUtil.offsetMinute(now, 10), DatePattern.PURE_DATETIME_PATTERN));

		// 带有上午下午标识的时间格式 AM PM
		param.put("createTime7", new SimpleDateFormat("yyyy-MM-dd hh:mm:ssa").format(new Date()).replace("上午", "AM")
				.replace("下午", "PM"));

	}

	private OrderInfo createOrderInfo(String ip, String amount, String frontId, Long channelId, Integer userId,
			Integer platformId, Date now, String orderId, Integer payConfigId, Integer gid, String otherOrderId,
			String extend1, String extend2, String extend3) {
		OrderInfo order = new OrderInfo();
		// order.setChannelId(channelId);
		order.setCreateIp(ip);
		order.setCreateTime(now);
		order.setFrontId(frontId);

		order.setNoticeStatus(0);
		order.setOrderId(orderId);
		order.setPayConfigChannelId(channelId);
		order.setPayConfigId(payConfigId);
		order.setPlatformId(platformId);
		order.setGameId(gid);
		order.setOtherOrderId(otherOrderId);
		order.setExtend1(extend1);
		order.setExtend2(extend2);
		order.setExtend3(extend3);
		order.setReqAmount(new BigDecimal(amount));
		order.setStatus(0);
		order.setUserId(userId);
		orderInfoMapper.insert(order);
		return order;
	}

	private OrderInfo getOrderInfo(String frontId, Integer platformId) {


		QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("platform_id", platformId);
		queryWrapper.eq("front_id", frontId);
		List<OrderInfo> list = orderInfoMapper.selectList(queryWrapper);

		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	private IPayThirdRequest findPayThird(PayConfigInfo payConfigInfo) {
//		Map<String, IPayThirdRequest> maps = applicationContext.getBeansOfType(IPayThirdRequest.class);
//		IPayThirdRequest req = maps.get(payConfigInfo.getThirdService());
		IPayThirdRequest req = applicationContext.getBean(payConfigInfo.getThirdService(), IPayThirdRequest.class);
		if (req == null) {
//			log.info("{}", maps.keySet());
			validate(req == null, payConfigInfo.getTitle() + "没有配置执行器", 8003, payConfigInfo.getTitle());
		}
		return req;
	}

	private MerchantInfo getMerchantInfo(PayPlatformInfo info, Integer payConfigId, PayConfigInfo payConfigInfo,
			Integer platformId) {
		MerchantInfo merchantInfo = extPayPlatformInfo.getMerchantInfo(payConfigId, platformId);
		validate(merchantInfo == null,
				info.getTitle() + " no config[" + payConfigInfo.getTitle() + "]" + payConfigId + "-" + platformId, 404);
		log.info("merchantInfo:{}", JSONUtil.toJsonStr(merchantInfo));
		return merchantInfo;
	}

	private PayConfigInfo getPayConfigInfo(Integer payConfigId) {

		PayConfigInfo payConfigInfo = payConfigInfoMapper.selectById(payConfigId);
		validate(payConfigInfo == null, "该支付不存在" + payConfigId, 8004, payConfigId + "");
		log.info("payConfigInfo:{}", JSONUtil.toJsonStr(payConfigInfo));
		return payConfigInfo;
	}

	private PayConfigChannel getPayConfigChannel(Long channelId) {
		PayConfigChannel channel = payConfigChannelMapper.selectById(channelId);
		validate(channel == null, "该支付通道不存在!" + channelId, 8005, channelId + "");
		log.info("channel:{}", JSONUtil.toJsonStr(channel));
		return channel;
	}

}
