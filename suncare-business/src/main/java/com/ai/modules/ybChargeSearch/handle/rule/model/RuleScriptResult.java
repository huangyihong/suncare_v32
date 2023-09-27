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
public class RuleScriptResult extends RuleRegexResult {
	private String script;

	public RuleScriptResult(boolean success, String message) {
		super(success, message);
	}

	public static RuleScriptResult ok(String script) {
		RuleScriptResult result = new RuleScriptResult(true, "验证通过");
		result.setScript(script);
		return result;
	}

	public static RuleScriptResult error(String message) {
		RuleScriptResult result = new RuleScriptResult(false, message);
		return result;
	}
}
