package com.letsvpn.pay.service.base.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.letsvpn.pay.entity.OrderBuildError;
import com.letsvpn.pay.mapper.OrderBuildErrorMapper;
import com.letsvpn.pay.service.base.IOrderBuildErrorService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单构建/处理错误记录表 服务实现类
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Service
public class OrderBuildErrorServiceImpl extends ServiceImpl<OrderBuildErrorMapper, OrderBuildError> implements IOrderBuildErrorService {

}