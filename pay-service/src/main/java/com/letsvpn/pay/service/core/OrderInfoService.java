package com.letsvpn.pay.service.core;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.pay.entity.OrderInfo;
import com.letsvpn.pay.mapper.OrderInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderInfoService {
	@Autowired
	OrderInfoMapper orderInfoMapper;


	public OrderInfo getOrderInfo(Long id) {
		return orderInfoMapper.selectById(id);
	}

	public OrderInfo getOrderInfo(String orderId) {
		QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("order_id", orderId);
		return orderInfoMapper.selectOne(queryWrapper);
	}
	public OrderInfo getOrderInfo(String frontId, int platformId) {
		QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("front_id", frontId);
		queryWrapper.eq("platform_id", platformId);
		return orderInfoMapper.selectOne(queryWrapper);
	}

	public int updateByPrimaryKey(OrderInfo record) {

		return orderInfoMapper.updateById(record);
	}



}
