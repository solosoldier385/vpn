package com.letsvpn.pay.dto;


import com.letsvpn.pay.entity.MerchantInfo;
import com.letsvpn.pay.entity.OrderInfo;
import com.letsvpn.pay.entity.PayConfigInfo;
import lombok.Data;

public interface IOrderQueryRequest {

	public OrderQueryResult exeOrderQueryReq(OrderInfo info, MerchantInfo merchant, PayConfigInfo payConfigInfo)
			throws Exception;

	@Data
	public class OrderQueryResult {

		OrderQueryResultEnum status;
		String resultText;
		String utr;
	}
}
