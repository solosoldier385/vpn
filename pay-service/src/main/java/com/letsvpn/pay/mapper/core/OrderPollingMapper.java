package com.letsvpn.pay.mapper.core;


import com.letsvpn.pay.entity.OrderInfo;
import com.letsvpn.pay.vo.PayConfigPolling;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface OrderPollingMapper {

	@Select("select * from PayConfigPolling where nullity=0")
	List<PayConfigPolling> listPayConfigPolling();

	@Update("UPDATE PayConfigPolling SET beginExtTime=#{beginTime} WHERE (payConfigId=#{payConfigId})")
	int beginExtTimePayConfigPolling(@Param("payConfigId") int payConfigId,@Param("beginTime") Date beginTime);

	@Update("UPDATE PayConfigPolling SET endExtTime=now() WHERE (payConfigId=#{payConfigId})")
	int endExtTimePayConfigPolling(@Param("payConfigId") int payConfigId);

	@Select("SELECT a.id FROM OrderInfo001 a LEFT JOIN OrderPollingRecord b ON a.id = b.id  WHERE a.STATUS = 0 and a.payConfigId=${payConfigId} AND b.id IS NULL AND a.createTime < DATE_SUB(now(), INTERVAL 20 MINUTE)")
	List<Long> notSuccess(@Param("payConfigId") int payConfigId);

	
	@Select("select id from OrderInfo001  where payConfigId=${payConfigId} and status=0 and createTime < #{beginTime} and createTime >= #{endTime}")
	List<String> notOrderSuccess(@Param("payConfigId") int payConfigId, @Param("beginTime") Date beginTime,
			@Param("endTime") Date endTime);

	@Select("SELECT id,pollingTimes FROM OrderPollingRecord  where payConfigId=${payConfigId} and nextTime<now() and status=0 limit 1000")
	List<Map<String, Object>> notOverPolling(@Param("payConfigId") int payConfigId);

	@Select("SELECT * FROM OrderInfo001  WHERE id=#{id}")
	OrderInfo getOrderInfo(@Param("id") long id);

	@Insert("INSERT INTO OrderPollingRecord (id, payConfigId, nextTime, pollingTimes, createTime, status,statusTitle, resultBody) VALUES (#{id}, #{payConfigId}, #{nextTime}, #{pollingTimes}, #{createTime}, #{status},#{statusTitle}, #{resultBody})")
	int addOrderPollingRecord(@Param("id") long id, @Param("payConfigId") int payConfigId,
			@Param("nextTime") Date nextTime, @Param("pollingTimes") int pollingTimes,
			@Param("createTime") Date createTime, @Param("status") int status, @Param("statusTitle") String statusTitle,
			@Param("resultBody") String resultBody);

//	@Insert("UPDATE OrderPollingRecord SET status=#{status},statusTitle=#{statusTitle} WHERE id=#{id}")
//	int updateOrderPollingRecord(@Param("id") long id, @Param("status") int status,
//			@Param("statusTitle") String statusTitle);

	@Insert("UPDATE OrderPollingRecord SET pollingTimes=#{pollingTimes},statusTitle=#{statusTitle} ,nextTime=#{nextTime} ,status=#{status} ,resultBody=#{resultBody} WHERE id=#{id}")
	int updateOrderPollingRecord(@Param("id") long id, @Param("pollingTimes") int pollingTimes,
			@Param("nextTime") Date nextTime, @Param("status") int status, @Param("statusTitle") String statusTitle,
			@Param("resultBody") String resultBody);

	@Insert("DELETE FROM OrderPollingRecord WHERE id=#{id}")
	int deleteOrderPollingRecord(@Param("id") long id);

}
