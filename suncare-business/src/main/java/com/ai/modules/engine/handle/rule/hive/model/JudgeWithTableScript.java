/**
 * JudgeWithTableScript.java	  V1.0   2022年11月16日 上午10:56:51
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
public class JudgeWithTableScript {
	private String sql;
	private String resultTable;
	
	public JudgeWithTableScript(String sql, String resultTable) {
		this.sql = sql;
		this.resultTable = resultTable;
	}
}
