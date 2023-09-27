/**
 * MedicalCaseColumnVO.java	  V1.0   2021年3月15日 上午10:34:10
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.medical.vo;

import com.ai.modules.medical.entity.MedicalRuleConditionSet;

import lombok.Data;

@Data
public class MedicalRuleConfigColumnVO extends MedicalRuleConditionSet {
	private String itemCodes;
	private String itemNames;
	private String actionId;
	private String ruleType;
	private String ruleLimit;
}
