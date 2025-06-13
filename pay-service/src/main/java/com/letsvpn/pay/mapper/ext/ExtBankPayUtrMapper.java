package com.letsvpn.pay.mapper.ext;


import com.letsvpn.pay.entity.PayPlatformInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface ExtBankPayUtrMapper {

	@Insert("INSERT INTO BankPayPushUtr (userId, platformId, gameId, payConfigId, utr, status, type, createTime, remark, nextExtTime, timeInterval) VALUES "
			+ "(#{userId}, #{platformId}, #{gameId}, #{payConfigId}, #{utr}, #{status}, #{type}, #{createTime}, #{remark}, #{nextExtTime}, #{timeInterval});")
	@SelectKey(keyProperty = "id", resultType = Long.class, before = false, statement = "SELECT LAST_INSERT_ID()")
	int addBankPayPushUtr(Map<String, Object> body);

	@Select({ "<script>", "SELECT a.id, a.userId, a.platformId, a.gameId, a.payConfigId, a.utr, a.status, a.createTime, a.remark FROM  BankPayPushUtr a",
			"<where> ",
			"<if test='paramsMap.utr != null and paramsMap.utr != \"\" ' > and a.utr=#{paramsMap.utr}</if>",
			"<if test='paramsMap.platformId != null and paramsMap.platformId != \"\"  and paramsMap.platformId != \"0\" ' > and a.platformId=#{paramsMap.platformId}</if>",
			"<if test='paramsMap.status != null and paramsMap.status != \"\" ' > and a.status=#{paramsMap.status}</if>",
			"<if test='paramsMap.beginTime != null and paramsMap.beginTime != \"\" ' > and a.createTime &gt;= #{paramsMap.beginTime}</if>",
			"<if test='paramsMap.endTime != null and paramsMap.endTime != \"\" ' > and a.createTime &lt;= #{paramsMap.endTime}</if>",
			"</where>", "order by a.id desc,a.status asc limit #{paramsMap.start}, #{paramsMap.size}", "</script>" })
	List<Map<String, Object>> searchBankPayListByParams(@Param("paramsMap") Map<String, Object> body);

	@Select({ "<script>", "SELECT count(id) FROM  BankPayPushUtr", "<where> ",
			"<if test='paramsMap.utr != null and paramsMap.utr != \"\" ' > and utr=#{paramsMap.utr}</if>",
			"<if test='paramsMap.platformId != null and paramsMap.platformId != \"\"  and paramsMap.platformId != \"0\" ' > and platformId=#{paramsMap.platformId}</if>",
			"<if test='paramsMap.status != null and paramsMap.status != \"\" ' > and status=#{paramsMap.status}</if>",
			"<if test='paramsMap.beginTime != null and paramsMap.beginTime != \"\" ' > and createTime &gt;= #{paramsMap.beginTime}</if>",
			"<if test='paramsMap.endTime != null and paramsMap.endTime != \"\" ' > and createTime &lt;= #{paramsMap.endTime}</if>",
			"</where>", "</script>" })
	int searchCountBankPayListByParams(@Param("paramsMap") Map<String, Object> body);

	@Select("select * from BankPayPushUtr where  id = #{id}")
	Map<String, Object> searchBankPayById(@Param("id") Long id);

	@Update("UPDATE BankPayPushUtr SET  refId = #{refId}, status = #{status}, remark = #{remark} WHERE id = #{id}")
	int updateBankPayOrder(Map<String, Object> body);

	@Select("select * from BankPayPushUtr where  utr = #{utr} limit 1")
	Map<String, Object> searchBankPayByUtr(@Param("utr") String utr);

	@Select("select * from BankPayPushUtr where status=0")
	List<Map<String, Object>> searchBankPayListByNoSuccess();

	@Select("select * from OrderVirtualAccount where  gameId=#{paramsMap.gameId} and platformId=#{paramsMap.platformId} limit 1")
	Map<String, Object> searchUserInfoPayByParams(@Param("paramsMap") Map<String, Object> body);

	@Select({ "<script>", "select * from BankPayPushUtr ", "<where> ",
			"<if test='paramsMap.idList != null and paramsMap.idList.size() > 0 ' > ", "and id in ",
			"<foreach collection=\"paramsMap.idList\" item=\"id\" index=\"index\" open=\"(\" close=\")\" separator=\",\">",
			"#{id} ", "</foreach>", "</if>",
			"<if test='paramsMap.status != null and paramsMap.status != \"\" ' > and status=#{paramsMap.status}</if>",
			"</where>", "</script>" })
	List<Map<String, Object>> searchBankPayNoSuccessByIds(@Param("paramsMap") Map<String, Object> body);

	@Select("select * from BankPayPushUtr where  status=0  and DATE_SUB(NOW(), INTERVAL 7 DAY) <= createTime and  DATE_SUB(NOW(), INTERVAL 1 DAY) >= createTime and timeInterval!=1280")
	List<Map<String, Object>> searchBankPayListByMorethanToday();

	@Select("select * from BankPayPushUtr where  status= 0 and NOW() >= nextExtTime and createTime >= (NOW() - INTERVAL 12 HOUR) and timeInterval<640")
	List<Map<String, Object>> searchBankPayListByToday();

	@Update("UPDATE BankPayPushUtr SET  nextExtTime = #{nextExtTime}, timeInterval = #{timeInterval} WHERE id = #{id}")
	int updateBankPayTask(Map<String, Object> body);

	@Select("select * from PayPlatformInfo where  platformNo = #{pf} limit 1")
	PayPlatformInfo selectPlatformInfoByPF(@Param("pf") String pf);

	@Insert("INSERT INTO OrderClientPushUtr (utr, platformId, gameId, userId,  status, createTime, remark) VALUES "
			+ "(#{utr}, #{platformId}, #{gameId}, #{userId}, #{status}, #{createTime}, #{remark});")
	int addClientPushUtr(Map<String, Object> body);

	@Select("select count(*) from OrderClientPushUtr where utr=#{utr}")
	long countOrderClientPushUtr(@Param("utr") String utr);

	@Select("select * from OrderClientPushUtr where status=0 and LENGTH(utr)=12")
	List<Map<String, Object>> searchOrderClientListByNoSuccess();

	@Update("UPDATE OrderClientPushUtr SET status = #{status}, remark = #{remark} WHERE utr = #{utr}")
	int updateOrderClientPushUtr(Map<String, Object> body);

	@Insert("INSERT INTO  OrderPayPushPan (id) SELECT id FROM  OrderInfo001  where status=1 and createStatus=0")
	int manualOrderPayPushPan();

	@Delete("DELETE FROM BankPayPushUtr where utr=#{utr}")
	int deleteBankPayOrder(@Param("utr") String utr);

}
