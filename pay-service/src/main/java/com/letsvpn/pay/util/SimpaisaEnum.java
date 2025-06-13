package com.letsvpn.pay.util;

/*ALTER TABLE `PayConfigParameter`
ADD COLUMN `configEnum`  enum('request','notify','query') NULL DEFAULT 'request' COMMENT '配置类型' AFTER `push`;
*/

public enum SimpaisaEnum {
	initiate, verify
}
