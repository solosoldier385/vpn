package com.letsvpn.pay.vo;

import lombok.Data;

import java.util.Date;

//`id` int(11) NOT NULL AUTO_INCREMENT,
//`payConfigId` int(11) DEFAULT NULL,
//`classServer` varchar(255) DEFAULT NULL,
//`nullity` int(11) DEFAULT NULL,
//`emailConfigJson` text,
//`gateway` varchar(255) DEFAULT NULL,
//`appId` varchar(255) DEFAULT NULL,
//`privateKey` varchar(255) DEFAULT NULL,
//`createTime` datetime DEFAULT NULL,
//`beginExtTime` datetime DEFAULT NULL,
//`endExtTime` datetime DEFAULT NULL,
@Data
public class PayConfigEmailPoll {

	Integer id;
	Integer payConfigId;
	String classServer;
	Integer nullity;
	String emailConfigJson;
	String gateway;
	String appId;
	String privateKey;
	Date createTime;
	Date beginExtTime;
	Date endExtTime;
	String regexId;
	String regexId2;
}
