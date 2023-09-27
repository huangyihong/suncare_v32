/**
 * EngineException.java	  V1.0   2020年11月11日 上午11:19:53
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.exception;

public class EngineException extends RuntimeException {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -4679267353931305474L;
	protected String errorCode;

	public EngineException(String message) {
		super(message);
		this.errorCode = "9999";
	
	}
	/**
	 * 自定义异常编码和异常提示信息构造业务异常信息
	 * @param code 异常编码
	 * @param message 异常提示信息
	 */
	public EngineException(String code, String message) {
		super(message);
		this.errorCode = code;
	}

	/**
	 * 自定义异常编码和异常提示信息,以及原始异常堆栈构造业务异常信息
	 * @param code 异常编码
	 * @param message 异常提示信息
	 * @param cause 原始异常堆栈
	 */
	public EngineException(String code, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = code;
	}

	public String getErrorCode() {
		return errorCode;
	}
}
