package com.letsvpn.pay.mapper.ext;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExtPayIpWhiteMapper {
	@Select("select count(1) from PayIpWhite where ip=#{ip} and platformId=#{platformId} and status=1")
	Long countPayIpWhite(@Param("ip") String ip, @Param("platformId") int platformId);

	@Select("select account from AccountBlacklist where status=1")
	List<String> allAccountBlacklist();

}
