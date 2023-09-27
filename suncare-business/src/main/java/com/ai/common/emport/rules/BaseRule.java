/**
 * BaseRule.java	  V1.0   2018年11月25日 下午3:03:12
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.common.emport.rules;

public class BaseRule {
	protected String title;
	protected String value;
	
	public BaseRule() {
		
	}	
	
	public void init(String title, String value) {
		this.title = title;
		this.value = value;
	}
	
	public RuleMessage validator() {
		return RuleMessage.newOk();
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
