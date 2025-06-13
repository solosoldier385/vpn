package com.letsvpn.pay.mapper.ext;


import com.letsvpn.pay.entity.PayConfigChannel;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ExtSearchOrderMapper {

	List<Map<String, Object>> search(Map<String, String> body);

	List<Map<String, Object>> searchTotal(Map<String, String> body);

	// 作废2020-1-9 aiden
	List<Map<String, Object>> searchPayConfigInfo(Map<String, String> body);

	List<Map<String, Object>> searchOrderBuildError(Map<String, String> body);

	List<Map<String, Object>> listPayConfigInfo(Map<String, String> body);

	List<PayConfigChannel> listPayConfigChannel(@Param("payConfigIds") List<Integer> payConfigIds);
	
//	List<PayConfigIp> listPayConfigIp(@Param("payConfigIds") List<Integer> payConfigIds);
//
	List<Map<String, Object>> searchOrderInfosByParams(Map<String, Object> body);

	List<Map<String, Object>> searchOrderInfosByPayConfigId(Map<String, Object> body);
}
