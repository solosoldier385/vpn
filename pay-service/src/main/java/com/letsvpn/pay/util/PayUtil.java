package com.letsvpn.pay.util;



import com.letsvpn.pay.exception.WanliException;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class PayUtil {

	public static void validate(boolean flag, String msg, int code) {
		if (flag) {
			throw new WanliException(code, msg);
		}

	}

	public static void validate(boolean flag, String msg, int code, String... param) {
		if (flag) {
			throw new WanliException(code, msg, param);
		}

	}

	public static void validateParam(boolean flag, int code, String... param) {
		if (flag) {
			throw new WanliException(code, "", param);
		}

	}

//	public static void validate(boolean flag, String msg) {
//		validate(flag, msg, 404);
//
//	}

	public static ResourceBundle extLocale(Locale curLoc) {

		if (curLoc == null) {
			curLoc = Locale.ENGLISH;
		}
		if (curLoc.getLanguage().equals("en")) {
			curLoc = Locale.ENGLISH;
		} else if (curLoc.getLanguage().equals("zh")) {
			curLoc = Locale.CHINESE;
		} else {
			curLoc = Locale.ENGLISH;
		}
		return ResourceBundle.getBundle("messages", curLoc);
	}

	public static String extLocale(Locale curLoc, int code) {
		return extLocale(curLoc).getString(String.valueOf(code));
	}

	public static String extLocale(Locale curLoc, int code, Object... params) {
		return MessageFormat.format(extLocale(curLoc).getString(String.valueOf(code)), params);
	}

	// https://www.cloudflare.com/ips-v4
	public static final String[] CLOUDFLARE_IP = "173.245.48.0/20,103.21.244.0/22,103.22.200.0/22,103.31.4.0/22,141.101.64.0/18,108.162.192.0/18,190.93.240.0/20,188.114.96.0/20,197.234.240.0/22,198.41.128.0/17,162.158.0.0/15,104.16.0.0/13,104.24.0.0/14,172.64.0.0/13,131.0.72.0/22"
			.split(",");

	// 核心代码，检索IP所属网段
	public static boolean isInRange(String ip, String cidr) {
		String[] ips = ip.split("\\.");
		long ipAddr = (Integer.parseInt(ips[0]) << 24) | (Integer.parseInt(ips[1]) << 16)
				| (Integer.parseInt(ips[2]) << 8) | Integer.parseInt(ips[3]);
		long type = Integer.parseInt(cidr.replaceAll(".*/", ""));
		long mask = 0xFFFFFFFF << (32 - type);
		String cidrIp = cidr.replaceAll("/.*", "");
		String[] cidrIps = cidrIp.split("\\.");
		long networkIpAddr = (Integer.parseInt(cidrIps[0]) << 24) | (Integer.parseInt(cidrIps[1]) << 16)
				| (Integer.parseInt(cidrIps[2]) << 8) | Integer.parseInt(cidrIps[3]);
		return (ipAddr & mask) == (networkIpAddr & mask);
	}

	public static boolean isCloudflare(String ip) {
		for (String cidr : CLOUDFLARE_IP) {
			if (isInRange(ip, cidr)) {
				return true;
			}
		}
		return false;
	}

}
