package com.letsvpn.pay.mapper.ext;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

public interface ExtOrderVirtualAccountMapper {
	@Select("SELECT userId,name,email,mobile FROM OrderVirtualAccount WHERE platformId =#{platformId} AND userId=#{userId} limit 1")
	Map<String, Object> getOrderVirtualAccount1(@Param("platformId") int platformId, @Param("userId") int userId);

}
