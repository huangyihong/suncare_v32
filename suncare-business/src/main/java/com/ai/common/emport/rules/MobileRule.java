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
 * 功能描述：手机号码限制
 *
 * @author  zhangly
 */
public class MobileRule extends BaseRule {
	
	public MobileRule() {
		
	}
	
	public MobileRule(String title) {
		this.title = title;
	}

	@Override
	public RuleMessage validator() {
		String regex = "^((17[0-9])|(14[0-9])|(13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
		if(StringUtils.isNotBlank(value) && !Pattern.matches(regex, value)) {
			return RuleMessage.newFail(String.format("%s格式不正确；", title));
		}
		return RuleMessage.newOk();
	}
}
