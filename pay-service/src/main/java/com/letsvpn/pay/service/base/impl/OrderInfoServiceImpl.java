package com.letsvpn.pay.service.base.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.pay.entity.OrderInfo;
import com.letsvpn.pay.mapper.OrderInfoMapper;
import com.letsvpn.pay.service.base.IOrderInfoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单信息表 服务实现类
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements IOrderInfoService {
    @Override
    public OrderInfo getByOrderId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return null;
        }
        return getOne(new QueryWrapper<OrderInfo>().eq("order_id", orderId));
    }
}