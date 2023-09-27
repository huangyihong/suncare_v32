/**
 * ApiOauthInfo.java	  V1.0   2018年4月21日 下午8:09:54
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.api;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ai.modules.api.util.DigestUtil;

public class ApiOauthInfo {
	public static final String DEFAULT_V = "2.0";
	public static final String DEFAULT_SIGN_METHOD = "MD5";
	// 方法名
	private String method;
	// 时间戳
	private String timestamp;
	// appKey
	private String appKey;
	// 数据签名
	private String sign;
	// appSession
	private String session;
	// appSecret
	private String secret;
	// 版本号
	private String v = DEFAULT_V;
	private String sign_method = DEFAULT_SIGN_METHOD;

	public Map<String, String> buildMap(Map<String, String> busiMap) {
		Map<String, String> result = new HashMap<String, String>();
		//result.put("method", getMethod());
		result.put("timestamp", getTimestamp());
		result.put("appKey", getAppKey());
		//result.put("session", getSession());
		result.put("v", getV());

		this.sign = calSign(busiMap, result);

		if ((this.sign != null) && (!this.sign.trim().equalsIgnoreCase(""))) {
			result.put("sign", this.sign);
		}

		return result;
	}

	private String calSign(Map<String, String> busiMap, Map<String, String> oauthMap) {
		Map<String, String> params = new HashMap<String, String>();
		params.putAll(oauthMap);
		if(busiMap!=null) {
			params.putAll(busiMap);
		}
		return DigestUtil.digest(params, getSecret(), DigestUtil.DigestALGEnum.MD5, "UTF-8");
	}
	
	public boolean checkParams() throws ApiOauthException {
		/*if ((getMethod() == null) || (getMethod().trim().equals(""))) {
			throw new ApiOauthException("参数验证失败，请求API服务端的方法名（method）信息缺失！");
		}*/
		if ((this.getAppKey() == null) || (getAppKey().trim().equals(""))) {
			throw new ApiOauthException(ApiErrorCode.UNKONW_APP_KEY_ERR);
		}
		if (this.getTimestamp() == null || this.getTimestamp().trim().equals("")) {
			throw new ApiOauthException(ApiErrorCode.UNKONW_TIMESTAMP_ERR);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date date = sdf.parse(this.getTimestamp());
			Timestamp t = new Timestamp(date.getTime());
			if(Math.abs(System.currentTimeMillis()-t.getTime())>600000) {
				//时间戳误差已超过10分钟
				throw new ApiOauthException(ApiErrorCode.TIMESTAMP_EXCESS_ERR);
			}
		} catch (ParseException e) {
			throw new ApiOauthException(ApiErrorCode.TIMESTAMP_FORMAT_ERR);
		}
		
		if ((this.getV() == null) || (getV().trim().equals(""))) {
			throw new ApiOauthException(ApiErrorCode.UNKONW_V_ERR);
		}
		if(!DEFAULT_V.equals(getV())) {
			throw new ApiOauthException(ApiErrorCode.V_ERR);
		}
		if ((this.getSign() == null) || (getSign().trim().equals(""))) {
			throw new ApiOauthException(ApiErrorCode.UNKONW_SIGN_ERR);
		}
		return true;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getV() {
		return v;
	}

	public void setV(String v) {
		this.v = v;
	}

	public String getSign_method() {
		return sign_method;
	}

	public void setSign_method(String sign_method) {
		this.sign_method = sign_method;
	}
}
