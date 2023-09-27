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

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * 功能描述：邮箱格式验证
 *
 * @author  zhangly
 */
public class EmailRule extends BaseRule {
	
	public EmailRule() {
		
	}
	
	public EmailRule(String title) {
		this.title = title;
	}

	@Override
	public RuleMessage validator() {
		String regex = "^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w+)+)$";
		if(StringUtils.isNotBlank(value) && !Pattern.matches(regex, value)) {
			return RuleMessage.newFail(String.format("%s格式不正确；", title));
		}
		return RuleMessage.newOk();
	}
}
