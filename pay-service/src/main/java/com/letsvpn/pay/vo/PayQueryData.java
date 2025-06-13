package com.letsvpn.pay.vo;

import java.util.HashMap;
import java.util.Map;

public class PayQueryData {

	Map<String, String> formData = new HashMap<String, String>();

	public Map<String, String> getFormData() {
		return formData;
	}

	public void setFormData(Map<String, String> formData) {
		this.formData = formData;
	}

}
