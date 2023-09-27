/**
 * ReportTableTd.java	  V1.0   2019年4月10日 下午5:09:40
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.report;

public class ReportTableTd {
	/**占用行数*/
	private int rowspan = 1;
	/**占用列数*/
	private int colspan = 1;
	/**值*/
	private Object value;
	
	public int getRowspan() {
		return rowspan;
	}
	public void setRowspan(int rowspan) {
		this.rowspan = rowspan;
	}
	public int getColspan() {
		return colspan;
	}
	public void setColspan(int colspan) {
		this.colspan = colspan;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
}
