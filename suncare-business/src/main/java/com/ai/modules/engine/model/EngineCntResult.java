/**
 * EngineCntResult.java	  V1.0   2020年12月30日 上午10:45:40
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

import lombok.Data;

@Data
public class EngineCntResult {
	protected boolean success;
	protected String message;
	//记录数
	private long count = 0;
	
	public EngineCntResult(boolean success, String message) {
		this.success = success;
		this.message = message;
	}
	
	public static EngineCntResult ok() {
		EngineCntResult result = new EngineCntResult(true, "success");
		return result;
	}
	
	public static EngineCntResult error(String message) {
		EngineCntResult result = new EngineCntResult(false, message);
		return result;
	}
}
