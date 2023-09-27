/**
 * AbsRuleScriptHandler.java	  V1.0   2023年2月16日 上午9:01:31
 *
 * Copyright (c) 2023 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.ybChargeSearch.handle.rule.script;

import com.ai.modules.ybChargeSearch.handle.rule.model.RuleScriptResult;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleTaskModel;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbsRuleScriptHandler {

	protected RuleTaskModel ruleTaskModel;

	public AbsRuleScriptHandler(RuleTaskModel ruleTaskModel) {
		this.ruleTaskModel = ruleTaskModel;
	}

	/**
	 *
	 * 功能描述：解析规则的sql脚本
	 * @author zhangly
	 * @date 2023-02-17 14:15:30
	 *
	 * @param
	 *
	 * @return com.ai.modules.ybChargeSearch.handle.rule.model.RuleScriptResult
	 *
	 */
	public abstract RuleScriptResult parseRuleScript();

	/**
	 *
	 * 功能描述：解析规则限定内容的查询条件
	 * @author zhangly
	 * @date 2023-02-17 14:14:54
	 *
	 * @param
	 *
	 * @return java.util.List<java.lang.String>
	 *
	 */
	public abstract List<String> parseRuleWhere();
}
