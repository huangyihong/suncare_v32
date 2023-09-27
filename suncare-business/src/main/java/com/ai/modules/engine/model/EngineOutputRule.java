/**
 * EngineOutputRule.java	  V1.0   2021年11月26日 上午9:25:09
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class EngineOutputRule {
	//表名
	private String tableName;
	//字段名
	private String colName;
	//比较运算符
	private String compareType;
	//比较值
	private Set<String> compareValueSet = new LinkedHashSet<String>();
	
	public EngineOutputRule(String tableName, String colName, String compareType, String compareValue) {
		this.tableName = tableName;
		this.colName = colName;
		this.compareType = compareType;
		compareValueSet.add(compareValue);
	}
	
	@Override
	public String toString() {
		return "EngineOutputRule [tableName=" + tableName + ", colName=" + colName + ", compareType=" + compareType
				+ ", compareValueSet=" + compareValueSet + "]";
	}

	public void addCompareValue(String value) {
		compareValueSet.add(value);
	}
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getColName() {
		return colName;
	}
	public void setColName(String colName) {
		this.colName = colName;
	}
	public String getCompareType() {
		return compareType;
	}
	public void setCompareType(String compareType) {
		this.compareType = compareType;
	}
	public Set<String> getCompareValueSet() {
		return compareValueSet;
	}
	public void setCompareValueSet(Set<String> compareValueSet) {
		this.compareValueSet = compareValueSet;
	}
}
