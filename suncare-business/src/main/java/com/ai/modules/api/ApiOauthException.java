/**
 * ApiOauthException.java	  V1.0   2018年4月21日 下午8:32:05
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.api;

import org.apache.shiro.authc.AuthenticationException;

public class ApiOauthException extends AuthenticationException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 5038363035941428076L;

	protected int errorCode;
	
	public ApiOauthException(int code, String message) {
		super(message);
		this.errorCode = code;
	}
	
	public ApiOauthException(String message) {
		super(message);
		this.errorCode = 9999;
	}
	
	public ApiOauthException(ApiErrorCode error) {
		super(error.getMessage());
		this.errorCode = error.getCode();
	}

	public int getErrorCode() {
		return errorCode;
	}
}
