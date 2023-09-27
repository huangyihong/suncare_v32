/**
 * EngineResult.java	  V1.0   2019年12月30日 下午12:54:51
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.drg.handle.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DrgResultModel {
	protected boolean success;
	protected String message;
	//已入组
	private Integer enrollment = 0;
	//未入组
	private Integer noEnrollment = 0;

	public DrgResultModel() {

	}

	public DrgResultModel(boolean success, String message) {
		this.success = success;
		this.message = message;
	}
	
	public static DrgResultModel ok() {
		DrgResultModel result = new DrgResultModel(true, "success");
		return result;
	}
	
	public static DrgResultModel error(String message) {
		DrgResultModel result = new DrgResultModel(false, message);
		return result;
	}
}
