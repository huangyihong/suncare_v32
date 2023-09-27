/**
 * RuleModel.java	  V1.0   2023年2月15日 下午5:56:01
 *
 * Copyright (c) 2023 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.ybChargeSearch.handle.rule.model;

import lombok.Data;

@Data
public class RuleLimitModel {
	
	private String limitType;
	private String limitText;
	
	public RuleLimitModel(String limitType, String limitText) {
		this.limitType = limitType;
		this.limitText = limitText;
	}
}
