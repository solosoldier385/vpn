package com.letsvpn.pay.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderPollingResult {

	//你返回就3种情况  1 就是成功  0 就是 继续轮询  -1就是 失败终结 订单
	// 1 完成, 0 没有完成需要继续轮询,  其他数字不在处理
	int status;
	// 备注你要的东西
	String statusTitle;
	// 第三方返回的内容
	String resultBody;
}
