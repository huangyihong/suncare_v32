/**
 * ExcelField.java	  V1.0   2018年11月25日 上午11:37:07
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.common.emport;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;

import com.ai.common.emport.rules.BaseDictRule;
import com.ai.common.emport.rules.BaseRule;
import com.ai.common.emport.rules.DefineDictRule;
import com.ai.common.emport.rules.LengthRule;
import com.ai.common.emport.rules.RequiredRule;

public class XmlField implements Serializable {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 2355327565713240400L;
	/** 索引位置从0开始*/
	private Integer index;
	/** 标题*/
	private String title;
	/** 是否模糊匹配，默认false*/
	private boolean like = false;
	/** 属性字段名*/
	private String fieldName;
	/** 属性字段类型*/
	private String fieldType;
	/** 限制长度*/
	private Integer length;
	/** 时间格式化*/
	private String format;
	/** 是否唯一值*/
	private boolean unique = false;
	/** 规则类对象*/
	private Set<Class<? extends BaseRule>> ruleClasses;
	/** 特殊规则element*/
	private Map<String, Element> elementMap;
	
	public void addRuleClass(Class<? extends BaseRule> clazz) {
		if(ruleClasses==null) {
			ruleClasses = new LinkedHashSet<Class<? extends BaseRule>>();
		}
		ruleClasses.add(clazz);
	}
	
	public Class<?> getRuleClass(Class<?> cls) {
		if(ruleClasses!=null && ruleClasses.size()>0) {
			for(Class<?> clazz: ruleClasses) {
				if(cls.isAssignableFrom(clazz)) {
					return clazz;
				}
			}
		}
		return null;
	}
	
	public void putElement(String key, Element element) {		
		if(elementMap==null) {
			elementMap = new HashMap<String, Element>();
		}
		elementMap.put(key, element);
	}
	
	public Element getElement(String key) {
		if(elementMap==null) {
			return null;
		}
		return elementMap.get(key);
	}	
	
	public boolean isRequired() {
		return hasRuleClass(RequiredRule.class);
	}
	
	public boolean hasRuleClass(Class<?> cls) {
		if(ruleClasses!=null && ruleClasses.size()>0) {
			for(Class<?> clazz: ruleClasses) {
				if(cls.isAssignableFrom(clazz)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "XmlField [index=" + index + ", title=" + title + ", fieldName=" + fieldName + ", fieldType=" + fieldType
				+ ", length=" + length + ", format=" + format + ", ruleClasses=" + ruleClasses + ", elementMap="
				+ elementMap + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XmlField other = (XmlField) obj;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		return true;
	}

	public Integer getIndex() {
		return index;
	}
	public void setIndex(Integer index) {
		this.index = index;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public boolean isLike() {
		return like;
	}

	public void setLike(boolean like) {
		this.like = like;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getFieldType() {
		return fieldType;
	}
	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}
	public Integer getLength() {
		return length;
	}
	public void setLength(Integer length) {
		this.length = length;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	
	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public Set<Class<? extends BaseRule>> getRuleClasses() {
		return ruleClasses;
	}

	public void setRuleClasses(Set<Class<? extends BaseRule>> ruleClasses) {
		this.ruleClasses = ruleClasses;
	}

	public Map<String, Element> getElementMap() {
		return elementMap;
	}

	public void setElementMap(Map<String, Element> elementMap) {
		this.elementMap = elementMap;
	}

	public static void main(String[] args) throws Exception {
		Set<Class<? extends BaseRule>> ruleClasses = new HashSet<Class<? extends BaseRule>>();
		ruleClasses.add(DefineDictRule.class);
		ruleClasses.add(LengthRule.class);
		
		System.out.println(BaseDictRule.class.isAssignableFrom(DefineDictRule.class));
		System.out.println(BaseDictRule.class.isAssignableFrom(LengthRule.class));
	}
}
