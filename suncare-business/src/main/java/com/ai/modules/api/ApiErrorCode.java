package com.ai.modules.api;

/**
 * api接口错误定义类
 *
 */
public enum ApiErrorCode {
	SUCCESS(200, "success"),
	UNKONW_APP_KEY_ERR(1001, "参数验证失败，请求API服务端的appKey信息缺失！"),		
	UNKONW_SIGN_ERR(1002, "参数验证失败，请求API服务端的签名（sign）信息缺失！"),	
	UNKONW_TIMESTAMP_ERR(1003, "参数验证失败，请求API服务端的时间戳（timestamp）信息缺失！"),
	UNKONW_V_ERR(1004, "参数验证失败，请求API服务端的版本号（v）信息缺失！"),
	APPKEY_VALUE_ERR(1005, "参数验证失败，appKey未被分配或已失效！"),
	SIGN_VALUE_ERR(1006, "参数验证失败，签名（sign）不正确！"),
	V_ERR(1007, "参数验证失败，版本号（v）不正确！"),
	TIMESTAMP_FORMAT_ERR(1008, "参数验证失败，时间戳（timestamp）格式不正确！"),
	TIMESTAMP_EXCESS_ERR(1009, "参数验证失败，时间戳（timestamp）超出误差范围！"),
	SERVER_BUSY_ERR(1098, "服务器正忙，请稍候再试！"),
	UNKNOW_EXCEPTION_ERR(1099, "未知错误，请联系服务提供商！"),
	FAIL(9999, "未知错误");

	private int code;

	private String message;

	private String desc;

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

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

	ApiErrorCode() {

	}

	ApiErrorCode(int code, String message) {
		this.code = code;
		this.message = message;

	}

	ApiErrorCode(int code, String message, String desc) {
		this.code = code;
		this.message = message;
		this.desc = desc;
	}

}
