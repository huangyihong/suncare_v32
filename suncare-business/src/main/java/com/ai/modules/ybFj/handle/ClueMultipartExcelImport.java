/**
 * MultipartExcelImport.java	  V1.0   2018年12月1日 下午5:40:47
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.ybFj.handle;

import com.ai.common.emport.AbstractExcelImport;
import com.ai.common.emport.MultipartExcelImport;
import com.ai.common.emport.RowFailMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ClueMultipartExcelImport extends MultipartExcelImport {

	public ClueMultipartExcelImport(MultipartFile multipartFile, InputStream xmlInputStream, Long limitFlow) {
		super(multipartFile, xmlInputStream, limitFlow);
	}

	public List<Map<String, String>> handleClue(String sheetName) throws Exception {
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
			sheet = wb.getSheet(sheetName);
			if(sheet==null) {
				int idx = "线索汇总表".equals(sheetName) ? 0 : 1;
				sheet = wb.getSheetAt(idx);
			}
			if(sheet==null) {
				throw new Exception("请按模板上传excel附件！");
			}
			int len = sheet.getLastRowNum();
			if(len<=0) {
				return null;
			}
			return foreachExcelRowAutoMapping(sheet, xmlRule);
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
}
