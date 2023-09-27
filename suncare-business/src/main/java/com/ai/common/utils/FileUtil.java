package com.ai.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.*;

//import com.linewell.core.util.BufferedReader;
//import com.linewell.core.util.FileReader;

/**
 * 文件操作相关的函数
 * @author songxiaoliang
 */

public class FileUtil
{

    /**
     * 构造输入的两个路径片断为组合路径
     *
     * @param s1
     * @param s2
     * @return
     */
    public static final String joinPath(String s1, String s2)
    {
        File f = new File(s1, s2);
        return f.getPath().replaceAll("\\\\", "/");
    }

    /**
     * 以byteArray的形式读文件的全部内容
     *
     * @param path
     * @return
     * @throws Exception
     */
    public static final byte[] readFileAsByteArray(String path) throws Exception
    {
        File file = new File(path);
        return readFileAsByteArray(file);
    }

    /**
     * 以byteArray的形式读文件的全部内容
     *
     * @return
     * @throws Exception
     */
    public static final byte[] readFileAsByteArray( File file) throws Exception
    {
        FileInputStream fi = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        try
        {
            fi.read(data);
        }
        finally
        {
            fi.close();
        }
        return data;
    }

    /**
     * 以byteArray的形式写入文件
     *
     * @param path
     * @param data
     * @return
     * @throws Exception
     */
    public static final void writeByteArrayToFile(String path, byte[] data) throws Exception
    {
        File file = new File(path);
        FileOutputStream fo = new FileOutputStream(file);
        try
        {
            fo.write(data);
        }
        finally
        {
            fo.close();
        }
    }

    /**
     * 创建文件夹路径
     *
     * @param filePath
     */
    public static void creatFilePath(String filePath)
    {
        File file = new File(filePath);
        if (!file.exists())
        {
            file.mkdirs();
        }
    }

    /**
     * 拷贝文件
     *
     */
    public static void copyFile(File sourceFile, File targetFile)throws IOException{
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            // 新建文件输入流并对它进行缓冲
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

            // 新建文件输出流并对它进行缓冲
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

            // 缓冲数组
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            outBuff.flush();
        } finally {
            // 关闭流
            if (inBuff != null){
                inBuff.close();
            }
            if (outBuff != null){
                outBuff.close();
            }

        }
    }

    /**
	 * 获取文件后缀名
	 * @param fileName
	 * @return
	 */
	public static String getSuffixName(String fileName){
		int lastPointIndex = fileName.lastIndexOf(".");
		if(lastPointIndex!=-1){
			return fileName.substring(lastPointIndex+1, fileName.length());
		}

		return "";
	}

	public static String getFileName(String filePath){
		if(StringUtils.isNotEmpty(filePath)){
			String name = filePath.replaceAll("\\\\", "/");
			if(name.indexOf("/")!=-1){
				return name.substring(name.lastIndexOf("/")+1);
			}
		}
		return filePath;
	}


	 /**
     * 读取文件的内容，结果是个字符串
     *
     * @param filePath 文件的地址
     */
    public static String fileToString(String filePath) {
        String result = "";
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(filePath);
            br = new BufferedReader(fr);
            String Line = br.readLine();

            while (Line != null) {
                result += Line + "\n";
                Line = br.readLine();
            }
            br.close();
            fr.close();
        } catch (IOException e) {
        	//logger.debug("读取文件内容失败，文件的地址为：[" + filePath + "]", e);
        	e.printStackTrace();
        } finally {
			try {
				if (fr != null) {
					fr.close();
					fr = null;
				}
				if (null != br) {
					br.close();
					br = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        return result;
    }


    /**
     * 文本文件转换为指定编码的字符串
     *
     * @param file 文本文件
     * @param encoding 编码类型
     * @return 转换后的字符串
     * @throws IOException
     */
    public static String fileToString(File file, String encoding) {
    	String result = null;
		InputStreamReader reader = null;
		StringWriter writer = new StringWriter();
		try {
			if (encoding == null || "".equals(encoding.trim())) {
				reader = new InputStreamReader(new FileInputStream(file));
			} else {
				reader = new InputStreamReader(new FileInputStream(file), encoding);
			}

			//将输入流写入输出流
			char[] buffer = new char[1024];
			int n = 0;
			while (-1 != (n = reader.read(buffer))) {
				writer.write(buffer, 0, n);
			}
			result = writer.toString();//返回转换结果
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (null != reader){
					reader.close();
					reader = null;
				}
				if (null != writer){
					writer.close();
					writer = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

    public static void main(String[] args)throws Exception
    {
//    	copyFile(new File("G:/nasapp/attachment/temp/1385552944906.xls"), new File("G:/nasapp/attachment/1385552944906.xls"));
    	System.out.println(getFileName("G:/nasapp/attachment/temp/1385552944906.xls"));
    }
}
