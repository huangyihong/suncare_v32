/**
 * ExportField.java	  V1.0   2016年4月14日 上午10:10:10
 *
 * Copyright (c) 2016 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.common.export;

import java.util.List;

public class ExportFields {
	/**多个属性集中展示一列*/
	private List<ExportField> attrs;
	/**属性直接的分隔符，默认空格*/
	private String split = " ";
	
	public List<ExportField> getAttrs() {
		return attrs;
	}
	public void setAttrs(List<ExportField> attrs) {
		this.attrs = attrs;
	}
	public String getSplit() {
		return split;
	}
	public void setSplit(String split) {
		this.split = split;
	}
}
