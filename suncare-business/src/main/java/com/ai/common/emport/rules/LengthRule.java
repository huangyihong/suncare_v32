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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * 功能描述：字符串长度限制
 *
 * @author  zhangly
 */
public class LengthRule extends BaseRule {
	private int max;
	
	public LengthRule() {
		
	}
	
	public LengthRule(String title) {
		this.title = title;
	}

	@Override
	public RuleMessage validator() {
		if(!lengthValidator()) {
			return RuleMessage.newFail(String.format("%s长度不允许超过%s，中文占用两个长度；", title, max));
		}
		return RuleMessage.newOk();
	}
	
	private boolean lengthValidator() {
		if (value == null) {
		      return true;
	    }		
	    int length = value.length();
	    Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(value);
        if(m.find()) {
        	length = 0;
        	for(int i=0; i<value.length(); i++) {
        		char c = value.charAt(i);
        		if(c >= 0x0391 && c <= 0xFFE5) {
        			length = length + 2;
        		} else {
        			length = length + 1;
        		}
        	}
        }
	    return length <= max;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}
}
