package com.letsvpn.pay.service.core;


import com.letsvpn.pay.util.PayConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PayPush2PanService extends BaseService {

	@Autowired
	PayPushPanService payPushPanService;
	@Autowired
	RedisService redisService;

	@Scheduled(fixedDelay = 3000, initialDelay = 10 * 1000)
	public void payPushOrder() {
		String orderId = redisService.rightPop(PayConstant.pay_success_list);
		while (orderId != null) {
			try {
				payPushPanService.pushSuccessOrderInfo(Long.valueOf(orderId));
//				redisService.removeList(PayConstant.pay_success_list_bak, orderId);
			} catch (Exception e) {
				log.error("orderId:{},{}", orderId, e.getMessage(), e);
			}
			// log.info("orderId:{}", orderId);
			orderId = redisService.rightPop(PayConstant.pay_success_list);
//			orderId = redisService.rightPop(PayConstant.pay_success_list, PayConstant.pay_success_list_bak);
		}
	}

}
