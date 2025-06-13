package com.letsvpn.pay.mapper.ext;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ExtProxyClientIpMapper {
	@Update("UPDATE ProxyClientIp SET updateTime=NOW(), reqCount=reqCount+1  WHERE ip=#{ip} and nullify=0")
	int updateProxyClientIp(@Param("ip") String ip);

}
