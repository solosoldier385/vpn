package com.letsvpn.pay.util;

import java.util.HashMap;
import java.util.Map;

public interface PayProjectType {

	Map<Integer, String> OrderInfoCreateStauts = new HashMap<Integer, String>() {
		{
			put(0, "未支付");
			// put(0, "新单");
			put(100, "已支付");
			

		}
	};
	Map<Integer, String> OrderInfoCreateCreateStatus = new HashMap<Integer, String>() {
		{
			put(0, "新单");
			put(100, "同步订单");
			put(-1, "不存在");
		}
	};

	//数据保存天数
	Map<String, String> OrderInfoDataDay= new HashMap<String, String>() {
		{
			put("001", "近期");
			put("002", "5天-15天");
			put("003", "15天-30天");
			put("004", "30天-45天");
			put("005", "45天-67天");
			put("006", "67天后");
		}
	};
	//数据保存天数
	Map<String, String> OrderInfoDataDaycn= new HashMap<String, String>() {
		{
			put("001", "近期");
			put("002", "5天-15天");
		 
		}
	};

	public static String translation(Integer type, Map<Integer, String> data) {
		return data.get(type) == null ? type.toString() : data.get(type);
	}
}
