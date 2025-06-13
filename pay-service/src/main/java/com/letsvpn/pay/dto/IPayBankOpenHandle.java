package com.letsvpn.pay.dto;



import com.letsvpn.pay.entity.MerchantInfo;
import com.letsvpn.pay.entity.PayConfigChannel;
import com.letsvpn.pay.entity.PayConfigInfo;

import java.util.Map;

/**
 * 使用场景
 * 1.bankopen虚拟开户处理
 * 2.onewallet网银收银台处理
 */
public interface IPayBankOpenHandle {


    /**
     * 新增开户处理接口
     * @param param
     * @param payConfigInfo
     * @param merchantInfo
     * @return
     */
    public String createAccount(Map<String, String> param, PayConfigInfo payConfigInfo, PayConfigChannel payConfigChannel, MerchantInfo merchantInfo);
}
