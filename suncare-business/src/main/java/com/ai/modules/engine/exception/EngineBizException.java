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

/**
 * 
 * 功能描述：业务异常类
 *
 * @author  zhangly
 * Date: 2021年8月18日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineBizException extends Exception {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	protected String errorCode;

	public EngineBizException(String message) {
		super(message);
		this.errorCode = "9999";
	
	}
	/**
	 * 自定义异常编码和异常提示信息构造业务异常信息
	 * @param code 异常编码
	 * @param message 异常提示信息
	 */
	public EngineBizException(String code, String message) {
		super(message);
		this.errorCode = code;
	}

	/**
	 * 自定义异常编码和异常提示信息,以及原始异常堆栈构造业务异常信息
	 * @param code 异常编码
	 * @param message 异常提示信息
	 * @param cause 原始异常堆栈
	 */
	public EngineBizException(String code, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = code;
	}

	public String getErrorCode() {
		return errorCode;
	}
}
