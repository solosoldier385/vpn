package com.letsvpn.pay.vo;

import lombok.Data;

import java.util.Date;

//`payConfigId` int(11) NOT NULL,
//`classServer` varchar(255) DEFAULT NULL,
//`gateway` varchar(255) DEFAULT NULL,
//`appId` varchar(255) DEFAULT NULL,
//`privateKey` varchar(255) DEFAULT NULL,
//`nullity` int(11) DEFAULT NULL,
//`createTime` datetime DEFAULT NULL,
//`beginExtTime` datetime DEFAULT NULL,
//`endExtTime` datetime DEFAULT NULL,
//`pollingId` bigint(20) DEFAULT 0,
@Data
public class PayConfigPolling {
	Integer payConfigId;
	String classServer;
	String gateway;
	String appId;
	String privateKey;
	Integer nullity;
	Date createTime;
	Date beginExtTime;
	Date endExtTime;
	Long pollingId;
}
