/**
 * DigestUtil.java	  V1.0   2018年4月21日 下午8:01:58
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.api.util;

import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DigestUtil {
	private static final Logger logger = LoggerFactory.getLogger(DigestUtil.class);

	public static <T> String digest(Map<String, T> dataMap, String secret, DigestALGEnum de,
			String encoding) {
		/*if (dataMap == null) {
			throw new IllegalArgumentException("数据不能为空");
		}*/
		if (secret == null) {
			throw new IllegalArgumentException("安全验证私钥（secret）不能为空！");
		}
		if (de == null) {
			de = DigestALGEnum.MD5;
		}
		if ((encoding == null) || (encoding.trim().equalsIgnoreCase(""))) {
			encoding = "UTF-8";
		}

		TreeMap<String, T> treeMap = new TreeMap<String, T>(dataMap);
		StringBuilder sb = new StringBuilder(secret);
		for (Map.Entry<String, T> entry : treeMap.entrySet()) {
			logger.debug(new StringBuilder().append(entry.getKey()).append(":").append(entry.getValue()).append(";").toString());
			if (entry.getValue() == null) {
				continue;
			}
			if (((String) entry.getKey()).equals("sign")) {
				continue;
			}
			if(entry.getValue() != null) {
				sb.append((String) entry.getKey()).append("=").append(entry.getValue().toString()).append("&");
			} else {
				sb.append((String) entry.getKey()).append("=&");
			}
		}
		sb.deleteCharAt(sb.length() - 1);

		sb.append(secret);
		try {
			String str = sb.toString();
			byte[] toDigest = str.getBytes(encoding);
			if (logger.isDebugEnabled()) {
				logger.debug(new StringBuilder().append("待签名url:").append(str).toString());
			}

			MessageDigest md = MessageDigest.getInstance(de.getName());
			md.update(toDigest);
			return new String(Hex.encodeHex(md.digest())).toUpperCase();
		} catch (Exception e) {
			throw new RuntimeException("签名失败", e);
		}		
	}

	public static String sign(String text) throws Exception {
		String str = text;
		byte[] toDigest = str.getBytes("utf-8");
		if (logger.isDebugEnabled()) {
			logger.debug(new StringBuilder().append("待签名url:").append(str).toString());
		}
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(toDigest);
		return new String(Hex.encodeHex(md.digest())).toUpperCase();
	}

	public static enum DigestALGEnum {
		SHA256("SHA-256"), MD5("MD5");

		private String name;

		private DigestALGEnum(String name) {
			this.name = name;
		}

		public static DigestALGEnum getByName(String name) {
			for (DigestALGEnum _enum : values()) {
				if (_enum.getName().equals(name)) {
					return _enum;
				}
			}
			return null;
		}

		public String getName() {
			return this.name;
		}
	}
	
	public static void main(String[] args) throws Exception {
		String text = "helloworldappKey=12345678&bizContent={userCode:\"userCode\", userPwd:\"password\"}&timestamp=2016-01-01 12:00:00&v=2.0helloworld";
		System.out.println(DigestUtil.sign(text));
	}
}
