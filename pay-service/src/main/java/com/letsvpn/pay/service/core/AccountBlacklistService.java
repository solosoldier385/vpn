package com.letsvpn.pay.service.core;

import com.letsvpn.pay.mapper.PayIpWhiteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Aiden
 */
@Service
public class AccountBlacklistService {

	@Autowired
	RedisService redisService;

	@Autowired
	PayIpWhiteMapper extPayIpWhiteMapper;

	// 判断是否存在这个值
	public Boolean isMember(String key, String value) {
		if(value==null) {
			return false;
		}
		return redisService.isMember(key, value.toLowerCase());
	}



}
