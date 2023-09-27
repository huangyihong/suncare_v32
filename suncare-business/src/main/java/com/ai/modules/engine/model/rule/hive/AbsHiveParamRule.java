/**
 * AbsHiveParamRule.java	  V1.0   2022年11月14日 上午10:29:50
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.rule.hive;

import com.ai.modules.engine.model.rule.AbsEngineParamRule;

import lombok.Data;

@Data
public abstract class AbsHiveParamRule extends AbsEngineParamRule {
	protected String fromTable;
	/**
	 * 是否需要追加在就诊日期之前条件
	 */
	protected boolean beforeVisit = false;
	
	public AbsHiveParamRule() {
		
	}
	
	public AbsHiveParamRule(String tableName, String colName, String compareType, String compareValue, String fromTable) {
		this.tableName = tableName;
		this.colName = colName;
		this.compareType = compareType;
		this.compareValue = compareValue;
		this.fromTable = fromTable;
	}
	
	public AbsHiveParamRule(String tableName, String colName, String compareValue, String fromTable) {
		this(tableName, colName, "=", compareValue, fromTable);
	}
	
	public AbsHiveParamRule(String colName, String compareValue, String fromTable) {
		this(null, colName, "=", compareValue, fromTable);
	}
}
