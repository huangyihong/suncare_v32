/**
 * AbsHiveRuleParser.java	  V1.0   2022年11月10日 上午9:50:38
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.parse.hive;

import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;

public abstract class AbsHiveRuleParser {
	
	protected MedicalRuleConfig rule;
	protected MedicalRuleConditionSet condition;
	protected String fromTable;
	
	public AbsHiveRuleParser(MedicalRuleConfig rule, MedicalRuleConditionSet condition, String fromTable) {
		this.rule = rule;
		this.condition = condition;
		this.fromTable = fromTable;
	}
	
	public abstract WithTableModel parseCondition();
	
	protected String buildWithTable() {
		Integer groupNo = condition.getGroupNo();
		groupNo = groupNo==null ? 0 : groupNo;
		Integer orderNo = condition.getOrderNo();
		orderNo = orderNo==null ? 0 : orderNo;
		return "table_".concat(condition.getType()).concat("_").concat(groupNo.toString()).concat(orderNo.toString());
	}
}
