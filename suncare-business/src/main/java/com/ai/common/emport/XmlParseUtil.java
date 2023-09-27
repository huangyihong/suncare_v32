/**
 * XmlParseUtil.java	  V1.0   2018年11月25日 下午2:43:51
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.common.emport;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.ai.common.emport.rules.BaseDictRule;
import com.ai.common.emport.rules.BaseRule;
import com.ai.common.emport.rules.DateRule;
import com.ai.common.emport.rules.DictRule;
import com.ai.common.emport.rules.DigitsRule;
import com.ai.common.emport.rules.EmailRule;
import com.ai.common.emport.rules.LengthRule;
import com.ai.common.emport.rules.MobileRule;
import com.ai.common.emport.rules.NumberRule;
import com.ai.common.emport.rules.RegexRule;
import com.ai.common.emport.rules.RequiredRule;
import com.ai.common.emport.rules.RuleUtil;

public class XmlParseUtil {

	public static XmlRule parse(File xmlFile) throws Exception {
		if(!xmlFile.exists()) {
			return null;
		}
		return parse(new FileInputStream(xmlFile));
	}

	public static XmlRule parse(InputStream is) throws Exception {
		if(is==null) {
			return null;
		}
		// 创建SAXReader的对象reader
        SAXReader reader = new SAXReader();
        // 通过reader对象的read方法加载xml文件，获取docuemnt对象
        Document document = reader.read(is);
        // 通过document对象获取根节点fields
        Element fields = document.getRootElement();
        String theme = fields.attributeValue("theme");
		String sheet = fields.attributeValue("sheet");
        int size = fields.elements().size();
        String[] titles = new String[size];
        XmlField[] xmlFields = new XmlField[size];
        Map<String, XmlField> xmlFieldMap = new HashMap<String, XmlField>();
        // 通过element对象的elementIterator方法获取迭代器
        Iterator<?> it = fields.elementIterator();        
        // 遍历迭代器，获取根节点中的字段       
        Integer index = 0;
        while (it.hasNext()) {
        	XmlField model = new XmlField();
        	model.setIndex(index);
        	Element element = (Element) it.next();
        	String text = element.elementText("title");
        	model.setTitle(text);
        	titles[index] = text;
        	Element titleEle = element.element("title");
        	Boolean like = new Boolean(titleEle.attributeValue("like", "false"));
        	model.setLike(like);
        	text = element.elementText("fieldName");
        	if(xmlFieldMap.containsKey(text)) {
        		throw new Exception("映射的xml文件存在重名字段（"+text+")");
        	}
        	model.setFieldName(text);
        	text = element.elementText("fieldType");
        	model.setFieldType(text);
        	if(element.element("length")!=null && element.elementText("length")!=null) {
        		text = element.elementText("length");
        		model.setLength(Integer.parseInt(text));        		        		
        		
        		// 长度验证
        		model.addRuleClass(LengthRule.class);
        	}
        	if(element.element("format")!=null && element.elementText("format")!=null) {
        		text = element.elementText("format");
        		model.setFormat(text);
        		
        		// 日期验证
        		model.addRuleClass(DateRule.class);
        	}
        	if(element.element("rules")!=null) {
        		Element rules = element.element("rules");
        		if(rules.element("unique")!=null) {
        			// 是否唯一
        			boolean unique = new Boolean(rules.elementText("unique"));
        			if(unique) {
        				model.setUnique(true);
        			} else {
        				model.setUnique(false);
        			}
        		}
        		if(rules.element("required")!=null) {
        			// 非空验证
        			boolean required = new Boolean(rules.elementText("required"));
        			if(required) {
        				model.addRuleClass(RequiredRule.class);
        			}
        		}
        		if(rules.element("mobile")!=null) {
        			// 手机号码验证
        			boolean mobile = new Boolean(rules.elementText("mobile"));
        			if(mobile) {
        				model.addRuleClass(MobileRule.class);
        			}
        		}
        		if(rules.element("email")!=null) {
        			// 邮箱验证
        			boolean email = new Boolean(rules.elementText("email"));
        			if(email) {
        				model.addRuleClass(EmailRule.class);
        			}
        		}
        		if(rules.element("regex")!=null) {
        			// 自定义正则表达式验证
            		Element regexElement = rules.element("regex");            		            		
            		String express = regexElement.elementText("express");
            		if(StringUtils.isNotBlank(express)) {
            			model.putElement("regex", rules.element("regex"));
                		model.addRuleClass(RegexRule.class);            			
            		}
            	}
        		if(rules.element("digits")!=null) {
        			// 整数验证
        			boolean digits = new Boolean(rules.elementText("digits"));
        			if(digits) {
        				if(rules.element("min")!=null) {
                			text = rules.elementText("min");
                			if(StringUtils.isNotBlank(text)) {
                				if(!RuleUtil.isDigits(text)) {
                					throw new Exception("请检查xml文件节点（min)，不是整数！");
                				}
                			}
                		}
        				if(rules.element("max")!=null) {
                			text = rules.elementText("max");
                			if(StringUtils.isNotBlank(text)) {
                				if(!RuleUtil.isDigits(text)) {
                					throw new Exception("请检查xml文件节点（max)，不是整数！");
                				}
                			}
                		}
        				model.putElement("min", rules.element("min"));
        				model.putElement("max", rules.element("max"));
        				model.addRuleClass(DigitsRule.class);        				
        			}
        		}
        		if(rules.element("number")!=null) {
        			// 数字验证
        			boolean number = new Boolean(rules.elementText("number"));
        			if(number) {
        				if(rules.element("min")!=null) {
                			text = rules.elementText("min");
                			if(StringUtils.isNotBlank(text)) {
                				if(!RuleUtil.isNumber(text)) {
                					throw new Exception("请检查xml文件节点（min)，不是数字！");
                				}
                			}
                		}
        				if(rules.element("max")!=null) {
                			text = rules.elementText("max");
                			if(StringUtils.isNotBlank(text)) {
                				if(!RuleUtil.isNumber(text)) {
                					throw new Exception("请检查xml文件节点（max)，不是数字！");
                				}
                			}
                		}
        				model.putElement("min", rules.element("min"));
        				model.putElement("max", rules.element("max"));
        				model.addRuleClass(NumberRule.class);        				
        			}
        		}      
        		if(rules.element("dict")!=null) {
        			// 字典参数验证
            		Element dictElement = rules.element("dict");
            		String key = dictElement.elementText("key");   
            		if(StringUtils.isBlank(key)) {
            			throw new Exception("请检查xml文件节点（dict)！");
            		}
            		model.putElement("dict", rules.element("dict"));
        			String className = dictElement.elementText("class");
        			if(StringUtils.isNotBlank(className)) {
        				Class<BaseDictRule> clazz = (Class<BaseDictRule>) Class.forName(className);
        				model.addRuleClass(clazz);
        			} else {
        				model.addRuleClass(DictRule.class);
        			}
            	}
        		if(rules.element("classes")!=null) {
        			Iterator<?> classesIt = rules.element("classes").elementIterator();        
        	        // 遍历
        	        while (classesIt.hasNext()) {
        	        	Element classElement = (Element) classesIt.next();
        	        	String className = classElement.getText();
        	        	if(StringUtils.isNotBlank(className)) {
            				Class<BaseRule> clazz = (Class<BaseRule>) Class.forName(className);
            				model.addRuleClass(clazz);
            			}
        	        }
        		}
        	}        	
        	
        	xmlFields[index] = model;
        	xmlFieldMap.put(model.getFieldName(), model);
        	index = index + 1;
        }
        XmlRule xmlRule = new XmlRule(theme, titles, xmlFields, xmlFieldMap);
		//设置页签
		xmlRule.setSheetName(sheet);
        String def = "0";
        if(StringUtils.isNotBlank(theme)) {
        	def = "1";
        }
        int start = Integer.parseInt(fields.attributeValue("start", def));
        int offset = Integer.parseInt(fields.attributeValue("offset", "1"));
        //设置标题行的起始位置
        xmlRule.setStart(start);
        //设置标题行的偏移量即行数
        xmlRule.setOffset(offset);
        //设置是否有底部
        boolean foot = new Boolean(fields.attributeValue("foot", "false"));
        xmlRule.setFoot(foot);
        //主键生成策略
        String pkTemplate = fields.attributeValue("pk");
        if(StringUtils.isNotBlank(pkTemplate)) {
        	String[] array = StringUtils.split(pkTemplate, "_");
        	for(String key : array) {
        		key = StringUtils.replace(key, "${", "");
        		key = StringUtils.replace(key, "}", "");
        		if(!xmlFieldMap.containsKey(key)) {
        			throw new Exception("主键生成策略("+key+")字段未能找到！");
        		}
        	}
        	xmlRule.setPkTemplate(pkTemplate);
        }
        return xmlRule;
	}
	
	public static XmlRule parse(String xmlPath) throws Exception {
		if(StringUtils.isBlank(xmlPath)) {
			return null;
		}
		
		return parse(new File(xmlPath));
	}
	
	public static void main(String[] args) throws Exception {
		String path = XmlParseUtil.class.getClassLoader().getResource("com/ai/common/emport/example/file.xml").getPath();
		XmlRule result = XmlParseUtil.parse(path);
		System.out.println(result);
	}

}
