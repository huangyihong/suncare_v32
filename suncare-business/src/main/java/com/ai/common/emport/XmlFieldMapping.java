/**
 * ExcelFieldMapping.java	  V1.0   2022年8月31日 下午12:06:38
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.common.emport;

/**
 * xml与excel的字段映射关系
 * @author  zhangly
 * Date: 2022年8月31日
 */
public class XmlFieldMapping {
	private XmlField xmlField;
	//excel字段标题
	private String title;
	//excel所在列号，-1表示未找到
	private Integer columnIdx;	
	
	public XmlFieldMapping(XmlField xmlField, String title, Integer columnIdx) {
		this.xmlField = xmlField;
		this.title = title;
		this.columnIdx = columnIdx;		
	}

	public String getTitle() {
		return title;
	}

	public Integer getColumnIdx() {
		return columnIdx;
	}

	public XmlField getXmlField() {
		return xmlField;
	}
}
