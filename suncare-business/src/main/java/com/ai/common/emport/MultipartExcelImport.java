/**
 * MultipartExcelImport.java	  V1.0   2018年12月1日 下午5:40:47
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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;

public class MultipartExcelImport extends AbstractExcelImport {
	/** excel文件*/
	protected MultipartFile multipartFile;	
	
	public MultipartExcelImport(MultipartFile multipartFile, InputStream xmlInputStream, Integer limitSize) {
		this.multipartFile = multipartFile;
		this.xmlInputStream = xmlInputStream;
		this.limitSize = limitSize;
		this.limitFlow = 0L;
	}
	
	public MultipartExcelImport(MultipartFile multipartFile, InputStream xmlInputStream, Long limitFlow) {
		this.multipartFile = multipartFile;
		this.xmlInputStream = xmlInputStream;
		this.limitFlow = limitFlow;
		this.limitSize = 0;
	}

	public MultipartExcelImport(MultipartFile multipartFile, String xmlFilePath, int limitSize) throws FileNotFoundException {
		this(multipartFile, new FileInputStream(new File(xmlFilePath)), limitSize);
	}

	public MultipartExcelImport(MultipartFile multipartFile, String xmlFilePath, long limitFlow) throws FileNotFoundException {
		this(multipartFile, new FileInputStream(new File(xmlFilePath)), limitFlow);
	}
	
	public List<Map<String, String>> handle(boolean agreement) throws Exception {
		this.parseXmlRule();
		
		Workbook wb = null;
		Sheet sheet = null;
		InputStream is = null;
		try {
			String filename = multipartFile.getOriginalFilename();
			int index = filename.lastIndexOf(".");
			// 文件扩展名
            String extname = filename.substring(index+1);
            // 验证是否EXCEL文件
            Pattern p = Pattern.compile("xlsx||xls");
            boolean valid = p.matcher(extname.toLowerCase()).matches();
            if (!valid) {
            	throw new Exception("请按模板上传excel附件！");
            }
			long fileSize = multipartFile.getSize();
        	if(limitFlow>0 && fileSize>limitFlow) {
        		throw new Exception("文件大小不允许超过"+limitFlow/(1024*1024)+"M！");
        	}
        	
			is = multipartFile.getInputStream();
			//ZipSecureFile.setMinInflateRatio(-1.0d);
			wb = WorkbookFactory.create(is);
			String sheetName = this.getXmlRule().getSheetName();
			if(StringUtils.isNotBlank(sheetName)) {
				sheet = wb.getSheet(sheetName);
			} else {
				sheet  = wb.getSheetAt(0);
			}
			if(sheet==null) {
				sheet  = wb.getSheetAt(0);
			}
			
			int len = sheet.getLastRowNum();
			if(len<=0) {
				throw new Exception("导入的文件没有数据，请检查！");						
			}
			if(limitSize>0) {
				limitSize = limitSize + xmlRule.getStart() + xmlRule.getOffset();
			}								
			if(agreement) {
				return foreachExcelRow(sheet, xmlRule);			
			} else {
				return foreachExcelRowAutoMapping(sheet, xmlRule);
			}
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
			String filename = multipartFile.getOriginalFilename();
			int index = filename.lastIndexOf(".");
			// 文件扩展名
            String extname = filename.substring(index+1);
            // 验证是否EXCEL文件
            Pattern p = Pattern.compile("xlsx||xls");
            boolean valid = p.matcher(extname.toLowerCase()).matches();
            if (!valid) {
            	throw new Exception("请按模板上传excel附件！");
            }
            long fileSize = multipartFile.getSize();
        	if(limitFlow>0 && fileSize>limitFlow) {
        		throw new Exception("文件大小不允许超过"+limitFlow/(1024*1024)+"M！");
        	}
        	
			is = multipartFile.getInputStream();
			//ZipSecureFile.setMinInflateRatio(-1.0d);
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
	
	public void exception() throws Exception {
		if(this.getFailTotal()>0) {
			StringBuilder sb = new StringBuilder();
			sb.append("本次导入");
			if(StringUtils.isNotBlank(xmlRule.getSheetName())) {
				sb.append("（sheet=").append(xmlRule.getSheetName()).append("）");
			}
			sb.append("：总记录数(").append(this.getTotal()).append(")");
			sb.append("，校验失败数(").append(this.getFailTotal()).append(")./n");
			if(this.getRowFailMessageMap()!=null) {
				for(Map.Entry<Integer, RowFailMessage> entry : this.getRowFailMessageMap().entrySet()) {
					sb.append("第").append(entry.getKey()).append("行：");
					RowFailMessage messages = entry.getValue();
					for(String msg : messages.getRowMessages()) {
						sb.append("[").append(msg).append("]，");
					}
					sb.append("/n");
				}
				
			}
			String message = sb.toString();
			if (message.length() > 4000) {
				message = message.substring(0, 4000) + "...";
			}
			throw new Exception(message);
		}
	}

	public MultipartFile getMultipartFile() {
		return multipartFile;
	}

	public void setMultipartFile(MultipartFile multipartFile) {
		this.multipartFile = multipartFile;
	}	
}
