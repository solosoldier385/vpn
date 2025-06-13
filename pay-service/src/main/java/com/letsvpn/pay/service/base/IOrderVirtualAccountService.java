package com.letsvpn.pay.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.letsvpn.pay.entity.OrderVirtualAccount;

import java.util.List;

/**
 * <p>
 * 订单相关的虚拟账户信息表 服务类
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
public interface IOrderVirtualAccountService extends IService<OrderVirtualAccount> {
    OrderVirtualAccount getByVirtualAccountNumber(String accountNumber);
    List<OrderVirtualAccount> getByUserId(Integer userId);
}