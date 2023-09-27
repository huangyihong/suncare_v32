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
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.web.multipart.MultipartFile;

public class MultipartCsvImport extends AbstractCsvImport {
	/** excel文件*/
	private MultipartFile multipartFile;	
	
	public MultipartCsvImport(MultipartFile multipartFile, InputStream xmlInputStream, long limitFlow) {
		this.multipartFile = multipartFile;
		this.xmlInputStream = xmlInputStream;
		this.limitFlow = limitFlow;
	}
	
	public List<Map<String, String>> handle(boolean agreement) throws Exception {
		this.parseXmlRule();
		
		String filename = multipartFile.getOriginalFilename();
		int index = filename.lastIndexOf(".");
		// 文件扩展名
        String extname = filename.substring(index+1);
        // 验证是否csv文件
        Pattern p = Pattern.compile("csv");
        boolean valid = p.matcher(extname.toLowerCase()).matches();
        if (!valid) {
        	throw new Exception("请按模板上传csv附件！");
        }
		long fileSize = multipartFile.getSize();
    	if(limitFlow>0 && fileSize>limitFlow) {
    		throw new Exception("文件大小不允许超过"+limitFlow/(1024*1024)+"M！");
    	}
    	
		if(agreement) {
			return foreachCsvRow(multipartFile.getInputStream(), xmlRule);			
		} else {
			return foreachCsvRowAutoMapping(multipartFile.getInputStream(), xmlRule);
		}
	}
	
	public <T> List<T> handle(Class<T> clazz) throws Exception {
		this.parseXmlRule();
		
		String filename = multipartFile.getOriginalFilename();
		int index = filename.lastIndexOf(".");
		// 文件扩展名
        String extname = filename.substring(index+1);
        // 验证是否EXCEL文件
        Pattern p = Pattern.compile("csv");
        boolean valid = p.matcher(extname.toLowerCase()).matches();
        if (!valid) {
        	throw new Exception("请按模板上传csv附件！");
        }
        long fileSize = multipartFile.getSize();
    	if(limitFlow>0 && fileSize>limitFlow) {
    		throw new Exception("文件大小不允许超过"+limitFlow/(1024*1024)+"M！");
    	}							
		List<T> result = foreachCsvRow(multipartFile.getInputStream(), xmlRule, clazz);			
		return result;
	}
	
	public void exception() throws Exception {
		if(this.getFailTotal()>0) {
			StringBuilder sb = new StringBuilder();
			sb.append("本次导入：总记录数(").append(this.getTotal()).append(")");
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
			throw new Exception(sb.toString());
		}
	}

	public MultipartFile getMultipartFile() {
		return multipartFile;
	}

	public void setMultipartFile(MultipartFile multipartFile) {
		this.multipartFile = multipartFile;
	}	
}
