package com.letsvpn.pay.util;

public class KeyValue<KEY, VALUE> {

	KEY key;

	public KeyValue(KEY key, VALUE value) {
		this.key = key;
		this.value = value;
	}

	public KeyValue() {

	}

	VALUE value;

	public KEY getKey() {
		return key;
	}

	public void setKey(KEY key) {
		this.key = key;
	}

	public VALUE getValue() {
		return value;
	}

	public void setValue(VALUE value) {
		this.value = value;
	}

}
