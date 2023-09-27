/**
 * ExcelCell.java	  V1.0   2022年9月26日 上午9:22:33
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.common.emport;

import org.apache.poi.ss.usermodel.CellType;

public class ExcelCell {
	private CellType cellType;
	/** 属性字段名*/
	private String fieldName;
	private String fieldValue;
	
	public ExcelCell(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public CellType getCellType() {
		return cellType;
	}
	public void setCellType(CellType cellType) {
		this.cellType = cellType;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getFieldValue() {
		return fieldValue;
	}
	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}
}
