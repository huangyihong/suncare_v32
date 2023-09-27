/**
 * AbstractExcelImport.java	  V1.0   2019年3月18日 上午10:18:01
 *
 * Copyright (c) 2019 AsiaInfo Linkage, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.common.emport;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.ai.common.emport.rules.BaseRule;

public abstract class AbstractCsvImport extends DataImport implements Serializable {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -1624367643427652452L;
	protected static final Long FILE_SIZE_LIMIT = 2*1024*1024L;
	/** 限制文件大小，单位字节*/
	protected long limitFlow = 0;
	/** csv文件的分隔符，默认逗号分割*/
	protected String split = ",";
	
	/**
	 * 
	 * 功能描述：遍历excel行
	 *
	 * @author  zhangly
	 */
	protected List<Map<String, String>> foreachCsvRow(InputStream is, XmlRule xmlRule) throws Exception {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		BufferedReader br = null;
		try {
			boolean hasTheme = false;
			if(StringUtils.isNotBlank(xmlRule.getTheme())) {
				//excel模板包含主题
				hasTheme = true;
			}
			br = new BufferedReader(new InputStreamReader(is));
			String line = br.readLine();
			if(line==null) {
				throw new Exception("请按模板上传csv附件！");
			}
			if(hasTheme) {
				line = br.readLine();
				if(line==null) {
					throw new Exception("请按模板上传csv附件！");
				}
			}
			String[] csvFields = StringUtils.split(line, split);
			if(xmlRule.getXmlFields().length>csvFields.length) {
				throw new Exception("请按模板上传csv附件！");
			}
			// 遍历标题行
			for(int i=0; i<xmlRule.getTitles().length; i++) {
				String value = csvFields[i];
				if(value==null || !value.trim().equals(xmlRule.getTitles()[i])) {
					throw new Exception("请按模板上传excel附件！");
				}
			}
			// 规则类map	
			Map<Class<? extends BaseRule>, BaseRule> ruleMap = new HashMap<Class<? extends BaseRule>, BaseRule>();
			// 数据起始行号
			int dataRow = hasTheme ? 3 : 2;	
			// 每行数据map
			Map<String, String> dataMap = null;
			// 唯一值存储
			Map<String, Set<String>> uniqueMap = new HashMap<String, Set<String>>();
			while((line=br.readLine())!=null) {
				String[] dataArray = StringUtils.split(line, split);
				// 每行数据验证成功与否
				boolean validate = true;
				// 每行数据map
				dataMap = new HashMap<String, String>();	
				// 每行的异常信息
				RowFailMessage rowMessage = new RowFailMessage(dataRow);
				
				for(int i=0, size=xmlRule.getTitles().length; i<size; i++) {
					XmlField bean = xmlRule.getXmlFields()[i];
					String value = null;	
					if(dataArray.length-1>i) {
						value = dataArray[i];
					}
					if(bean.isUnique()) {
						// 唯一值验证
						Set<String> uniqueSet = null;
						if(StringUtils.isNotBlank(value)) {
							if(uniqueMap.containsKey(bean.getFieldName())) {
								uniqueSet = uniqueMap.get(bean.getFieldName());
								if(uniqueSet.contains(value)) {
									throw new Exception(String.format("%s（%s）存在重复数据！", bean.getTitle(), value));
								}
								uniqueSet.add(value);
							} else {
								uniqueSet = new HashSet<String>();
								uniqueSet.add(value);
								uniqueMap.put(bean.getFieldName(), uniqueSet);
							}
						}
					}
					//验证数据
					boolean flag = executeRule(bean, value, ruleMap, rowMessage);					
					validate = validate && flag;					
				}
				if(validate) {
					// 数据验证通过，再次遍历行，数据转换为map对象
					for(int i=0,size=xmlRule.getTitles().length; i<size; i++) {
						XmlField bean = xmlRule.getXmlFields()[i];
						String value = null;	
						if(dataArray.length-1>i) {
							value = dataArray[i];
						}
						dataMap.put(bean.getFieldName(), value);						
					}
					dataMap.put("ROW_NUM", String.valueOf(dataRow));
					result.add(dataMap);
					successTotal++;				
				} else {
					failTotal++;
					//failMessages.add(rowMessage);
					rowFailMessageMap.put(dataRow, rowMessage);
				}
				total++;
				dataRow++;
			}
			return result;
		} catch(Exception e) {
			throw e;
		} finally {
			try {
				if(br!=null) {
					br.close();
				}
			} catch(Exception e) {}
		}
	}
	
	/**
	 * 
	 * 功能描述：遍历csv行（csv标题行允许与xml文件配置的顺序不一致，智能匹配xml对应字段）
	 *
	 * @author  zhangly
	 */
	protected List<Map<String, String>> foreachCsvRowAutoMapping(InputStream is, XmlRule xmlRule) throws Exception {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		BufferedReader br = null;
		try {
			boolean hasTheme = false;
			if(StringUtils.isNotBlank(xmlRule.getTheme())) {
				//excel模板包含主题
				hasTheme = true;
			}
			br = new BufferedReader(new InputStreamReader(is, "gbk"));
			String line = br.readLine();
			if(line==null) {
				throw new Exception("请按模板上传csv附件！（必填项未匹配到）");
			}
			if(hasTheme) {
				line = br.readLine();
				if(line==null) {
					throw new Exception("请按模板上传csv附件！（必填项未匹配到）");
				}
			}
			String[] csvFields = StringUtils.split(line, split);
			// 遍历标题行
			List<XmlFieldMapping> xmlMappingList = new ArrayList<XmlFieldMapping>();
			for(int i=0,len=xmlRule.getXmlFields().length; i<len; i++) {
				XmlField xmlField = xmlRule.getXmlFields()[i];
				//xml模板字段标题，多个范围时|分隔
				String title = xmlField.getTitle();
				String[] array = StringUtils.split(title, "|");
				//=-1表示未找到
				int idx = -1;
				String excelColumnTitle = null;
				List<String> includeSet = Arrays.asList(array);
				for(int j=0; j<csvFields.length; j++) {
					String value = csvFields[j].trim();
					if(idx==-1 && includeSet.contains(value)) {
						//找到字段在excel中的列
						idx = j;
						excelColumnTitle = value;
					}
				}
				if(idx==-1 && xmlField.isRequired()) {
					throw new Exception("请按模板上传csv附件！（必填项["+xmlField.getTitle()+"]未匹配到）");
				}
				XmlFieldMapping mapping = new XmlFieldMapping(xmlField, excelColumnTitle, idx);
				xmlMappingList.add(mapping);				
			}
			
			// 规则类map	
			Map<Class<? extends BaseRule>, BaseRule> ruleMap = new HashMap<Class<? extends BaseRule>, BaseRule>();
			// 数据起始行号
			int dataRow = hasTheme ? 3 : 2;
			// 每行数据map
			Map<String, String> dataMap = null;
			// 唯一值存储
			Map<String, Set<String>> uniqueMap = new HashMap<String, Set<String>>();
			//遍历数据行
			while((line=br.readLine())!=null) {
				String[] dataArray = StringUtils.split(line, split);
				// 每行数据验证成功与否
				boolean validate = true;
				// 每行数据map
				dataMap = new HashMap<String, String>();	
				// 每行的异常信息
				RowFailMessage rowMessage = new RowFailMessage(dataRow);
				for(int i=0; i<xmlMappingList.size(); i++) {
					XmlFieldMapping mapping = xmlMappingList.get(i);
					XmlField bean = mapping.getXmlField();
					Integer col = mapping.getColumnIdx();
					String value = null;
					if(col>-1 && col<dataArray.length) {
						value = dataArray[col];
						if(bean.isUnique()) {
							// 唯一值验证
							Set<String> uniqueSet = null;
							if(StringUtils.isNotBlank(value)) {
								if(uniqueMap.containsKey(bean.getFieldName())) {
									uniqueSet = uniqueMap.get(bean.getFieldName());
									if(uniqueSet.contains(value)) {
										throw new Exception(String.format("%s（%s）存在重复数据！", bean.getTitle(), value));
									}
									uniqueSet.add(value);
								} else {
									uniqueSet = new HashSet<String>();
									uniqueSet.add(value);
									uniqueMap.put(bean.getFieldName(), uniqueSet);
								}
							}
						}
					}								
					//验证数据
					boolean flag = executeRule(bean, mapping.getTitle(), value, ruleMap, rowMessage);					
					validate = validate && flag;					
				}
				if(validate) {
					// 数据验证通过，再次遍历行，数据转换为map对象
					for(int i=0; i<xmlMappingList.size(); i++) {
						XmlFieldMapping mapping = xmlMappingList.get(i);
						XmlField bean = xmlRule.getXmlFields()[i];
						Integer col = mapping.getColumnIdx();
						String value = null;
						if(col>-1 && col<dataArray.length) {
							value = dataArray[col];
						}
						dataMap.put(bean.getFieldName(), value);						
					}
					dataMap.put("ROW_NUM", String.valueOf(dataRow));
					result.add(dataMap);
					successTotal++;				
				} else {
					failTotal++;
					//failMessages.add(rowMessage);
					rowFailMessageMap.put(dataRow, rowMessage);
				}
				total++;
				dataRow++;
			}
			return result;
		} catch(Exception e) {
			throw e;
		} finally {
			try {
				if(br!=null) {
					br.close();
				}
			} catch(Exception e) {}
		}		
	}
	
	/**
	 * 
	 * 功能描述：遍历excel行
	 *
	 * @author  zhangly
	 */
	protected <T> List<T> foreachCsvRow(InputStream is, XmlRule xmlRule, Class<T> clazz) throws Exception {
		List<T> result = new ArrayList<T>();
		BufferedReader br = null;
		try {
			boolean hasTheme = false;
			if(StringUtils.isNotBlank(xmlRule.getTheme())) {
				//excel模板包含主题
				hasTheme = true;
			}
			br = new BufferedReader(new InputStreamReader(is));
			String line = br.readLine();
			if(line==null) {
				throw new Exception("请按模板上传csv附件！");
			}
			if(hasTheme) {
				line = br.readLine();
				if(line==null) {
					throw new Exception("请按模板上传csv附件！");
				}
			}
			String[] csvFields = StringUtils.split(line, split);
			if(xmlRule.getXmlFields().length>csvFields.length) {
				throw new Exception("请按模板上传csv附件！");
			}
			// 遍历标题行
			for(int i=0; i<xmlRule.getTitles().length; i++) {
				String value = csvFields[i];
				if(value==null || !value.trim().equals(xmlRule.getTitles()[i])) {
					throw new Exception("请按模板上传excel附件！");
				}
			}
			
			// 规则类map	
			Map<Class<? extends BaseRule>, BaseRule> ruleMap = new HashMap<Class<? extends BaseRule>, BaseRule>();
			// 数据起始行号
			int dataRow = hasTheme ? 3 : 2;	
			// 每行数据map
			Map<String, String> dataMap = null;
			// 唯一值存储
			Map<String, Set<String>> uniqueMap = new HashMap<String, Set<String>>();
			while((line=br.readLine())!=null) {
				String[] dataArray = StringUtils.split(line, split);
				// 每行数据验证成功与否
				boolean validate = true;
				// 每行数据map
				dataMap = new HashMap<String, String>();	
				// 每行的异常信息
				RowFailMessage rowMessage = new RowFailMessage(dataRow);
				
				for(int i=0; i<xmlRule.getTitles().length; i++) {
					XmlField bean = xmlRule.getXmlFields()[i];
					String value = null;	
					if(dataArray.length-1>i) {
						value = dataArray[i];
					}	
					
					if(bean.isUnique()) {
						// 唯一值验证
						Set<String> uniqueSet = null;
						if(StringUtils.isNotBlank(value)) {
							if(uniqueMap.containsKey(bean.getFieldName())) {
								uniqueSet = uniqueMap.get(bean.getFieldName());
								if(uniqueSet.contains(value)) {
									throw new Exception(String.format("%s（%s）存在重复数据！", bean.getTitle(), value));
								}
								uniqueSet.add(value);
							} else {
								uniqueSet = new HashSet<String>();
								uniqueSet.add(value);
								uniqueMap.put(bean.getFieldName(), uniqueSet);
							}
						}
					}
					
					boolean flag = executeRule(bean, value, ruleMap, rowMessage);					
					validate = validate && flag;					
				}
				if(validate) {
					// 数据验证通过，再次遍历行，数据转换为map对象
					for(int i=0; i<xmlRule.getTitles().length; i++) {
						XmlField bean = xmlRule.getXmlFields()[i];
						String value = null;	
						if(dataArray.length-1>i) {
							value = dataArray[i];
						}
						dataMap.put(bean.getFieldName(), value);						
					}
					T t = mapConvertObject(dataMap, clazz, xmlRule);
					result.add(t);
					successTotal++;
				} else {
					failTotal++;
					//failMessages.add(rowMessage);
					rowFailMessageMap.put(dataRow, rowMessage);
				}
				total++;
				dataRow++;
			}
			return result;
		} catch(Exception e) {
			throw e;
		} finally {
			try {
				if(br!=null) {
					br.close();
				}
			} catch(Exception e) {}
		}
	}
}
