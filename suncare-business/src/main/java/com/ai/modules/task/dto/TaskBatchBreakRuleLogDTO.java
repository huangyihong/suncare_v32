/**
 * TaskBatchBreakRuleLogDTO.java	  V1.0   2021年4月28日 上午11:04:59
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.task.dto;

import lombok.Data;

@Data
public class TaskBatchBreakRuleLogDTO {
	private String batchId;
	private String itemType;
	private String itemName;
	private String status;
	private String actionId;
	private String ruleLimit;
}
