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
public class ImgDownloadHandler {
	private Log logger = LogFactory.getLog(ImgDownloadHandler.class);

	/**
	 * 写文件到本地
	 *
	 */
	public void download(HttpServletResponse response, String filePath) throws Exception {
		FileInputStream inputStream = null;
		ServletOutputStream outputStream = null;
		try {
			inputStream = new FileInputStream(filePath);
			response.reset();
			response.setContentType("image/jpeg");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);

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
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new Exception("下载图片失败："+filePath);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new Exception("下载图片失败："+filePath);
		}
		finally{
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

	public void download(HttpServletResponse response, InputStream inputStream) throws Exception {
		if(inputStream==null){
			throw new Exception("输入流为空！");
		}
		ServletOutputStream outputStream = null;
		try {
			response.reset();
			response.setContentType("image/jpeg");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);

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
			throw new Exception("下载图片失败！");
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

	public void download(HttpServletResponse response, byte[] bytes) throws Exception {
		if(bytes==null){
			throw new Exception("输入字节为空！");
		}
		ServletOutputStream outputStream = null;
		try {
			response.reset();
			response.setContentType("image/jpeg");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);

			outputStream = response.getOutputStream();

			outputStream.write(bytes);

			outputStream.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new Exception("下载图片失败！");
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new Exception("下载图片失败！");
		}
		finally{
			if(outputStream!=null){
				try {
					outputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}

