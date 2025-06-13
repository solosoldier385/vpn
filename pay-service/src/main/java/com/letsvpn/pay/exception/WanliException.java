package com.letsvpn.pay.exception;

public class WanliException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int code;
	private Object[] param;

	public Object[] getParam() {
		return param;
	}

	public void setParam(Object[] param) {
		this.param = param;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public WanliException(int code, String msg) {
		super(msg);
		this.code = code;

	}

	public WanliException() {

	}

	public WanliException(String msg) {
		super(msg);
		this.code = 404;
	}

	public WanliException(int code, String msg, String... param) {
		super(msg);
		this.code = code;
		this.param = param;

	}

//	public WanliException(KeyValue<Integer, String> keyValue) {
//		super(keyValue.getValue());
//		this.code = keyValue.getKey();
//
//	}
}
