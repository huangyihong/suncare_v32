/**
 * EngineTableEntity.java	  V1.0   2020年11月18日 下午3:07:13
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

public class EngineTableEntity {
	private String table;
	private String alias;
	
	public EngineTableEntity(String table, String alias) {
		this.table = table;
		this.alias = alias;
	}
	
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
}
