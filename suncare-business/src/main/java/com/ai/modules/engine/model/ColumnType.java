/**
 * ColumnType.java	  V1.0   2022年12月13日 上午11:06:18
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

import lombok.Data;

@Data
public class ColumnType {
	private String columnName;
	private String columnType;
	
	public ColumnType(String columnName, String columnType) {
		this.columnName = columnName;
		this.columnType = columnType;
	}
}
