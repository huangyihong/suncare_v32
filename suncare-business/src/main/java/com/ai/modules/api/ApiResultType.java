package com.ai.modules.api;

/**
 * api接口错误定义类
 *
 */
public enum ApiResultType {
	OK(200, "success"),
	ERROR(9999, "未知错误");

	private int code;

	private String message;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	ApiResultType() {

	}

	ApiResultType(int code, String message) {
		this.code = code;
		this.message = message;
	}
}
