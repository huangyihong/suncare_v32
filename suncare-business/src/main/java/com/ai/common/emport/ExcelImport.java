/**
 * ExcelParseUtil.java	  V1.0   2018年11月25日 下午4:37:30
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.common.emport;

import com.ai.common.emport.example.TestUser;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ExcelImport extends AbstractExcelImport {
	/** excel文件*/
	private File excelFile;	
	
	public ExcelImport(File excelFile, InputStream xmlInputStream, Integer limitSize) {
		this.excelFile = excelFile;
		this.xmlInputStream = xmlInputStream;
		this.limitSize = limitSize;
		this.limitFlow = 0L;
	}
	
	public ExcelImport(File excelFile, InputStream xmlInputStream, Long limitFlow) {
		this.excelFile = excelFile;
		this.xmlInputStream = xmlInputStream;
		this.limitFlow = limitFlow;
		this.limitSize = 0;
	}
	
	public ExcelImport(String excelPath, String xmlFilePath, int limitSize) throws FileNotFoundException {
		this(new File(excelPath), new FileInputStream(new File(xmlFilePath)), limitSize);
	}
	
	public ExcelImport(String excelPath, String xmlFilePath, long limitFlow) throws FileNotFoundException {
		this(new File(excelPath), new FileInputStream(new File(xmlFilePath)), limitFlow);
	}
	
	/**
	 * 
	 * 功能描述：处理xml文件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年3月15日 下午5:15:50</p>
	 *
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public List<Map<String, String>> handle(boolean agreement) throws Exception {
		this.parseXmlRule();
		
		Workbook wb = null;
		Sheet sheet = null;
		InputStream is = null;
		try {
			String filename = excelFile.getName();
			int index = filename.lastIndexOf(".");
			// 文件扩展名
            String extname = filename.substring(index+1);
            // 验证是否EXCEL文件
            Pattern p = Pattern.compile("xlsx||xls");
            boolean valid = p.matcher(extname.toLowerCase()).matches();
            if (!valid) {
            	throw new Exception("请按模板上传excel附件！");
            }
			long fileSize = excelFile.length();
        	if(limitFlow>0 && fileSize>limitFlow) {
        		throw new Exception("文件大小不允许超过"+limitFlow/(1024*1024)+"M！");
        	}
        	
			is = new FileInputStream(excelFile);
			wb = WorkbookFactory.create(is);
			sheet  = wb.getSheetAt(0);
			
			int len = sheet.getLastRowNum();
			if(len<=0) {
				throw new Exception("导入的文件没有数据，请检查！");						
			}
			if(limitSize>0) {
				if(StringUtils.isNotBlank(xmlRule.getTheme())) {
					limitSize = limitSize + 1;
				}
				if(len>limitSize) {
					throw new Exception("导入的记录条数不得超过"+limitSize+"条！");
				}
			}								
			List<Map<String, String>> result = foreachExcelRow(sheet, xmlRule);			
			return result;
		} catch(Exception e) {
			// e.printStackTrace();
			throw e;
		} finally {
			if(is!=null) {
				is.close();
			}
			if(wb!=null) {
				wb.close();
			}
		}
	}
	
	public <T> List<T> handle(Class<T> clazz) throws Exception {
		this.parseXmlRule();
		
		Workbook wb = null;
		Sheet sheet = null;
		InputStream is = null;
		try {
			String filename = excelFile.getName();
			int index = filename.lastIndexOf(".");
			// 文件扩展名
            String extname = filename.substring(index+1);
            // 验证是否EXCEL文件
            Pattern p = Pattern.compile("xlsx||xls");
            boolean valid = p.matcher(extname.toLowerCase()).matches();
            if (!valid) {
            	throw new Exception("请按模板上传excel附件！");
            }
            long fileSize = excelFile.length();
        	if(limitFlow>0 && fileSize>limitFlow) {
        		throw new Exception("文件大小不允许超过"+limitFlow/(1024*1024)+"M！");
        	}
        	
			is = new FileInputStream(excelFile);
			wb = WorkbookFactory.create(is);
			sheet  = wb.getSheetAt(0);
			
			int len = sheet.getLastRowNum();
			if(len<=0) {
				throw new Exception("导入的文件没有数据，请检查！");						
			}
			if(limitSize>0) {
				if(StringUtils.isNotBlank(xmlRule.getTheme())) {
					limitSize = limitSize + 1;
				}
				if(len>limitSize) {
					throw new Exception("导入的记录条数不得超过"+limitSize+"条！");
				}
			}								
			List<T> result = foreachExcelRow(sheet, xmlRule, clazz);			
			return result;
		} catch(Exception e) {
			// e.printStackTrace();
			throw e;
		} finally {
			if(is!=null) {
				is.close();
			}
			if(wb!=null) {
				wb.close();
			}
		}
	}		

	public File getExcelFile() {
		return excelFile;
	}

	public void setExcelFile(File excelFile) {
		this.excelFile = excelFile;
	}

	public static void main(String[] args) throws Exception {	
		String path = ExcelImport.class.getClassLoader().getResource("com/ai/common/emport/example/file.xml").getPath();
		String[] paths = path.split("target");
		String excelPath = paths[0] + "src/main/java/com/ai/common/emport/example/template.xlsx";
		String xmlPath = paths[0] + "src/main/java/com/ai/common/emport/example/file.xml";
		System.out.println(excelPath);
		ExcelImport emport = new ExcelImport(excelPath, xmlPath, 10);		
		/*List<Map<String, String>> result = emport.handle();
		System.out.println(JSON.toJSONString(emport.getFailMessages()));
		System.out.println("failTotal:"+emport.getFailTotal()+";successTotal:"+emport.getSuccessTotal());
		System.out.println("size:"+result.size());
		for(Map<String, String> dataMap : result) {
			System.out.println(emport.mapConvertObject(dataMap, TestUser.class));
		}*/
		List<TestUser> result = emport.handle(TestUser.class);
		System.out.println(JSON.toJSONString(emport.getRowFailMessageMap()));
		System.out.println("failTotal:"+emport.getFailTotal()+";successTotal:"+emport.getSuccessTotal());
		System.out.println("size:"+result.size());
		for(TestUser bean : result) {
			System.out.println(bean);
		}
	}
}
