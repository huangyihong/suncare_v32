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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.dom4j.Element;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.MD5Util;

import com.ai.common.emport.rules.BaseDictRule;
import com.ai.common.emport.rules.BaseRule;
import com.ai.modules.engine.util.PlaceholderResolverUtil;

public abstract class AbstractExcelImport extends DataImport implements Serializable {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -1624367643427652452L;
	protected static final Long FILE_SIZE_LIMIT = 2*1024*1024L;
	/** 限制导入条数*/
	protected Integer limitSize = 1000;
	/** 限制文件大小，单位字节*/
	protected Long limitFlow = 0L;
	
	/**
	 * 
	 * 功能描述：判断是否存在合并列（主题行）
	 *
	 * @author  zhangly
	 *
	 * @param sheet
	 * @return
	 */
	protected int getMergedRowNum(Sheet sheet) {
		int num = 0;
		int total = sheet.getLastRowNum();
        for(int row=0; row<total; row++) {
        	int column = 0;
    		int sheetMergeCount = sheet.getNumMergedRegions();
    		boolean has = false;
    		for (int i = 0; i < sheetMergeCount; i++) {   
    	       	CellRangeAddress range = sheet.getMergedRegion(i);   
    	        int firstColumn = range.getFirstColumn(); 
    	        int lastColumn = range.getLastColumn();   
    	        int firstRow = range.getFirstRow();   
    	        int lastRow = range.getLastRow();	        
    	        if(row >= firstRow && row <= lastRow && column >= firstColumn && column <= lastColumn) { 
    	        	has = true;
    	        	break;
    	        }
    		}
    		if(!has) {
    			break;
    		}
    		num++;
        }
        return num;
	}
	
