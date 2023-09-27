package com.ai.common.utils;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;


/**
 * saveName（保存名） 在类初始时自动创建
 *
 * @author songxiaoliang
 *
 */
public abstract class FileUploadHandler{
	private Log logger = LogFactory.getLog(FileUploadHandler.class);

	private MultipartFile file;

	private String originalName;	//原文件名
	private String suffixName;		//文件后缀
	private String saveName;		//保存名：时间戳+原文件后缀
	private String savePath;		//保存路径(包括保存名,如G:\nasapp\fjgs\transfer\text.jpg)

	public FileUploadHandler(MultipartFile file) {
		super();
		this.file = file;
		if(file!=null && !file.isEmpty()){
			originalName = file.getOriginalFilename();

			suffixName = getSuffixName(getOriginalName());
			saveName = System.currentTimeMillis() + "." + suffixName;
		}
	}

	public void upload(String saveDir)throws Exception {
		if(file==null || file.isEmpty()){
			throw new Exception("上传文件为空！");
		}
		else{
			//文件验证
			validate(file);
			try {
				FileUtil.creatFilePath(saveDir);
				savePath = FileUtil.joinPath(saveDir, saveName);
				logger.info("保存文件到："+savePath);
				file.transferTo(new File(savePath));
			} catch (IOException e) {
				e.printStackTrace();
				throw new Exception("保存文件失败："+savePath);
			}
		}
	}
	public void upload()throws Exception {
		if(file==null || file.isEmpty()){
			throw new Exception("上传文件为空！");
		}
		else{
			//文件验证
			validate(file);
			try {
				logger.info("保存文件到："+savePath);
				file.transferTo(new File(savePath));
			} catch (IOException e) {
				e.printStackTrace();
				throw new Exception( "保存文件失败！");
			}
		}
	}
	/**
	 * 获取文件后缀名
	 * @param fileName
	 * @return
	 */
	private String getSuffixName(String fileName){
		int lastPointIndex = fileName.lastIndexOf(".");
		if(lastPointIndex!=-1){
			return fileName.substring(lastPointIndex+1, fileName.length());
		}

		return "";
	}

	/**
	 * 文件验证
	 * @param file
	 */
	public abstract void validate(MultipartFile file);

	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("[originalName:").append(getOriginalName()).append("]");
		sb.append("[suffixName:").append(suffixName).append("]");
		sb.append("[saveName:").append(saveName).append("]");
		sb.append("[savePath:").append(savePath).append("]");
		return sb.toString();
	}

	public String getOriginalName() {
		return originalName;
	}

	public String getSaveName() {
		return saveName;
	}

	public void setSaveName(String saveName) {
		this.saveName = saveName;
	}

	public String getSavePath() {
		return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public String getSuffixName() {
		return suffixName;
	}
}

