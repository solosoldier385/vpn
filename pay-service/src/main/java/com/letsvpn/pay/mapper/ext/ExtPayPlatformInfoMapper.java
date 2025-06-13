package com.letsvpn.pay.mapper.ext;


import com.letsvpn.pay.entity.MerchantInfo;
import com.letsvpn.pay.entity.PayConfigInfo;
import com.letsvpn.pay.entity.PayPlatformInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ExtPayPlatformInfoMapper {

	@Select("select * from pay_platform_info where platform_no=#{platformNo}")
	PayPlatformInfo getPayPlatformInfo(@Param("platformNo") String platformNo);

	@Select("select * from pay_platform_info where platform_id=#{platformId}")
	PayPlatformInfo getPayPlatformInfoId(@Param("platformId") int platformId);
	
	@Select("select * from pay_config_info where id=#{id}")
	PayConfigInfo getPayConfigInfo(@Param("id") int id);

	@Select("select * from merchant_info where pay_config_id=#{payConfigId} and  platform_id=#{platformId}")
	MerchantInfo getMerchantInfo(@Param("payConfigId") int payConfigId, @Param("platformId") int platformId);
}
