/**
 * AbsRuleRegex.java	  V1.0   2023年2月15日 下午5:47:14
 *
 * Copyright (c) 2023 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.ybChargeSearch.handle.rule.regex;

import com.ai.modules.ybChargeSearch.handle.rule.model.RuleLimitModel;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleRegexResult;

public abstract class AbsRuleRegexParser {
	
	protected RuleLimitModel ruleLimitModel;
	
	public AbsRuleRegexParser(RuleLimitModel ruleLimitModel) {
		this.ruleLimitModel = ruleLimitModel;
	}
	
	public abstract RuleRegexResult validate();
}
