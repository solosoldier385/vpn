package com.letsvpn.pay.mapper.ext;

import com.letsvpn.pay.entity.OrderInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ExtOrderInfoMapper {

	@Select("select * from order_info where order_id=#{orderId} limit 1")
	OrderInfo getOrderInfo(@Param("orderId") String orderId);

	@Select("select * from order_info where platform_id=#{platformId} and front_id=#{frontId} limit 1")
	OrderInfo getOrderInfoByFrontId(@Param("frontId") String frontId, @Param("platformId") int platformId);
}
