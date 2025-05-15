package com.letsvpn.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.letsvpn.pay.entity.OrderInfo;

/**
 * <p>
 * 订单信息表 服务类
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
public interface IOrderInfoService extends IService<OrderInfo> {
    OrderInfo getByOrderId(String orderId);
}