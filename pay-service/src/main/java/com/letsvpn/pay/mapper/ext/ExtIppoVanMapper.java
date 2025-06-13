package com.letsvpn.pay.mapper.ext;

import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ExtIppoVanMapper {

	@Insert("INSERT INTO IppoVanAccount (ippoName, ippoLock, account, ifsc, account_name, status, van_id, van_type, createAt, updateLock) VALUES "
			+ "(#{ippoName}, #{ippoLock}, #{account}, #{ifsc}, #{account_name}, #{status}, #{van_id}, #{van_type}, #{createAt}, #{updateLock});")
	@SelectKey(keyProperty = "ippoVanId", resultType = Long.class, before = false, statement = "SELECT LAST_INSERT_ID()")
	int addIppoVanAccount(Map<String, Object> body);

	@Insert("INSERT INTO IppoVanOrderRecord (platformId, userId, gameId,payConfigId, channelId, status,createTime) VALUES "
			+ "(#{platformId}, #{userId}, #{gameId},#{payConfigId},#{channelId},  #{status}, #{createTime});")
	@SelectKey(keyProperty = "id", resultType = Long.class, before = false, statement = "SELECT LAST_INSERT_ID()")
	int addIppoVanOrderRecord(Map<String, Object> body);

	@Update("UPDATE IppoVanAccount SET  ippoLock=#{ippoLock},updateLock=now() WHERE ippoName=#{ippoName}  and ippoLock=0 limit 1")
	int lockIppoVanAccount(@Param("ippoLock") Long id, @Param("ippoName") String ippoName);

	@Select("select * from IppoVanAccount where  ippoLock  =#{ippoLock} ")
	Map<String, Object> getIppoVanAccount(@Param("ippoLock") Long id);

	@Select("SELECT * FROM  IppoVanOrderRecord  where platformId=#{platformId} and userId=#{userId} and channelId=#{channelId} and status=1 order  by  id desc  limit 1")
	Map<String, Object> countIppoVanOrderRecord(@Param("platformId") Integer platformId,
			@Param("userId") Integer userId,@Param("channelId")  Long channelId);

	@Select("SELECT * FROM IppoVanAccount WHERE van_id = #{van_id}")
	Map<String, Object> getIppoVanAccountByVanId(@Param("van_id") String van_id);
	
	@Select("SELECT * FROM IppoVanAccount WHERE account = #{account} and  ifsc = #{ifsc}")
	List<Map<String, Object>> listIppoVanAccountByAccountAndIfsc(@Param("account") String account,@Param("ifsc") String ifsc);

	@Select("SELECT * FROM IppoVanOrderRecord WHERE id = #{id}")
	Map<String, Object> getIppoVanOrderRecord(@Param("id") Long id);
	

	@Select("SELECT count(*) FROM OrderInfo001 WHERE orderId = #{orderId}")
	Long countOrderInfo001(@Param("orderId") String orderId);

	@Insert("INSERT INTO IppoVanCallack (type, param, createTime) VALUES " + "(#{type}, #{param}, #{createTime} );")
	int addIppoVanCallack(@Param("type") int type, @Param("param") String param, @Param("createTime") Date createTime);

	@Update("UPDATE IppoVanOrderRecord SET ippoVanId =#{ippoVanId}, van_id =#{van_id}, account = #{account}, ifsc =#{ifsc},account_name=#{account_name}, status =#{status},successTime=#{successTime},ippoName=#{ippoName} WHERE id = #{id}")
	int updateIppoVanOrderRecord2(Map<String, Object> body);

	@Update("UPDATE IppoVanOrderRecord SET  successTime=#{successTime} WHERE id = #{id}")
	int updateIppoVanOrderRecord3(Map<String, Object> body);

//	@Update("UPDATE IppoVanOrderRecord SET payAmount =#{payAmount}, successTime = #{successTime},status =#{status} WHERE id = #{id}")
//	int updateIppoVanOrderRecordSuccess(Map<String, Object> body);

	

	@Select("SELECT * FROM  IppoVanOrderRecord  WHERE `status` = 1 AND  successTime < #{successTime}  limit #{limit}")
	List<Map<String, Object>>  recoverIppoVanOrderRecord(@Param("successTime") Date successTime , @Param("limit") int limit);

	@Update("UPDATE IppoVanOrderRecord SET  status=#{status} WHERE id = #{id}")
	int nullifyIppoVanOrderRecord(@Param("id") Long id, @Param("status") int status);
	
	

	@Select("select * from IppoVanAccount where  ippoVanId  =#{ippoVanId} ")
	Map<String, Object> getIppoVanAccountByIppoVanId(@Param("ippoVanId") Long ippoVanId);
	
	@Update("UPDATE IppoVanAccount SET  ippoLock=0 WHERE ippoVanId = #{ippoVanId}")
	int nullifyIppoVanAccount(@Param("ippoVanId") Long ippoVanId );

	@Select("SELECT * FROM  IppoVanOrderRecord  where account=#{account} and ifsc=#{ifsc} and account_name=#{account_name} and status=1 order by id desc limit 1")
	Map<String, Object> getIppoVanOrderRecordByAccount(@Param("account") String account, @Param("ifsc") String ifsc, @Param("account_name") String account_name);

	@Select({"<script>", "SELECT * FROM  IppoVanOrderRecord" ,
			"<where> ",
			"<if test='account != null and account != \"\" ' > and account=#{account}</if>",
			"<if test='van_id != null and van_id != \"\" ' > and van_id=#{van_id}</if>",
			"<if test='account_name != null and account_name != \"\" ' > and account_name=#{account_name}</if>",
			"<if test='gameId != null and gameId != \"\" ' > and gameId=#{gameId}</if>",
			"<if test='platformId != null and platformId != \"\" and platformId != \"0\" ' > and platformId=#{platformId}</if>",
			"</where>","order by id desc limit 10",
			"</script>"})
	List<Map<String, Object>> getIppoVanOrderRecordListByQuery(@Param("account") String account, @Param("van_id") String van_id, @Param("account_name") String account_name,
															   @Param("gameId") String gameId, @Param("platformId") String platformId);

	@Select({"<script>", "SELECT * FROM  IppoVanOrderRecord" ,
			"<where> ",
			"<if test='paramsMap.account != null and paramsMap.account != \"\" ' > and account=#{paramsMap.account}</if>",
			"<if test='paramsMap.van_id != null and paramsMap.van_id != \"\" ' > and van_id=#{paramsMap.van_id}</if>",
			"<if test='paramsMap.account_name != null and paramsMap.account_name != \"\" ' > and account_name=#{paramsMap.account_name}</if>",
			"<if test='paramsMap.gameId != null and paramsMap.gameId != \"\" ' > and gameId=#{paramsMap.gameId}</if>",
			"<if test='paramsMap.platformId != null and paramsMap.platformId != \"\"  and paramsMap.platformId != \"0\" ' > and platformId=#{paramsMap.platformId}</if>",
			"</where>", "order by id desc limit 10",
			"</script>"})
	List<Map<String, Object>> searchVanOrderListByParams(@Param("paramsMap") Map<String, String> body);

	@Select({"<script>", "SELECT * FROM  IppoVanOrderRecord2022" ,
			"<where> ",
			"<if test='paramsMap.account != null and paramsMap.account != \"\" ' > and account=#{paramsMap.account}</if>",
			"<if test='paramsMap.van_id != null and paramsMap.van_id != \"\" ' > and van_id=#{paramsMap.van_id}</if>",
			"<if test='paramsMap.account_name != null and paramsMap.account_name != \"\" ' > and account_name=#{paramsMap.account_name}</if>",
			"<if test='paramsMap.platformId != null and paramsMap.platformId != \"\"  and paramsMap.platformId != \"0\" ' > and platformId=#{paramsMap.platformId}</if>",
			"<if test='paramsMap.gameId != null and paramsMap.gameId != \"\" ' > and gameId=#{paramsMap.gameId}</if>",
			"</where>", "order by id desc limit 10",
			"</script>"})
	List<Map<String, Object>> searchVanOrderListByParams2022(@Param("paramsMap") Map<String, String> body);
}
