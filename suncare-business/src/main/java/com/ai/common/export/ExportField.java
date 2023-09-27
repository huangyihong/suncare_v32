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

public class ExportField {
	/**对应的属性*/
	private String attr;
	/**字典参数解析key*/
	private String dictKey;
		
	public String getAttr() {
		return attr;
	}
	public void setAttr(String attr) {
		this.attr = attr;
	}
	public String getDictKey() {
		return dictKey;
	}
	public void setDictKey(String dictKey) {
		this.dictKey = dictKey;
	}
	@Override
	public String toString() {
		return "ExportField [attr=" + attr + ", dictKey=" + dictKey + "]";
	}	
}
