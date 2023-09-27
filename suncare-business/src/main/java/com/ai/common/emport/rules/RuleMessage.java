/**
 * Rule.java	  V1.0   2018年11月25日 下午3:03:38
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.common.emport.rules;

public class RuleMessage {
	private boolean success;
	private String message;
	
	public RuleMessage(boolean success, String message) {
		this.success = success;
		this.message = message;
	}
	
	public static RuleMessage newOk() {
		return new RuleMessage(true, "success");
	}
	
	public static RuleMessage newFail(String message) {
		return new RuleMessage(false, message);
	}
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
