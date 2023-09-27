/**
 * RuleRegexResult.java	  V1.0   2023年2月16日 上午10:47:52
 *
 * Copyright (c) 2023 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.ybChargeSearch.handle.rule.model;

import com.ai.modules.ybChargeSearch.constants.RuleRegexConstants;

import lombok.Data;

@Data
public class RuleRegexResult {
	protected boolean success;
	protected String message;
	
	public RuleRegexResult(boolean success, String message) {
		this.success = success;
		this.message = message;
	}
	
	public static RuleRegexResult ok() {
		RuleRegexResult result = new RuleRegexResult(true, "验证通过");
		return result;
	}
	
	public static RuleRegexResult error(String message) {
		RuleRegexResult result = new RuleRegexResult(false, message);
		return result;
	}
	
	public static RuleRegexResult error(RuleRegexConstants ruleRegex) {
		RuleRegexResult result = new RuleRegexResult(false, ruleRegex.getMessage());
		return result;
	}

	@Override
	public String toString() {
		return "RuleRegexResult{" +
				"success=" + success +
				", message='" + message + '\'' +
				'}';
	}
}
