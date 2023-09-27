/**
 * FileImport.java	  V1.0   2022年9月1日 下午5:15:28
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.common.emport;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.jeecg.common.util.DateUtils;
import org.springframework.util.ReflectionUtils;

import com.ai.common.emport.rules.BaseDictRule;
import com.ai.common.emport.rules.BaseRule;
import com.ai.common.emport.rules.DateRule;
import com.ai.common.emport.rules.DigitsRule;
import com.ai.common.emport.rules.LengthRule;
import com.ai.common.emport.rules.NumberRule;
import com.ai.common.emport.rules.RegexRule;
import com.ai.common.emport.rules.RuleMessage;

public abstract class DataImport {
	
	/** excel文件映射到xml*/
	protected InputStream xmlInputStream;
	/** 总记录数*/
	protected int total = 0;
	/** 成功记录条数*/
	protected int successTotal = 0;
	/** 失败记录条数*/
	protected int failTotal = 0;
	/** 失败行异常信息*/
	protected Map<Integer, RowFailMessage> rowFailMessageMap = new HashMap<Integer, RowFailMessage>();
	/** excel文件映射到xml的规则对象*/
	protected XmlRule xmlRule;

	/**
	 *
	 * 功能描述：xml文件解析
	 * @author zhangly
	 * @date  09:21:33
	 *
	 * @param
	 *
	 * @return void
	 *
	 */
	protected void parseXmlRule() throws Exception {
		try {
			XmlRule xmlRule = XmlParseUtil.parse(xmlInputStream);
			if(xmlRule==null) {
				throw new Exception("导入的文件未映射到xml");
			}
			this.setXmlRule(xmlRule);
		} catch(Exception e) {
			throw e;
		} finally {
			this.close();
		}
	}
	
	/**
	 * 
	 * 功能描述：excel正确的行数据转换成map
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年3月18日 上午11:00:59</p>
	 *
	 * @param agreement 列顺序是否需要保持一致
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public abstract List<Map<String, String>> handle(boolean agreement) throws Exception;
	
	/**
	 * 
	 * 功能描述：excel正确的行数据转换成对象
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年3月18日 上午11:00:05</p>
	 *
	 * @param clazz
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public abstract <T> List<T> handle(Class<T> clazz) throws Exception;

	protected void close() {
		if(xmlInputStream!=null) {
			try {
				xmlInputStream.close();
			} catch (Exception e){}
		}
	}
	
	/**
	 * 
	 * 功能描述：验证规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2018年11月26日 下午4:09:33</p>
	 *
	 * @param bean
	 * @param value
	 * @param rowMessage
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected boolean executeRule(XmlField bean, String excelColumnTitle, String value, Map<Class<? extends BaseRule>, BaseRule> ruleMap, RowFailMessage rowMessage) throws Exception {
		boolean validate = true;
		if(bean.getRuleClasses()!=null && bean.getRuleClasses().size()>0) {
			for(Class<? extends BaseRule> clazz : bean.getRuleClasses()) {
				if(!ruleMap.containsKey(clazz)) {
					ruleMap.put(clazz, clazz.newInstance());
				}
				BaseRule baseRule = ruleMap.get(clazz);
				String title = StringUtils.isBlank(excelColumnTitle) ? bean.getTitle() : excelColumnTitle;
				baseRule.init(title, value);
				if(clazz.isAssignableFrom(LengthRule.class)) {
					// 长度验证
					LengthRule lengthRule = (LengthRule) baseRule;
					lengthRule.setMax(bean.getLength());
				} else if(clazz.isAssignableFrom(DateRule.class)) {
					// 日期验证
					DateRule dateRule = (DateRule) baseRule;
					dateRule.setFormat(bean.getFormat());
				} else if(clazz.isAssignableFrom(RegexRule.class)) {
					// 自定义正则表达式验证
					Element element = bean.getElement("regex");
					String express = element.elementText("express");
					String message = element.elementText("message");
        			RegexRule regexRule = (RegexRule) baseRule;
        			regexRule.setRegex(express);
        			regexRule.setMessage(message);
				} else if(clazz.isAssignableFrom(DigitsRule.class)
						|| clazz.isAssignableFrom(NumberRule.class)) {
					// 整数验证/数字验证
					Element element = bean.getElement("min");
					Integer min = null;
    				Long max = null;
    				if(element!=null) {
            			String text = element.getText();
            			if(StringUtils.isNotBlank(text)) {
            				min = Integer.parseInt(text);
            			}
            		}
    				element = bean.getElement("max");
    				if(element!=null) {
            			String text = element.getText();
            			if(StringUtils.isNotBlank(text)) {
            				max = Long.parseLong(text);
            			}
            		}
    				if(clazz.isAssignableFrom(DigitsRule.class)) {
    					DigitsRule digitsRule = (DigitsRule) baseRule;
    					digitsRule.setMin(min);
    					digitsRule.setMax(max);
    				} else {
    					NumberRule numberRule = (NumberRule) baseRule;
    					numberRule.setMin(min);
    					numberRule.setMax(max);
    				}
				} else if(baseRule instanceof BaseDictRule) {
					// 字典参数验证
					Element element = bean.getElement("dict");
					String dictKey = element.elementText("key");
					String type = element.elementText("type");
					BaseDictRule dictRule = (BaseDictRule) baseRule;
					dictRule.setDictKey(dictKey);
					dictRule.setType(type);
				}
				RuleMessage rule = (RuleMessage) baseRule.validator();
				if(!rule.isSuccess()) {
					validate = false;
					rowMessage.addRowMessage(rule.getMessage());
				}
			}
		}
		return validate;
	}
	
	protected boolean executeRule(XmlField bean, String value, Map<Class<? extends BaseRule>, BaseRule> ruleMap, RowFailMessage rowMessage) throws Exception {
		return executeRule(bean, null, value, ruleMap, rowMessage);
	}
	
	/**
	 * 
	 * 功能描述：map转换为对象
	 *
	 * @author  zhangly
	 */
	protected <T> T mapConvertObject(Map<String, String> dataMap, Class<T> clazz, XmlRule xmlRule) throws Exception {
		if(dataMap==null) {
			return null;
		}
		if(clazz.isAssignableFrom(Map.class)) {
			return (T) dataMap;
		}
		T object = clazz.newInstance();
		for (Map.Entry<String, String> entry : dataMap.entrySet()) {
			String value = entry.getValue();
			String key = entry.getKey();
			if(StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
				String methodName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);					
				Method method = findMethod(clazz, methodName);										
				if(method!=null) {
					if(Arrays.equals(method.getParameterTypes(), new Class<?>[]{String.class})){
						ReflectionUtils.invokeMethod(method, object, value);
					} else if(Arrays.equals(method.getParameterTypes(),new Class<?>[]{Date.class})) {
						XmlField bean = xmlRule.getXmlFieldMap().get(key);
						String format = bean.getFormat()!=null ? bean.getFormat() : "yyyy/MM/dd";
						//匹配所有可能的日期格式，剔除特殊、中午等字符
						String regex = "[^y|Y|m|M|d|D|h|H|s|S]+";
						format = format.replaceAll(regex, "");
						//剔除非数字的字符
						regex = "[^0-9]+";				
						value = value.replaceAll(regex, "");
						ReflectionUtils.invokeMethod(method, object, DateUtils.parseDate(String.valueOf(value), format));
					} else {
						Object newValue = method.getParameterTypes()[0].getConstructor(String.class).newInstance(value.toString());
						ReflectionUtils.invokeMethod(method, object, newValue);
					}
				}
			}
		}
		
		return object;
	}
	
	protected Method findMethod(Class<?> clazz, String methodName) {
		Class<?>[] matchClassTypes = new Class<?>[] { String.class, Long.class, Integer.class, Short.class,
				Double.class, Date.class, BigDecimal.class };
		Class<?>[] classTypes = new Class<?>[1];
		for (Class<?> classType : matchClassTypes) {
			classTypes[0] = classType;
			Method method = ReflectionUtils.findMethod(clazz, methodName, classTypes);
			if (method != null)
				return method;
		}
		return null;
	}
	
	public void addRowFailMessage(Integer row, String message) {
		RowFailMessage rowFailMessage = rowFailMessageMap.get(row);
		if(rowFailMessage==null) {
			rowFailMessage = new RowFailMessage(row);	
			//failMessages.add(rowFailMessage);
			rowFailMessageMap.put(row, rowFailMessage);
			this.failTotal = this.failTotal + 1;
			this.successTotal = this.successTotal - 1;
		}
		rowFailMessage.addRowMessage(message);
	}
	
	public boolean rowExistsFail(Integer row) {
		RowFailMessage rowFailMessage = rowFailMessageMap.get(row);
		return rowFailMessage==null ? false : true;
	}

	public InputStream getXmlInputStream() {
		return xmlInputStream;
	}

	public void setXmlInputStream(InputStream xmlInputStream) {
		this.xmlInputStream = xmlInputStream;
	}

	public int getTotal() {
		return total;
	}

	public int getSuccessTotal() {
		return successTotal;
	}

	public int getFailTotal() {
		return failTotal;
	}

	/*public List<RowFailMessage> getFailMessages() {
		return failMessages;
	}

	public void setFailMessages(List<RowFailMessage> failMessages) {
		this.failMessages = failMessages;
	}*/

	public Map<Integer, RowFailMessage> getRowFailMessageMap() {
		return rowFailMessageMap;
	}

	public void setRowFailMessageMap(Map<Integer, RowFailMessage> rowFailMessageMap) {
		this.rowFailMessageMap = rowFailMessageMap;
	}

	public XmlRule getXmlRule() {
		return xmlRule;
	}

	public void setXmlRule(XmlRule xmlRule) {
		this.xmlRule = xmlRule;
	}
}
