/**
 * MedicalColumnQualityDtlVO.java	  V1.0   2021年3月16日 上午10:59:02
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.medical.vo;

import com.ai.modules.medical.entity.MedicalDrugRule;

public class MedicalColumnQualityDtlVO {
	private String column;
	private String actionId;
	private String ruleId;
	
	public MedicalColumnQualityDtlVO(String column, MedicalDruguseColumnVO vo) {
		this.column = column;
		this.actionId = vo.getActionId();
		this.ruleId = vo.getRuleId();
	}
	
	public MedicalColumnQualityDtlVO(String column, MedicalRuleConfigColumnVO vo) {
		this.column = column;
		this.actionId = vo.getActionId();
		this.ruleId = vo.getRuleId();
	}
	
	public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		this.column = column;
	}
	public String getActionId() {
		return actionId;
	}
	public void setActionId(String actionId) {
		this.actionId = actionId;
	}
	public String getRuleId() {
		return ruleId;
	}
	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}
}
