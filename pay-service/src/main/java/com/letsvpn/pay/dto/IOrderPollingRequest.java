package com.letsvpn.pay.dto;


import com.letsvpn.pay.entity.OrderInfo;
import com.letsvpn.pay.vo.OrderPollingResult;
import com.letsvpn.pay.vo.PayConfigPolling;

public interface IOrderPollingRequest {

	public OrderPollingResult exeOrderPollingReq(OrderInfo info, PayConfigPolling payConfigPolling);

}
