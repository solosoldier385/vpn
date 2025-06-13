package com.letsvpn.pay.vo;


import com.letsvpn.pay.util.PayCallMethod;

public class PayResultData {

	public PayResultData(PayCallMethod method) {
		super();
		this.method = method;
	}

	public PayResultData() {

	}

	PayCallMethod method;

	String link;
	String html;

	// 第三订单id
	String thirdOrderId;

	//kyc 手机号
	String extend1;

	//kyc 邮箱
	String extend2;

	//kyc 姓名
	String extend3;

	// 保存数据,是否保存数据,默认是报错的
	boolean saveData = true;

	public boolean isSaveData() {
		return saveData;
	}

	public void setSaveData(boolean saveData) {
		this.saveData = saveData;
	}

	public String getThirdOrderId() {
		return thirdOrderId;
	}

	public void setThirdOrderId(String thirdOrderId) {
		this.thirdOrderId = thirdOrderId;
	}

	public PayCallMethod getMethod() {
		return method;
	}

	public void setMethod(PayCallMethod method) {
		this.method = method;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public String getExtend1() {
		return extend1;
	}

	public void setExtend1(String extend1) {
		this.extend1 = extend1 == null ? null : extend1.trim();
	}

	public String getExtend2() {
		return extend2;
	}

	public void setExtend2(String extend2) {
		this.extend2 = extend2 == null ? null : extend2.trim();
	}

	public String getExtend3() {
		return extend3;
	}

	public void setExtend3(String extend3) {
		this.extend3 = extend3 == null ? null : extend3.trim();
	}

}
