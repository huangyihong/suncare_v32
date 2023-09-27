/**
 * XmlRule.java	  V1.0   2018年11月25日 下午5:12:41
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.common.emport;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class XmlRule implements Serializable {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -6207747010603424650L;
	//主题
	private String theme;
	//excel中的sheet页签
	private String sheetName;
	//在excel中标题行的起始位置从0开始，默认0
	private int start = 0;
	//在excel中标题行的偏移量即行数，默认1行
	private int offset = 1;
	//在excel中是否有底部
	private boolean foot = false;
	private String[] titles;
	private XmlField[] xmlFields;
	private Map<String, XmlField> xmlFieldMap;
	//主键生成策略
	private String pkTemplate;
	
	public XmlRule(String theme, String[] titles, XmlField[] xmlFields, Map<String, XmlField> xmlFieldMap) {
		this.theme = theme;
		this.titles = titles;
		this.xmlFields = xmlFields;
		this.xmlFieldMap = xmlFieldMap;
	}
	
	public XmlRule(String[] titles, XmlField[] xmlFields, Map<String, XmlField> xmlFieldMap) {
		this.titles = titles;
		this.xmlFields = xmlFields;
		this.xmlFieldMap = xmlFieldMap;
	}

	@Override
	public String toString() {
		return "XmlRule [theme=" + theme + ", titles=" + Arrays.toString(titles) + ", xmlFields="
				+ Arrays.toString(xmlFields) + ", xmlFieldMap=" + xmlFieldMap + "]";
	}
	
	public String getRequiredFields() {
		Set<String> set = new LinkedHashSet<String>();
		for(XmlField xmlField : xmlFields) {
			if(xmlField.isRequired()) {
				set.add(xmlField.getTitle());
			}
		}
		if(set.size()>0) {
			return StringUtils.join(set, ",");
		}
		return null;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String[] getTitles() {
		return titles;
	}

	public void setTitles(String[] titles) {
		this.titles = titles;
	}

	public XmlField[] getXmlFields() {
		return xmlFields;
	}

	public void setXmlFields(XmlField[] xmlFields) {
		this.xmlFields = xmlFields;
	}

	public Map<String, XmlField> getXmlFieldMap() {
		return xmlFieldMap;
	}

	public void setXmlFieldMap(Map<String, XmlField> xmlFieldMap) {
		this.xmlFieldMap = xmlFieldMap;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public boolean isFoot() {
		return foot;
	}

	public void setFoot(boolean foot) {
		this.foot = foot;
	}

	public String getPkTemplate() {
		return pkTemplate;
	}

	public void setPkTemplate(String pkTemplate) {
		this.pkTemplate = pkTemplate;
	}

	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}
}
