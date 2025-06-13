package com.letsvpn.pay.dto;




import com.letsvpn.pay.entity.MerchantInfo;
import com.letsvpn.pay.entity.PayConfigChannel;
import com.letsvpn.pay.entity.PayConfigInfo;

import java.util.Map;

/**
 * 使用场景
 * bankopen 提交数据验证
 */
public interface IPayBankOpenCheckHandle {

    /**
     * 数据验证
     * @param data
     * @param payConfigInfo
     * @param merchantInfo
     * @param payConfigChannel
     * @return
     */
    String checkData(Map<String,String> data, PayConfigInfo payConfigInfo, MerchantInfo merchantInfo, PayConfigChannel payConfigChannel);
}
