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
 * 功能描述：自定义正则表达式验证
 *
 * @author  zhangly
 */
public class RegexRule extends BaseRule {
	private String regex;
	private String message;
	
	public RegexRule() {
		
	}
	
	public RegexRule(String title, String regex, String message) {
		this.title = title;
		this.regex = regex;
		this.message = message;
	}

	@Override
	public RuleMessage validator() {
		if(StringUtils.isNotBlank(value) && !Pattern.matches(regex, value)) {
			if(StringUtils.isNotBlank(message)) {
				return RuleMessage.newFail(String.format("%s格式不正确，%s；", title, message));
			} else {
				return RuleMessage.newFail(String.format("%s格式不正确；", title));
			}
		}
		return RuleMessage.newOk();
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
