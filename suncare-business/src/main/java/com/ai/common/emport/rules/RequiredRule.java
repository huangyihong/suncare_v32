/**
 * RequiredRule.java	  V1.0   2018年11月25日 下午3:10:31
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.common.emport.rules;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * 功能描述：非空限制
 *
 * @author  zhangly
 */
public class RequiredRule extends BaseRule {
	
	public RequiredRule() {
		
	}
	
	public RequiredRule(String title) {
		this.title = title;
	}

	@Override
	public RuleMessage validator() {
		if(StringUtils.isBlank(value)) {
			return RuleMessage.newFail(String.format("%s不能为空；", title));
		}
		return RuleMessage.newOk();
	}
}
