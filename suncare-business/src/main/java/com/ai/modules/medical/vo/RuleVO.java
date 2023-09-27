/**
 * RuleVO.java	  V1.0   2022年3月14日 下午4:55:43
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.medical.vo;

public class RuleVO {
	private String ruleType;
	private String ruleId;
	private String ruleCode;
	private String ruleName;
	private String ruleBasis;
	private String ruleRemark;
	private String ruleLimit;
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RuleVO other = (RuleVO) obj;
		if (ruleId == null) {
			if (other.ruleId != null)
				return false;
		} else if (!ruleId.equals(other.ruleId))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ruleId == null) ? 0 : ruleId.hashCode());
		return result;
	}
	
	public String getRuleType() {
		return ruleType;
	}

	public void setRuleType(String ruleType) {
		this.ruleType = ruleType;
	}

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public String getRuleCode() {
		return ruleCode;
	}

	public void setRuleCode(String ruleCode) {
		this.ruleCode = ruleCode;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getRuleRemark() {
		return ruleRemark;
	}

	public void setRuleRemark(String ruleRemark) {
		this.ruleRemark = ruleRemark;
	}

	public String getRuleLimit() {
		return ruleLimit;
	}

	public void setRuleLimit(String ruleLimit) {
		this.ruleLimit = ruleLimit;
	}

	public String getRuleBasis() {
		return ruleBasis;
	}

	public void setRuleBasis(String ruleBasis) {
		this.ruleBasis = ruleBasis;
	}
}
