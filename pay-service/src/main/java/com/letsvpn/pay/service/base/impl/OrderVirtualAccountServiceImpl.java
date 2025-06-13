package com.letsvpn.pay.service.base.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.pay.entity.OrderVirtualAccount;
import com.letsvpn.pay.mapper.OrderVirtualAccountMapper;
import com.letsvpn.pay.service.base.IOrderVirtualAccountService;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * <p>
 * 订单相关的虚拟账户信息表 服务实现类
 * </p>
 *
 * @author (Your Name or Generator)
 * @since (Date)
 */
@Service
public class OrderVirtualAccountServiceImpl extends ServiceImpl<OrderVirtualAccountMapper, OrderVirtualAccount> implements IOrderVirtualAccountService {

    @Override
    public OrderVirtualAccount getByVirtualAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return null;
        }
        return getOne(new QueryWrapper<OrderVirtualAccount>().eq("virtual_account_number", accountNumber));
    }

    @Override
    public List<OrderVirtualAccount> getByUserId(Integer userId) {
        if (userId == null) {
            return null; // Or an empty list
        }
        return list(new QueryWrapper<OrderVirtualAccount>().eq("user_id", userId));
    }
}