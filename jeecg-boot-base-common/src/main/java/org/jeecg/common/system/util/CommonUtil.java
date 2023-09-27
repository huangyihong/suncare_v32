package org.jeecg.common.system.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Auther: zhangpeng
 * @Date: 2020/4/10 10
 * @Description:
 */
@Component
public class CommonUtil {
	//文件上传扩展名白名单
    private static String[] VALID_FILE_TYPES = {"xls","xlsx","doc","docx","pdf","jpg","jpeg","png" ,"gif","bmp"};

    public static String UPLOAD_PATH;

    @Value(value = "${jeecg.path.upload}")
    public void setUploadPath(String path) {
        UPLOAD_PATH = path;
    }

    private static SimpleDateFormat daySdf = new SimpleDateFormat("yyyyMMdd");
    public static String upload(MultipartFile mf,String bizPath) throws IOException {
        String fileName;
        if(StringUtils.isBlank(bizPath)){
            bizPath = "files";
        }
        String nowday = daySdf.format(new Date());
        File file = new File(UPLOAD_PATH + File.separator + bizPath + File.separator + nowday);
        if (!file.exists()) {
            file.mkdirs();// 创建文件根目录
        }
        // 获取文件名
        String originalFilename = mf.getOriginalFilename();
        //文件名去除空格
        originalFilename = originalFilename.replaceAll("\\s*","");

        //判断文件扩展名是否符合白名单要求
        if(isValidFileType(originalFilename) == false) {
        	throw new IOException("文件扩展名不符合要求!");
        }


        //防止文件名过长攻击
        if(originalFilename.length()>50) {
        	int index = originalFilename.lastIndexOf(".");
        	String prex="";
        	if(index>0) {
        		prex = originalFilename.substring(index);
        	}
        	originalFilename=originalFilename.substring(0,50) + prex;
        }
        fileName = originalFilename.substring(0, originalFilename.lastIndexOf(".")) + "_" + System.currentTimeMillis() + originalFilename.substring(originalFilename.indexOf("."));


        String savePath = file.getPath() + File.separator + fileName;
        File savefile = new File(savePath);
        FileCopyUtils.copy(mf.getBytes(), savefile);
        String dbpath = bizPath + File.separator + nowday + File.separator + fileName;
        if (dbpath.contains("\\")) {
            dbpath = dbpath.replace("\\", "/");
        }
        return dbpath;

    }

    /**
     * 判断文件名是否是合法的扩展名
     * @param fileName
     * @return
     */
    private static boolean isValidFileType(String fileName) {
    	if(fileName == null) {
    		return false;
    	}

    	//获取文件扩展名
    	String tempFileName = fileName.trim().toLowerCase();
    	int index = tempFileName.lastIndexOf(".");

    	//如果没有扩展名则返回false
    	if(index <0) {
    		return false;
    	}

    	String fileType = tempFileName.substring(index+1);

    	//循环判断白名单，符合范围的返回true
    	for(String type :VALID_FILE_TYPES) {
    		if(type.equals(fileType)) {
    			return true;
    		}
    	}


    	return false;
    }

    /**
     * 拥有时间戳的文件路径还原文件名
     * @param dbPath
     * @return
     */
    public static String pathToFileName(String dbPath){
        String name = dbPath.substring(dbPath.lastIndexOf("/") + 1);
        return name.substring(0,name.lastIndexOf("_")) + name.substring(name.lastIndexOf("."));

    }


}
