/**
 * EngineResult.java	  V1.0   2019年12月30日 下午12:54:51
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.config.vo;

import lombok.Data;

@Data
public class MedicalOrganResult {
	private String serialNum;
	private String message;
	//记录数
	private Integer count = 0;
	
	public MedicalOrganResult(String serialNum, String message) {
		this.serialNum = serialNum;
		this.message = message;
	}
}
