package com.ai.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileZip {
    /**
     * @param inputFileName 你要压缩的文件夹(整个完整路径)
     * @param zipFileName 压缩后的文件(整个完整路径)
     * @throws Exception
     */
    public static Boolean zip(String inputFileName, String zipFileName) throws Exception {
        zip(zipFileName, new File(inputFileName));
        return true;
    }

    private static void zip(String zipFileName, File inputFile)  throws Exception{
        ZipOutputStream out = null;
        try{
            out = new ZipOutputStream(new FileOutputStream(zipFileName));
            zip(out, inputFile, "");
            out.flush();
            out.close();
        }catch (Exception e){
            throw new Exception("zip error",e);
        }finally {
            try{
                if(out!=null){
                    out.close();
                }
            }catch (Exception e){
                throw new Exception("zip error",e);
            }
        }


    }

    public static void zip(String zipFileName, List<String> files, String base)  throws Exception{
        ZipOutputStream out = null;
        try{
            out = new ZipOutputStream(new FileOutputStream(zipFileName));
            out.putNextEntry(new ZipEntry(base + "/"));
            base = base.length() == 0 ? "" : base + "/";
            for (int i = 0; i < files.size(); i++) {
                File inputFile = new File(files.get(i));
                zip(out, inputFile, base + inputFile.getName());
            }
            out.flush();
            out.close();
        }catch (Exception e){
            throw new Exception("zip error",e);
        }finally {
            try{
                if(out!=null){
                    out.close();
                }
            }catch (Exception e){
                throw new Exception("zip error",e);
            }
        }


    }

    private static void zip(ZipOutputStream out, File f, String base) throws Exception {
        if (f.isDirectory()) {
            File[] fl = f.listFiles();
            out.putNextEntry(new ZipEntry(base + "/"));
            base = base.length() == 0 ? "" : base + "/";
            for (int i = 0; i < fl.length; i++) {
                zip(out, fl[i], base + fl[i].getName());
            }
        } else {
            ZipEntry zipEntry = new ZipEntry(f.getName());
            out.putNextEntry(zipEntry);
            FileInputStream in = new FileInputStream(f);
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            in.close();
        }
    }

    public static void main(String [] temp){
        try {
            //zip("D:\\home\\devbigdata\\files\\excel\\99b9336f56d541649712c88cf5ac2df0.csv","D:\\home\\devbigdata\\files\\excel\\test.zip");//你要压缩的文件夹      和  压缩后的文件
           List<String> list = new ArrayList();
            list.add("E:\\ASIAProject\\suncare_v3\\upFiles\\fjTemplate\\20230203\\1_1675410245119.jpg");
            list.add("E:\\ASIAProject\\suncare_v3\\upFiles\\fjTemplate\\20230203\\企业年审数据_1675407233310.xls");
            list.add("E:\\ASIAProject\\suncare_v3\\upFiles\\fjTemplate\\20230203\\3.webp_1675414543124.webp.jpg");
            zip("E:\\ASIAProject\\suncare_v3\\upFiles\\fjTemplate\\20230203\\test.zip",list,"");//你要压缩的文件夹

        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
