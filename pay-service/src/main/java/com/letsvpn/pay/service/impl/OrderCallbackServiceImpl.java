package com.letsvpn.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.letsvpn.pay.entity.OrderBuildError;
import com.letsvpn.pay.entity.OrderCallback;
import com.letsvpn.pay.mapper.OrderBuildErrorMapper;
import com.letsvpn.pay.mapper.OrderCallbackMapper;
import com.letsvpn.pay.service.IOrderBuildErrorService;
import com.letsvpn.pay.service.IOrderCallbackService;
import org.springframework.stereotype.Service;

@Service
public class OrderCallbackServiceImpl extends ServiceImpl<OrderCallbackMapper, OrderCallback> implements IOrderCallbackService {

}
