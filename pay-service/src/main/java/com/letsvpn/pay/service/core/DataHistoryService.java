package com.letsvpn.pay.service.core;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;

import com.letsvpn.pay.mapper.ext.ExtDataHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class DataHistoryService {

	@Autowired
	ExtDataHistoryMapper extDataHistoryMapper;
//	@Autowired
//	RedisService redisService;


	@Value("${wanli.data.day:5}")
	int dataDay;

	@Scheduled(cron = "0 15 5 * * ?")
	public void dataHistory() {

		String key = "payproject_DataHistoryService";

		Date now1 = new Date();

		try {
			List<String> list = new ArrayList<>();
			// 迁移到2里面去

			extractedOrderCallback(now1, list);
			// -----------------------------------------------------------

			list.add("DELETE from OrderReqRecord where createTime<DATE_SUB(CURDATE(),INTERVAL 1 DAY)");
			list.add("DELETE from OrderNotifyRecord where createTime<DATE_SUB(CURDATE(),INTERVAL 2 DAY)");
			// -----------------------------------------------------------
			// 迁移到2里面去
			// -----------------------------------------------------------
			int day = dataDay;
			if (day < 5) {
				day = 5;
			}
			String lei = "id, orderId, platformId, frontId, payConfigId, channelId, userId, gameId, status, reqAmount, realAmount, payTime, createTime, createStatus, createIp, noticeStatus, noticeTime, payConfigChannelId, otherOrderId, onLineId, remark,extend1,extend2,extend3,syncStatus,settleAmount,upi";
			list.add("INSERT into OrderInfo002 (" + lei + ") select " + lei
					+ "  from OrderInfo001 where createTime<DATE_SUB(CURDATE(),INTERVAL " + day + " DAY)");
			list.add("DELETE from OrderInfo001 where createTime<DATE_SUB(CURDATE(),INTERVAL " + day + " DAY)");

			// ------------------------------------
			list.add("DELETE from OrderBuildError where createTime<DATE_SUB(CURDATE(),INTERVAL 5 DAY)");
			list.add("");
			// ------------------------------------
			// 迁移到003去
			list.add("INSERT into OrderInfo003 (" + lei + ") select " + lei
					+ "  from OrderInfo002 where createTime<DATE_SUB(CURDATE(),INTERVAL " + (day + 10) + " DAY)");
			list.add("DELETE from OrderInfo002 where createTime<DATE_SUB(CURDATE(),INTERVAL " + (day + 10) + " DAY)");

			list.add("");

			list.add("INSERT into OrderInfo004 (" + lei + ") select " + lei
					+ "  from OrderInfo003 where createTime<DATE_SUB(CURDATE(),INTERVAL " + (day + 25) + " DAY)");
			list.add("DELETE from OrderInfo003 where createTime<DATE_SUB(CURDATE(),INTERVAL " + (day + 25) + " DAY)");

			list.add("");
			list.add("INSERT into OrderInfo005 (" + lei + ") select " + lei
					+ "  from OrderInfo004 where createTime<DATE_SUB(CURDATE(),INTERVAL " + (day + 40) + " DAY)");
			list.add("DELETE from OrderInfo004 where createTime<DATE_SUB(CURDATE(),INTERVAL " + (day + 40) + " DAY)");

			list.add("");
			if (DateUtil.dayOfMonth(now1) == 1) {
				list.add("create table  if not exists OrderInfo006  like OrderInfo005;");
				list.add(String.format("RENAME TABLE OrderInfo006 TO OrderInfo006_%s;",
						DateUtil.format(now1, DatePattern.PURE_DATE_PATTERN)));
				list.add("create table  if not exists OrderInfo006  like OrderInfo005;");

			}

			list.add("");

			list.add("INSERT into OrderInfo006 (" + lei + ") select " + lei
					+ "  from OrderInfo005 where createTime<DATE_SUB(CURDATE(),INTERVAL " + (day + 62) + " DAY)");
			list.add("DELETE from OrderInfo005 where createTime<DATE_SUB(CURDATE(),INTERVAL " + (day + 62) + " DAY)");

			// 67天没有支付的就情况
			list.add("DELETE from OrderInfo005 where status=0");
			// list.add("DELETE from OrderInfo006 where status=0");
			// -----------------------------------------------------------

			// -----------------------------------------------------------
			list.add("DELETE from  ReportDay where dday = date_sub(curdate(),interval 1 day)");
			list.add(
					"INSERT INTO ReportDay (dday, areaType, countNum, amount, createTime) SELECT date(a.createTime) AS dday, b.areaType, count(*) AS count, sum(a.realAmount) amount, now() FROM OrderInfo001 a, PayPlatformInfo b WHERE a.platformId = b.platformId AND a. STATUS = 1 AND a.platformId NOT IN (101, 282) AND date(a.createTime) = date_sub(curdate(), INTERVAL 1 DAY) GROUP BY dday, b.areaType");
			// -----------------------------------------------------------
			list.add("DELETE from  ReportPlatformDay where dday = date_sub(curdate(),interval 1 day)");
			list.add(
					"INSERT INTO ReportPlatformDay (platformId,dday, countNum, amount,createTime) SELECT platformId,date(createTime) AS dday, count(*) AS count, sum(realAmount) amount,now() FROM OrderInfo001 WHERE STATUS = 1 AND date(createTime) = date_sub(curdate(), INTERVAL 1 DAY) GROUP BY platformId,dday");
			// -----------------------------------------------------------
			list.add("DELETE from  OrderEmailPollRecord where createTime <DATE_SUB(CURDATE(),INTERVAL 4 DAY)");
			list.add("DELETE from  ReportPayConfigDay where dday = date_sub(curdate(),interval 1 day)");
			list.add(
					"INSERT INTO ReportPayConfigDay (payConfigId,dday, countNum, amount,createTime, status) SELECT payConfigId,date(createTime) AS dday, count(*) AS count, sum(realAmount) amount,now(), status FROM OrderInfo001 WHERE date(createTime) = date_sub(curdate(), INTERVAL 1 DAY) GROUP BY payConfigId, dday, status");

			for (String sql : list) {
				if (StrUtil.isBlank(sql)) {
					ThreadUtil.sleep(90 * 1001);
					continue;
				}
				try {
					Long now = System.currentTimeMillis();
					int a = extDataHistoryMapper.updateSql(sql);
					now = System.currentTimeMillis() - now;
					log.info("dataHistory:now: {} , sql:{} , a:{}", now, sql, a);
					ThreadUtil.sleep(70 * 1000 + now);
				} catch (Exception e) {

					log.error("{} , {}", sql, e.getMessage(), e);
					throw e;
				}
			}
		} finally {
//			redisService.unLock(key);
		}

	}

	private void extractedOrderCallback(Date now, List<String> list) {
		String lei1 = "id,type,platformNo,payConfigId,reqUrl,createTime,createIp,param,status";

		DateTime daym = DateUtil.offsetDay(now, -1);

		int a = daym.getField(Calendar.DAY_OF_MONTH);

		String day5 = daym.toString("yyyyMM") + (a < 15 ? "01" : "15");

		String tableName = "OrderCallback" + day5;
		list.add("create table  if not EXISTS " + tableName + " like OrderCallback001");

		list.add("INSERT into " + tableName + " (" + lei1 + ") select " + lei1
				+ "  from  OrderCallback001 where createTime<DATE_SUB(CURDATE(),INTERVAL 0 DAY)");
		list.add("DELETE from OrderCallback001 where createTime<DATE_SUB(CURDATE(),INTERVAL 0 DAY)");

//			list.add("INSERT into OrderCallback003 (" + lei1 + ") select " + lei1
//					+ "  from  OrderCallback002 where createTime<DATE_SUB(CURDATE(),INTERVAL 5 DAY)");
//			list.add("DELETE from OrderCallback002 where createTime<DATE_SUB(CURDATE(),INTERVAL 5 DAY)");
//
//			list.add("INSERT into OrderCallback004 (" + lei1 + ") select " + lei1
//					+ "  from  OrderCallback003 where createTime<DATE_SUB(CURDATE(),INTERVAL 9 DAY)");
//			list.add("DELETE from OrderCallback003 where createTime<DATE_SUB(CURDATE(),INTERVAL 9 DAY)");
//
//			list.add("INSERT into OrderCallback005 (" + lei1 + ") select " + lei1
//					+ "  from  OrderCallback004 where createTime<DATE_SUB(CURDATE(),INTERVAL 15 DAY)");
//			list.add("DELETE from OrderCallback004 where createTime<DATE_SUB(CURDATE(),INTERVAL 15 DAY)");
//
//			list.add("INSERT into OrderCallback006 (" + lei1 + ") select " + lei1
//					+ "  from  OrderCallback005 where createTime<DATE_SUB(CURDATE(),INTERVAL 20 DAY)");
//			list.add("DELETE from OrderCallback005 where createTime<DATE_SUB(CURDATE(),INTERVAL 20 DAY)");
//
//			// 备份数据只保存67天
//			list.add("DELETE from OrderCallback006 where createTime<DATE_SUB(CURDATE(),INTERVAL 50 DAY)");
		// -----------------------------------------------------------
	}

}
