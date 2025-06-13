package com.letsvpn.pay.service.core;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.letsvpn.pay.dto.IPayNotifyHandle;
import com.letsvpn.pay.dto.IPayNotifyResultHandle;
import com.letsvpn.pay.entity.*;
import com.letsvpn.pay.mapper.*;
import com.letsvpn.pay.util.*;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class PayNotifyService extends BaseService {
	//
//	@Autowired
//	ExtPayPlatformInfoMapper extPayPlatformInfo;
	@Autowired
	OrderCallbackMapper orderCallbackMapper;
	// @Autowired
//	PayConfigChannelMapper payConfigChannelMapper;
//	@Autowired
//	PayConfigInfoMapper payConfigInfoMapper;
//
	@Autowired
	PayConfigNotifyMapper payConfigNotifyMapper;
	@Autowired
	MerchantInfoMapper merchantInfoMapper;
	@Autowired
	PayConfigIpMapper payConfigIpMapper;
	@Autowired
	OrderNotifyRecordMapper orderNotifyRecordMapper;
	@Autowired
	ApplicationContext applicationContext;
	//
	@Autowired
	OrderBuildErrorService orderBuildErrorService;
	@Autowired
	RedisService redisService;
	// @Autowired
//	PaySettingMapper paySettingMapper;
	@Autowired
	OrderInfoService orderInfoService;



	@Autowired
	PayConfigParameterService payConfigParameterService;



	public Long create(String callbackIp, String param, String reqUrl, String type, Integer platformId,
			Integer payConfigId) {
		OrderCallback record = new OrderCallback();
		record.setCreateIp(callbackIp);
		record.setCreateTime(new Date());
		record.setParam(param);
		record.setType(type);
		record.setPayConfigId(payConfigId);
		record.setPlatformNo(platformId.toString());
		record.setReqUrl(reqUrl);
		orderCallbackMapper.insert(record);

		//

		return record.getId();
	}

	PayConfigNotify getPayConfigNotify(Integer payConfigId) {
		QueryWrapper<PayConfigNotify> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("pay_config_id", payConfigId);
		return payConfigNotifyMapper.selectOne(queryWrapper);
	}

	OrderInfo getOrderInfoByOrderId(String orderId) {
		return orderInfoService.getOrderInfo(orderId);
	}

	private void callbackIPCheck(String callbackIp, int payConfigId, int platformId, String orderId) {
		QueryWrapper<PayConfigIp> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("callback_ip", callbackIp);
		queryWrapper.eq("pay_config_id", payConfigId);
		PayConfigIp record = payConfigIpMapper.selectOne(queryWrapper);

		if (record == null) {
			orderBuildErrorService.add(platformId, payConfigId, 0L, "IP回调", callbackIp, "第一次回调,请检查是否合法" + orderId, "");
			record = new PayConfigIp();
			record.setCallbackIp(callbackIp);
			record.setPayConfigId(payConfigId);
			record.setCreateTime(new Date());
			record.setBackCount(0L);
			record.setNullify(1);
			record.setUpdateTime(new Date());
			payConfigIpMapper.insert(record);
		} else {
			if (record.getNullify() != 0) {
				orderBuildErrorService.add(platformId, payConfigId, 0L, "IP回调", callbackIp, "非法访问,多次回调," + orderId, "");
			}
		}

		// "错误码:14:非法访问!!!"
		validateParam(record.getNullify() != 0, 1003, "!");

		// 每天只更新一次
		if (!DateUtil.isSameDay(new Date(), record.getUpdateTime())) {
			PayConfigIp record1 = new PayConfigIp();
			record1.setCallbackIp(callbackIp);
			record1.setPayConfigId(payConfigId);
			record1.setUpdateTime(new Date());
			record1.setBackCount(record.getBackCount() + 1);

			UpdateWrapper<PayConfigIp> updateWrapper = new UpdateWrapper<>();
			updateWrapper.eq("callback_ip", callbackIp);
			updateWrapper.eq("pay_config_id", payConfigId);

			payConfigIpMapper.update(record1,updateWrapper);
		}
	}

	public String notify(String callbackIp, Map<String, String> paramsMap, PayPlatformInfo payPlatformInfo,
			int payConfigId, List<String> proxyIps) {
		Integer platformId = payPlatformInfo.getPlatformId();
		PayConfigNotify notify = getPayConfigNotify(payConfigId);
		validate(notify == null, "PayConfigNotify 没有配置 " + payConfigId, 5000, "PayConfigNotify:" + payConfigId);

		MerchantInfo merchantInfo = getMerchantInfo(payConfigId, platformId);

		paramExtraction(paramsMap, payPlatformInfo, merchantInfo, notify, callbackIp);

		String orderParam = notify.getOrderParam();
		String orderId = MapUtil.getStr(paramsMap, orderParam, "");

		if (notify.getIpCheck() == null) {
			notify.setIpCheck(100);
		}
		// -1 不校验,其他都要校验
		if (notify.getIpCheck() != -1) {
			callbackIPCheck(callbackIp, payConfigId, platformId, orderId);

//			if(proxyIps != null) {
//				for (String proxyIp : proxyIps) {
//					if (!PayUtil.isCloudflare(proxyIp)) { // cf代理ip控制
//						log.info("not isCloudflare:{}", proxyIp);
//						proxyCheck(new ArrayList<>(Arrays.asList(proxyIp)));
//					}
//				}
//			}
		} else {
			log.info("--payConfigId:{}---getIpCheck:{}---", payConfigId, notify.getIpCheck());
		}

		validate(StrUtil.isEmpty(orderId), "订单id为空!", 8001, orderId);
		String key = RedisConstant.orderlock(orderId);
		validate(!redisService.lock(key), "休息一下" + orderId, 8002, orderId);
		try {
			return endStep(paramsMap, payPlatformInfo, orderId, notify, merchantInfo);
		} finally {
			redisService.unLock(key);
		}

	}





	public MerchantInfo getMerchantInfo(int payConfigId, Integer platformId) {

		QueryWrapper<MerchantInfo> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("platform_id", platformId);
		queryWrapper.eq("pay_config_id", payConfigId);
		return merchantInfoMapper.selectOne(queryWrapper);
	}

	private void paramExtraction(Map<String, String> paramsMap, PayPlatformInfo payPlatformInfo,
			MerchantInfo merchantInfo, PayConfigNotify notify, String ip) {
		IPayNotifyHandle pe = findPayThird(notify.getType(), IPayNotifyHandle.class);
		if (pe != null) {
			// 提取参数
			pe.paramExtraction(paramsMap, merchantInfo);

			// 验签
			if (notify.getSignCheck() != null && notify.getSignCheck() > 0) {

				Map<String, String> tempMap = new HashMap<String, String>();
				tempMap.putAll(paramsMap);
				extracted4(merchantInfo, tempMap);
				log.debug("signCheck:{}", tempMap);

				List<PayConfigParameter> payConfigParameters = payConfigParameterService
						.listPayConfigParameter(notify.getPayConfigId(), 0, PayConfigEnum.notify);
//				for (PayConfigParameter payConfigParameter : payConfigParameters) {
//					if ("text".equals(payConfigParameter.getType())) {
//						param.put(payConfigParameter.getPayName(), payConfigParameter.getPayValue());
//					}
//				}
				log.debug("payConfigParameters:{}", payConfigParameters);
				log.debug("paramsMap:{}", tempMap);
				StringBuffer logText = new StringBuffer();

				// tempMap 这个参数不可以变化,如果变化就正常代码有异常,
				String signParamChange1 = SecureUtil.signParamsMd5(tempMap);
				boolean signCheck = false;
				try {
					signCheck = pe.signCheck(tempMap, payConfigParameters, logText);
				} catch (Exception e) {
					signCheck = false;
					log.error("tempMap:{}, payConfigParameters:{}, logText:{},{}", tempMap, payConfigParameters,
							logText, e.getMessage(), e);
				}

				String signParamChange2 = SecureUtil.signParamsMd5(tempMap);
				boolean error = signParamChange1.equals(signParamChange2);
				log.debug("info:{},{}:{}", error, signParamChange1, signParamChange2);
				if (!error) {
					log.info("错误码:18,非法代码,{}", tempMap);
					// validate(!error, "错误码:18,非法代码");
				}

				log.debug("signCheck:{}", signCheck);
				logText.append("signCheck:").append(signCheck);

				String className = pe.getClass().getSimpleName();

				Integer dataSignCheck = notify.getSignCheck();

				extracted(tempMap, payPlatformInfo, merchantInfo, payConfigParameters, logText,
						signCheck + ":" + dataSignCheck, className, ip);

				if (dataSignCheck != null && dataSignCheck == 100) {
					validate(!signCheck, "验签不通过!", 1019);
				}
			}

		}

	}

	private void extracted(Map<String, String> paramsMap, PayPlatformInfo payPlatformInfo, MerchantInfo merchantInfo,
			List<PayConfigParameter> payConfigParameters, StringBuffer logText, String signCheck, String className,
			String createIp) {
		OrderNotifyRecord record = new OrderNotifyRecord();
		record.setCreateTime(new Date());
		record.setLogText(logText.toString());
		record.setParamsMap(paramsMap.toString());
		record.setPayConfigId(merchantInfo.getPayConfigId());
		record.setPayConfigParameters(JSONUtil.toJsonStr(payConfigParameters));
		record.setPlatformId(payPlatformInfo.getPlatformId());
		record.setCreateIp(createIp);
		record.setSignCheck(signCheck);
		record.setClassName(className);
		orderNotifyRecordMapper.insert(record);
	}

	public void extracted4(MerchantInfo merchantInfo, Map<String, String> param) {
		param.put("__appId", merchantInfo.getAppId()); // 商户id
		param.put("privateKey", merchantInfo.getPrivateKey());// 秘钥
		param.put("__privateKey", merchantInfo.getPrivateKey());// 秘钥
		param.put("__privateKey1", merchantInfo.getPrivateKey1());// 秘钥
		param.put("__privateKey2", merchantInfo.getPrivateKey2());// 秘钥
		param.put("__privateKey3", merchantInfo.getPrivateKey3());// 秘钥
		param.put("__privateKey4", merchantInfo.getPrivateKey4());// 秘钥
	}

	private <T> T findPayThird(String type, Class<T> cls) {
		if (StrUtil.isBlank(type)) {
			return null;
		}
//		Map<String, T> maps = applicationContext.getBeansOfType(cls);
//		return maps.get(type);
		return applicationContext.getBean(type, cls);
	}

	String endStep(Map<String, String> paramsMap, PayPlatformInfo payPlatformInfo, String orderId,
			PayConfigNotify payConfigInfo, MerchantInfo merchantInfo) {

		String successParam = payConfigInfo.getSuccessParam();
		String successCode = payConfigInfo.getSuccessCode();
		String failParam = payConfigInfo.getFailParam();
		String failCode = payConfigInfo.getFailCode();
		String successResult = payConfigInfo.getSuccessResult();
		String otherOrderIdParam = payConfigInfo.getOtherOrderIdParam();
		String amountParam = payConfigInfo.getAmountParam();
		String settleAmountParam = payConfigInfo.getSettleAmountParam();
		String unit = payConfigInfo.getAmountUnit();
		String refundParam = payConfigInfo.getRefundParam();
		String refundCode = payConfigInfo.getRefundCode();// 6,7,8,9 格式,:三方或四方状态 6 待退款 7 退款中 8 退款成功 9 退款失败
		String upiParam = payConfigInfo.getUpiParam();

		// ---------
		// int payConfigId = payConfigInfo.getPayConfigId();

		// ---------
		String success = MapUtil.getStr(paramsMap, successParam, "");
		String fail = MapUtil.getStr(paramsMap, failParam, "");
		String amount = MapUtil.getStr(paramsMap, amountParam, "");
		String settleAmount = MapUtil.getStr(paramsMap, settleAmountParam, "");
		String otherOrderId = MapUtil.getStr(paramsMap, otherOrderIdParam, "");
		String refund = MapUtil.getStr(paramsMap, refundParam, "");
		String upi = MapUtil.getStr(paramsMap, upiParam, "");
		Integer platformId = payPlatformInfo.getPlatformId();
		OrderInfo order = getOrderInfoByOrderId(orderId);
		// "订单不存在" +
		validateParam(order == null, 1021, orderId);
		// "错误码:13:已经处理过" + orderId

		// 退款状态初始化
		String refundInit = "6";
		String refundRequested = "7";
		String refundSuccess = "8";
		String refundFail = "9";
		if (StrUtil.isNotEmpty(refundCode)) {
			String[] refundCodeArr = refundCode.split(",");
			refundInit = refundCodeArr[0];// 待退款
			refundRequested = refundCodeArr[1];// 退款中
			refundSuccess = refundCodeArr[2];// 退款成功
			refundFail = refundCodeArr[3];// 退款失败
		}

		if (StrUtil.isNotEmpty(successCode) && successCode.equals(success)) {
			validateParam(order.getStatus() == 1, 1022, orderId);// 重复提交上分验证
			validateParam(order.getStatus() == 2, 1022, orderId);// 明天danny帮忙修改一下
			validateParam(order.getStatus() == 8, 1025, orderId);// 退款完成，不允许上分提交

		} else if (StrUtil.isNotEmpty(refundCode) && refundSuccess.equals(refund)) {// 退款完成状态配置不为空的情况
			validateParam(order.getStatus() == 8, 1024, orderId);// 重复提交退款验证
		}

		// "错误码:15:非法访问::" + orderId

		// 只有2380 2583 2278 2644 2712 2578 2756不做平台id判断。 -1关闭,其他数值开启
		if (payConfigInfo.getPlatformCheck() != null && payConfigInfo.getPlatformCheck().intValue() != -1) {
			validateParam(order.getPlatformId().intValue() != platformId.intValue(), 1003, orderId + "_1");
		}

		if (payPlatformInfo.getAreaType() == 4 || payPlatformInfo.getAreaType() == 8
				|| payPlatformInfo.getAreaType() == 9) {// 印尼、巴西和巴基斯坦国家 处理最近2天的订单
			validate((System.currentTimeMillis() - 1000L * 60 * 60 * 48) > order.getCreateTime().getTime(),
					"印尼、巴西支付处理最近2天的订单" + orderId, 1023, orderId);
		} else if (payPlatformInfo.getAreaType() == 7) {// 墨西哥处理最近30天的订单
			validate((System.currentTimeMillis() - 1000L * 60 * 60 * 720) > order.getCreateTime().getTime(),
					"墨西哥支付处理最近30天的订单" + orderId, 1023, orderId);
		} else if (payPlatformInfo.getAreaType() == 2) {
			// 印度项目, 后面有一个挂单的控制
		} else if (payPlatformInfo.getAreaType() == 3 || payPlatformInfo.getAreaType() == 10
				|| payPlatformInfo.getAreaType() == 11 || payPlatformInfo.getAreaType() == 13) {// 越南 孟加拉 日本 马来西亚
																								// 处理最24小时的订单
			validate((System.currentTimeMillis() - 1000L * 60 * 60 * 24) > order.getCreateTime().getTime(),
					"日本、孟加拉支付处理最近24小时的订单" + orderId, 1023, orderId);
		} else {// 国内只能处理最近的订单
			validate((System.currentTimeMillis() - 1000L * 60 * 45) > order.getCreateTime().getTime(),
					"只能处理最近的订单" + orderId, 1023, orderId);
		}

		// 如果支付成功，回调无订单状态值，设置固定订单参数 noStatus，成功状态值200
		if (ObjectUtil.equal(successParam, "noStatus")) {
			success = "200";
		}

		BigDecimal realAmount = null;
		PayUnitEnum payUnitEnum = PayUnitEnum.valueOf(unit);
		if (StrUtil.isEmpty(amount)) {
			orderBuildErrorService.add(platformId, payConfigInfo.getPayConfigId(), 0L, "未获回调金额", orderId, "未获得金额订单",
					"");
//				realAmount = order.getReqAmount();
			return "fail";
		} else {
			realAmount = new BigDecimal(amount);
			if (payUnitEnum.compareTo(PayUnitEnum.fen) == 0) {
				realAmount = realAmount.divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
			} else if (payUnitEnum.compareTo(PayUnitEnum.li) == 0) {
				realAmount = realAmount.divide(BigDecimal.valueOf(1000), 3, BigDecimal.ROUND_HALF_UP);
			}
		}

		// 支付成功
		if (StrUtil.isNotEmpty(successCode) && successCode.equals(success) && order.getStatus() != 8) {// 退款完成状态，不允许上分操作
			Long id = order.getId();
			OrderInfo record = new OrderInfo();
			record.setId(id);
			record.setPayTime(new Date());
			record.setStatus(1);

			// 设置上浮2%参数
			BigDecimal maxTax = new BigDecimal("1.02");
			// 设置下浮50%参数
			BigDecimal minTax = new BigDecimal("0.5");
			// 限制回调金额开关功能amountCheck 100 验证/-1 不验证 letspay和巴基斯坦跑分3872不验证回调金额
			if (payConfigInfo.getAmountCheck() == null) {
				payConfigInfo.setAmountCheck(100);
			}
			if (payConfigInfo.getAmountCheck() == 100) {
				if (realAmount.compareTo(order.getReqAmount().multiply(maxTax)) > 0
						|| realAmount.compareTo(order.getReqAmount().multiply(minTax)) < 0) {// 下单请求金额和回调金额区间在上浮2%
					// 和下浮50%之间，其余返回失败
					orderBuildErrorService.add(platformId, payConfigInfo.getPayConfigId(), 0L, "回调金额",
							"请求金额:" + order.getReqAmount() + "回调金额：" + realAmount + "不在上浮2%和下浮50%区间内",
							"请求金额和回调不匹配," + orderId, "");
					log.info("请求金额:" + order.getReqAmount() + "回调金额：" + realAmount + "不在上浮2%和下浮50%区间内");
					return "fail";
				}
			} else if (payConfigInfo.getAmountCheck() == -1) {
				// 不限制回调金额
			} else {
				orderBuildErrorService.add(platformId, payConfigInfo.getPayConfigId(), 0L, "回调金额开关配置",
						"回调金额开关配置错误，只能100或-1", "回调金额开关配置错误", "");
				log.info("回调金额开关配置错误 只能100/-1,支付ID:{}", payConfigInfo.getPayConfigId());
				return "fail2";
			}

			record.setRealAmount(realAmount);
			record.setSettleAmount(realAmount);

			// 结算金额
			if (StrUtil.isNotBlank(settleAmount)) {
				record.setSettleAmount(new BigDecimal(settleAmount));
			}

			if (StrUtil.isNotBlank(otherOrderId)) {
				record.setOtherOrderId(otherOrderId);
			}
			record.setSyncStatus(1);

			int a = orderInfoService.updateByPrimaryKey(record);
			if (a > 0) {
				Long i = redisService.leftPush(PayConstant.pay_success_list, id.toString());
				log.info("successResult:orderId:{}, id:{}, a:{}, i:{}", orderId, id, a, i);
			}

			if ("JAVACLASS".equals(successResult)) {
				IPayNotifyResultHandle pnr = findPayThird(payConfigInfo.getType(), IPayNotifyResultHandle.class);
				if (pnr != null) {
					return pnr.result(PayResultType.success, paramsMap, merchantInfo);
				}
				return successResult;
			}

//

			return successResult;
		} else if (StrUtil.isNotEmpty(failCode) && failCode.equals(fail)) {
			Long id = order.getId();
			OrderInfo record = new OrderInfo();
			record.setId(id);
			record.setCreateStatus(100);

			int a = orderInfoService.updateByPrimaryKey(record);
			return successResult;

		} else if (StrUtil.isNotEmpty(refundCode) && StrUtil.isNotEmpty(refund)) {
			Long id = order.getId();
			OrderInfo record = new OrderInfo();
			record.setId(id);

			if (refundSuccess.equals(refund)) {
				record.setStatus(8);// 退款完成状态
			} else if (refundInit.equals(refund)) {
				record.setStatus(6);// 待退款 状态
			} else if (refundRequested.equals(refund)) {
				record.setStatus(7);// 退款中状态
			} else if (refundFail.equals(refund)) {
				record.setStatus(9);// 退款失败状态
			}

			record.setPayTime(new Date());// 款操作时间
			record.setRealAmount(realAmount);

			int a = orderInfoService.updateByPrimaryKey(record);
			if (a > 0) {
				Long i = redisService.leftPush(PayConstant.pay_success_list, id.toString());
				log.info("successResult:orderId:{}, id:{}, a:{}, i:{}", orderId, id, a, i);
			}
			return successResult;
		}

		return "fail";
	}

}
