/**
 * MedicalColumnQualityVO.java	  V1.0   2021年3月15日 上午10:57:16
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.medical.vo;

import java.util.HashSet;
import java.util.Set;

public class MedicalColumnQualityVO {
	private String column;
	private Set<String> actionSet = new HashSet<String>();
	private Set<String> actionNameSet = new HashSet<String>();
	private Set<String> ruleSet = new HashSet<String>();
	
	public MedicalColumnQualityVO(String column) {
		this.column = column;
	}
	
	public void addActionId(String actionId) {
		actionSet.add(actionId);
	}
	
	public void addActionName(String actionName) {
		actionNameSet.add(actionName);
	}
	
	public void addRule(String ruleId) {
		ruleSet.add(ruleId);
	}
	
	@Override
	public String toString() {
		String text = "MedicalColumnQualityVO[字段名=%s, 涉及不合规行为=%s, 涉及不合规行为名称=%s, 涉及规则数量=%s]";
		return String.format(text, column, actionSet, actionNameSet, ruleSet.size());
	}

	public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		this.column = column;
	}

	public Set<String> getActionSet() {
		return actionSet;
	}

	public void setActionSet(Set<String> actionSet) {
		this.actionSet = actionSet;
	}

	public Set<String> getActionNameSet() {
		return actionNameSet;
	}

	public void setActionNameSet(Set<String> actionNameSet) {
		this.actionNameSet = actionNameSet;
	}

	public Set<String> getRuleSet() {
		return ruleSet;
	}

	public void setRuleSet(Set<String> ruleSet) {
		this.ruleSet = ruleSet;
	}
}
