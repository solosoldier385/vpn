package com.letsvpn.pay.dto;



import com.letsvpn.pay.entity.MerchantInfo;
import com.letsvpn.pay.entity.OrderInfo;
import com.letsvpn.pay.entity.PayConfigInfo;
import com.letsvpn.pay.vo.PayResultData;

import java.util.Map;

//创建订单
public interface IPayCreateOrderRequest {

	PayResultData createOrder(MerchantInfo merchantInfo, PayConfigInfo payConfigInfo, Map<String, Object> thirdParam,
							  OrderInfo order);
}
