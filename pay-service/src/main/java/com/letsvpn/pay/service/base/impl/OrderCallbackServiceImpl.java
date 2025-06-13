package com.letsvpn.pay.service.base.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.letsvpn.pay.entity.OrderCallback;
import com.letsvpn.pay.mapper.OrderCallbackMapper;
import com.letsvpn.pay.service.base.IOrderCallbackService;
import org.springframework.stereotype.Service;

@Service
public class OrderCallbackServiceImpl extends ServiceImpl<OrderCallbackMapper, OrderCallback> implements IOrderCallbackService {

}
