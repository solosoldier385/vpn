package com.letsvpn.pay.service.core;

import cn.hutool.json.JSONObject;

import com.letsvpn.pay.entity.OrderVirtualAccount;
import com.letsvpn.pay.entity.PayPlatformInfo;
import com.letsvpn.pay.mapper.OrderVirtualAccountMapper;
import com.letsvpn.pay.mapper.ext.ExtOrderVirtualAccountMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.hutool.core.map.MapUtil;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderVirtualAccountService extends BaseService {

	@Autowired
	OrderVirtualAccountMapper orderVirtualAccountMapper;

	@Autowired
	ExtOrderVirtualAccountMapper extOrderVirtualAccountMapper;



	public JSONObject getUserKYC(String ip, PayPlatformInfo info, Map<String, String> body) {
		Integer userId = MapUtil.getInt(body, "userId");
		Integer platformId = info.getPlatformId();
		Map<String, Object> map = extOrderVirtualAccountMapper.getOrderVirtualAccount1(platformId, userId);
		if (map == null) {
			return result(404, "不存在!" + userId, null);
		}
		return resultData(map);

	}

	public JSONObject updateUserKYC(String ip, PayPlatformInfo info, Map<String, String> body) {
		Integer userId = MapUtil.getInt(body, "userId");
		Integer gameId = MapUtil.getInt(body, "gameId");
		String name = MapUtil.getStr(body, "name");
		String email = MapUtil.getStr(body, "email");
		String mobile = MapUtil.getStr(body, "mobile");
		Integer platformId = info.getPlatformId();
		Map<String, Object> map = extOrderVirtualAccountMapper.getOrderVirtualAccount1(platformId, userId);
		if (map == null) {
			OrderVirtualAccount orderVirtualAccount = new OrderVirtualAccount();
			orderVirtualAccount.setUserId(userId);
			orderVirtualAccount.setPlatformId(platformId);
			orderVirtualAccount.setGameId(gameId);
			orderVirtualAccount.setPayConfigId(0);
			orderVirtualAccount.setName(name);
			orderVirtualAccount.setPrimaryContact(name);
			orderVirtualAccount.setContactType("Customer");
			orderVirtualAccount.setEmail(email);
			orderVirtualAccount.setMobile(mobile);
			orderVirtualAccount.setCreateTime(new Date());
			orderVirtualAccount.setUpdateTime(new Date());
			orderVirtualAccountMapper.insert(orderVirtualAccount);
			map = new HashMap<>();
			map.put("name", name);
			map.put("email", email);
			map.put("mobile", mobile);
		}

		return resultData(map);
	}
}
