package com.ai.common.utils;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


/**
 *
 * @author songxiaoliang
 *
 */
public class FileDownloadHandler{
	private Log logger = LogFactory.getLog(FileDownloadHandler.class);

	/**
	 * 写文件到本地
	 *
	 * @param fileName
	 */
	public void download(HttpServletResponse response, String fileName, String filePath) throws Exception {
		FileInputStream inputStream = null;
		ServletOutputStream outputStream = null;
		try {
			String realFileName = java.net.URLEncoder.encode(fileName,"UTF-8");
			inputStream = new FileInputStream(filePath);
//			response.reset();
			response.setContentType("application/x-download");
			response.setHeader("Content-Disposition", "attachment;filename=\"" + realFileName + "\"");

			outputStream = response.getOutputStream();
			byte[] b = new byte[1024];
			int i = 0;

			while((i = inputStream.read(b)) > 0){
				outputStream.write(b, 0, i);
			}

			inputStream.close();
			outputStream.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new Exception("文件无法找到："+filePath);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("下载文件失败："+filePath);
		} finally{
			if(inputStream!=null){
				try {
					inputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(outputStream!=null){
				try {
					outputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void download(HttpServletResponse response, InputStream inputStream, String fileName) throws Exception {
		if(inputStream==null){
			throw new Exception("输入流为空！");
		}
		ServletOutputStream outputStream = null;
		try {
			String realFileName = java.net.URLEncoder.encode(fileName,"UTF-8");
			response.reset();
			response.setContentType("application/x-download");
			response.setHeader("Content-Disposition", "attachment;filename=\"" + realFileName + "\"");

			outputStream = response.getOutputStream();
			byte[] b = new byte[1024];
			int i = 0;

			while((i = inputStream.read(b)) > 0){
				outputStream.write(b, 0, i);
			}

			inputStream.close();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("下载文件失败！");
		} finally{
			try {
				inputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(outputStream!=null){
				try {
					outputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void download(HttpServletResponse response, byte[] bytes, String fileName) throws Exception {
		if(bytes==null) {
			throw new Exception("文件字节为空！");
		}
		ServletOutputStream outputStream = null;
		try {
			String realFileName = java.net.URLEncoder.encode(fileName,"UTF-8");
			response.reset();
			response.setContentType("application/x-download");
			response.setHeader("Content-Disposition", "attachment;filename=\"" + realFileName + "\"");

			outputStream = response.getOutputStream();

			outputStream.write(bytes);

			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("下载文件失败！");
		} finally{
			if(outputStream!=null){
				try {
					outputStream.close();
					outputStream = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}