	/**
	 * 
	 * 功能描述：遍历excel行
	 *
	 * @author  zhangly
	 */
	protected List<Map<String, String>> foreachExcelRow(Sheet sheet, XmlRule xmlRule) throws Exception {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		Iterator<Row> it = sheet.rowIterator();
		Row titleRow = (Row) it.next();
		if(StringUtils.isNotBlank(xmlRule.getTheme())) {
			//excel模板包含主题
			titleRow = (Row) it.next();
		}
		// 遍历标题行
		for(int i=0; i<xmlRule.getTitles().length; i++) {
			Cell cell = titleRow.getCell(i);
			if(cell==null) {
				throw new Exception("请按模板上传excel附件！");
			}
			String value = cell.getStringCellValue().trim();
			if(cell.getStringCellValue()==null || !value.equals(xmlRule.getTitles()[i])) {
				throw new Exception("请按模板上传excel附件！");
			}
		}
		// 规则类map	
		Map<Class<? extends BaseRule>, BaseRule> ruleMap = new HashMap<Class<? extends BaseRule>, BaseRule>();
		// 数据起始行号
		int dataRow = StringUtils.isNotBlank(xmlRule.getTheme()) ? 3 : 2;	
		// 每行数据map
		Map<String, String> dataMap = null;
		// 唯一值存储
		Map<String, Set<String>> uniqueMap = new HashMap<String, Set<String>>();
		while(it.hasNext()) {
			// 每行数据验证成功与否
			boolean validate = true;
			// 每行数据map
			dataMap = new HashMap<String, String>();	
			// 每行的异常信息
			RowFailMessage rowMessage = new RowFailMessage(dataRow);
			
			Row row = (Row) it.next();								
			for(int i=0; i<xmlRule.getTitles().length; i++) {
				Cell cell = row.getCell(i);
				XmlField bean = xmlRule.getXmlFields()[i];
				String value = getCellValue(cell, bean);	
				
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
				for(int i=0; i<xmlRule.getTitles().length; i++) {
					Cell cell = row.getCell(i);
					XmlField bean = xmlRule.getXmlFields()[i];
					String value = getCellValue(cell, bean, false);
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
	}
	
	/**
	 * 
	 * 功能描述：遍历excel行（excel标题行允许与xml文件配置的顺序不一致，智能匹配xml对应字段）
	 *
	 * @author  zhangly
	 */
	protected List<Map<String, String>> foreachExcelRowAutoMapping(Sheet sheet, XmlRule xmlRule) throws Exception {
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
			for(int start=xmlRule.getStart(); start<xmlRule.getStart()+xmlRule.getOffset(); start++) {
				Row titleRow = sheet.getRow(start);
				if(titleRow!=null) {
					for(int j=0,size=titleRow.getPhysicalNumberOfCells(); j<size&&idx==-1; j++) {
						Cell cell = titleRow.getCell(j);
						if(cell!=null) {
							//excel字段标题
							cell.setCellType(CellType.STRING);
							String value = cell.getStringCellValue().trim();
							value = value.replaceAll("\\s*|\r|\n|\t", "");
							boolean find = false;
							if(xmlField.isLike()) {
								//模糊匹配
								for(String match : includeSet) {
									if(value.contains(match)) {
										find = true;
									}
								}
							} else {
								if(includeSet.contains(value)) {
									find = true;
								}
							}
							if(find) {
								//找到字段在excel中的列
								idx = j;
								excelColumnTitle = value;
								break;
							}
						}
					}
				}
			}
			if(idx==-1 && xmlField.isRequired()) {
				throw new Exception("请按模板上传excel附件！（必填项["+xmlField.getTitle()+"]未匹配到）");
			}
			XmlFieldMapping mapping = new XmlFieldMapping(xmlField, excelColumnTitle, idx);
			xmlMappingList.add(mapping);
			
		}
		
		return this.foreachExcelDataRow(sheet, xmlMappingList);
	}
	
	/**
	 * 
	 * 功能描述：遍历数据行
	 *
	 * @author  zhangly
	 *
	 * @param sheet
	 * @param xmlMappingList
	 * @return
	 * @throws Exception
	 */
	protected List<Map<String, String>> foreachExcelDataRow(Sheet sheet, List<XmlFieldMapping> xmlMappingList) throws Exception {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		// 规则类map	
		Map<Class<? extends BaseRule>, BaseRule> ruleMap = new HashMap<Class<? extends BaseRule>, BaseRule>();
		// 每行数据map
		Map<String, String> dataMap = null;
		// 唯一值存储
		Map<String, Set<String>> uniqueMap = new HashMap<String, Set<String>>();
		// 数据起始行索引号
		int start = xmlRule.getStart() + xmlRule.getOffset();
		// 数据终止行索引号
		int end = sheet.getPhysicalNumberOfRows();
		List<ExcelCell> cells = new ArrayList<ExcelCell>();
		for(; start<end; start++) {
			cells.clear();
			// 每行数据验证成功与否
			boolean validate = true;
			// 每行数据map
			dataMap = new HashMap<String, String>();	
			// 每行的异常信息
			int dataRow = start+1;
			RowFailMessage rowMessage = new RowFailMessage(dataRow);
			
			Row row = sheet.getRow(start);
			if(row==null) {
				continue;
			}
			
			for(int i=0; i<xmlMappingList.size(); i++) {
				XmlFieldMapping mapping = xmlMappingList.get(i);
				XmlField bean = mapping.getXmlField();
				Integer col = mapping.getColumnIdx();
				String value = null;
				if(col>-1) {
					Cell cell = row.getCell(col);
					if(cell!=null) {
						ExcelCell excelCell = new ExcelCell(bean.getFieldName());
						excelCell.setCellType(cell.getCellType());
						value = getCellValue(cell, bean);
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
						excelCell.setFieldValue(value);
						cells.add(excelCell);
					}
				}								
				//验证数据
				boolean flag = executeRule(bean, mapping.getTitle(), value, ruleMap, rowMessage);					
				validate = validate && flag;					
			}
			if(!validate && xmlRule.isFoot() && this.stopRow(cells)) {
				//如果是终止行，停止遍历
				break;
			}
			if(validate) {
				// 数据验证通过，再次遍历行，数据转换为map对象
				for(int i=0; i<xmlMappingList.size(); i++) {
					XmlFieldMapping mapping = xmlMappingList.get(i);
					XmlField bean = xmlRule.getXmlFields()[i];
					Integer col = mapping.getColumnIdx();
					String value = null;
					if(col>-1) {
						Cell cell = row.getCell(col);
						value = getCellValue(cell, bean, false);
					}
					dataMap.put(bean.getFieldName(), value);						
				}
				dataMap.put("ROW_NUM", String.valueOf(dataRow));
				if(StringUtils.isNotBlank(xmlRule.getPkTemplate())) {
					//主键
					String pk = xmlRule.getPkTemplate();
					pk = PlaceholderResolverUtil.replacePlaceholders(pk, dataMap);
			        pk = MD5Util.MD5Encode(pk, "UTF-8");
					dataMap.put("PRIMARY_KEY", pk);
				}
				result.add(dataMap);
				successTotal++;				
			} else {
				failTotal++;
				//failMessages.add(rowMessage);
				rowFailMessageMap.put(dataRow, rowMessage);
			}
			total++;
		}
		return result;
	}
	
	/**
	 * 
	 * 功能描述：遍历excel行
	 *
	 * @author  zhangly
	 */
	protected <T> List<T> foreachExcelRow(Sheet sheet, XmlRule xmlRule, Class<T> clazz) throws Exception {
		List<T> result = new ArrayList<T>();
		Iterator<Row> it = sheet.rowIterator();
		Row titleRow = (Row) it.next();
		if(StringUtils.isNotBlank(xmlRule.getTheme())) {
			//excel模板包含主题
			titleRow = (Row) it.next();
		}
		// 遍历标题行
		for(int i=0; i<xmlRule.getTitles().length; i++) {
			Cell cell = titleRow.getCell(i);
			if(cell==null) {
				throw new Exception("请按模板上传xlsx附件！");
			}
			if(cell.getStringCellValue()==null || !cell.getStringCellValue().equals(xmlRule.getTitles()[i])) {
				throw new Exception("请按模板上传xlsx附件！");
			}
		}
		// 规则类map	
		Map<Class<? extends BaseRule>, BaseRule> ruleMap = new HashMap<Class<? extends BaseRule>, BaseRule>();
		// 数据起始行号
		int dataRow = StringUtils.isNotBlank(xmlRule.getTheme()) ? 3 : 2;	
		// 每行数据map
		Map<String, String> dataMap = null;
		// 唯一值存储
		Map<String, Set<String>> uniqueMap = new HashMap<String, Set<String>>();
		while(it.hasNext()) {
			// 每行数据验证成功与否
			boolean validate = true;
			// 每行数据map
			dataMap = new HashMap<String, String>();	
			// 每行的异常信息
			RowFailMessage rowMessage = new RowFailMessage(dataRow);
			
			Row row = (Row) it.next();								
			for(int i=0; i<xmlRule.getTitles().length; i++) {
				Cell cell = row.getCell(i);
				XmlField bean = xmlRule.getXmlFields()[i];
				String value = getCellValue(cell, bean);	
				
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
					Cell cell = row.getCell(i);
					XmlField bean = xmlRule.getXmlFields()[i];
					String value = getCellValue(cell, bean, false);
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
	}
	
	/**
	 * 
	 * 功能描述：判断终止行
	 *
	 * @author  zhangly
	 *
	 * @param cells
	 * @return
	 */
	protected boolean stopRow(List<ExcelCell> cells) {
		Set<String> values = new HashSet<String>();
		Set<CellType> cellTypes = new HashSet<CellType>();
		for(ExcelCell cell : cells) {
			String value = cell.getFieldValue();
			if(StringUtils.isNotBlank(value)) {
				values.add(value);
				cellTypes.add(cell.getCellType());
			}
		}
		if(cellTypes.size()==1 && cellTypes.iterator().next()==CellType.FORMULA) {
			return true;
		}
		if(values.contains("合计") || values.contains("经办人：")) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * 功能描述：获取列值
	 *
	 * @author  zhangly
	 *
	 * @param cell
	 * @param bean
	 * @param valid 是否校验数据
	 * @return
	 */
	protected String getCellValue(Cell cell, XmlField bean, boolean valid) {
		if(cell==null) {
			return null;
		}
		if(CellType.NUMERIC==cell.getCellType()) {
			//数字
			short format = cell.getCellStyle().getDataFormat();
			if(org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)
					|| format == 20 || format == 32
					|| format == 14 || format == 31 || format == 57 || format == 58) {
				String cellValue = "";				
				//日期字段
				SimpleDateFormat sdf = null;  
                if (format == 20 || format == 32) {  
                    sdf = new SimpleDateFormat("HH:mm");  
                } else if (format == 14 || format == 31 || format == 57 || format == 58) {  
                    // 处理自定义日期格式：m月d日(通过判断单元格的格式id解决，id的值是58)  
                    sdf = new SimpleDateFormat("yyyy-MM-dd");  
                    double value = cell.getNumericCellValue();  
                    Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value);  
                    cellValue = sdf.format(date);  
                } else {
                	// 日期
                	String pattern = "yyyy-MM-dd";
                	if(StringUtils.isNotBlank(bean.getFormat())) {
                		pattern = bean.getFormat();
                	}
                    sdf = new SimpleDateFormat(pattern);  
                }  
                try {
                    cellValue = sdf.format(cell.getDateCellValue());// 日期
                } catch (Exception e) {
                    try {
                        throw new Exception("exception on get date data !".concat(e.toString()));
                    } catch (Exception e1) {
                    }
                } finally {
                    sdf = null;
                }
                return cellValue;
			}
			long longVal = Math.round(cell.getNumericCellValue());
			double value = cell.getNumericCellValue();
			if(Double.parseDouble(longVal+".0")==value) {
				//整数，cell类型转成String再取值，否则取出值是用科学计数法表示的数值
				cell.setCellType(CellType.STRING);
				return cell.getStringCellValue();
			}
			return String.valueOf(value);
		} else if(cell.getCellType()!=CellType.STRING) {
			cell.setCellType(CellType.STRING);
		}
		String value = cell.getStringCellValue();
		if(value!=null && "null".equalsIgnoreCase(value)) {
			return null;
		}
		if(cell.getCellType()==CellType.STRING
				&& StringUtils.isNotBlank(bean.getFormat())) {
			String format = bean.getFormat();
			String regex = "[^y|Y|m|M|d|D|h|H|s|S]+";
			Pattern p = Pattern.compile(regex);
			//是否严格模式，严格模式包含-/年月日特殊、中文等字符，如：yyyy-MM-dd
			boolean strict = p.matcher(format).find();
			if(!strict) {
				//剔除非数字的字符
				regex = "[^0-9]+";				
				value = value.replaceAll(regex, "");
			}
			if("Date".equals(bean.getFieldType())) {
				//为防止库表字段是时间类型遇到年或月份字符串插入数据库后出现0000日期问题
				try {
					SimpleDateFormat sdf = new SimpleDateFormat(format);
					Date date = sdf.parse(value);
					if(format.length()<8) {
						format = "yyyyMMdd";
					}
					value = DateUtils.formatDate(date, format);
				} catch(Exception e) {}
			}
		}
		if(!valid && bean.hasRuleClass(BaseDictRule.class)) {
			//字典解析
			Element element = bean.getElement("dict");
			String dictKey = element.elementText("key");
			String type = element.elementText("type");			
			try {
				Class<?> clazz = bean.getRuleClass(BaseDictRule.class);
				Constructor<?> constructor = clazz.getConstructor(new Class[] {});
				BaseDictRule dictRule = (BaseDictRule)constructor.newInstance();
				dictRule.init(bean.getTitle(), value);
				dictRule.setDictKey(dictKey);
				dictRule.setType(type);
				value = dictRule.findDict();
			} catch (Exception e) {
				
			}		
		}
		return value;
	}
	
	protected String getCellValue(Cell cell, XmlField bean) {
		return getCellValue(cell, bean, true);
	}
}
