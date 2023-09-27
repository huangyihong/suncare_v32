/**
 * WithTableModel.java	  V1.0   2022年11月9日 下午8:02:33
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.hive.model;

import lombok.Data;

@Data
public class WithTableModel {
	private String alias;
	private String sql;
	private String logic = "AND";
	
	public WithTableModel(String alias, String sql) {
		this.alias = alias;
		this.sql = sql;
	}
	
	public WithTableModel(String alias, String sql, String logic) {
		this(alias, sql);
		this.logic = logic;
	}
}
