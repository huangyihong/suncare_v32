package com.ai.modules.engine.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ObjectCacheWithFile {
    
    private static Logger logger = LoggerFactory.getLogger(ObjectCacheWithFile.class);
    private final static String cacheDir = "object_cache/";
    private final static String tmpCacheDir = "tmp_dir/";
    
    
    /**
     * 从文件中获取对象
     * 
     * @param cacheName
     * @return
     * @throws Exception
     */
    public static Object getObjectFromFile(String cacheType, String cacheName ,long expireSeconds) {
    	ObjectInputStream objInputStream = null;
        File f;
        try {
            f = new File(getCacheFileName(cacheType, cacheName));
            if (f.exists() == false) {
                return null;
            }

            // 判断缓存文件是否过期，如果过期，则返回空，如果expireSeconds小于等于0，则不判断
            if (expireSeconds >0 && (System.currentTimeMillis() - f.lastModified() > 1000 * expireSeconds)) {
                return null;
            }
            
            objInputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
            
            return   objInputStream.readUnshared();
        } catch (Exception e) {
            logger.error("", e);
            return null;
        } finally {
            f = null;
            try {
                if (objInputStream != null) {
                    objInputStream.close();
                }
            } catch (Exception e) {
            }
        }
    	
    }
    
   
    
    /**
     * 保存对象到文件中
     * @param cacheType
     * @param cacheName
     * @param obj
     */
    public static void saveObjectToFile(String cacheType, String cacheName, Object obj ) {

        
        ObjectOutputStream objOutputStream = null;
        File tempFile;
        File formFile;
        try {
            
            // 将文件写入临时文件
            /**
             * 需要保存临时文件与缓存目录必须在同一个文件系统下；
             * 所以，需要修改临时文件夹为：cacheDir目录下；
             */
            tempFile = new File( tmpCacheDir + getTmpFileName());
            if (!tempFile.getParentFile().exists()) {
                tempFile.getParentFile().mkdirs();
                // 第一次创建文件夹，需要等待100毫秒，以便后续缓存能创建成功
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
            }
            
            
            objOutputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
                    tempFile)));
            
            // 存入对象
            objOutputStream.writeObject(obj);
            objOutputStream.flush();
            objOutputStream.close();
            
            // 创建正式缓存文件对象
            formFile = new File(getCacheFileName(cacheType, cacheName));
            
            
            // 判断缓存文件夹是否存在，如果不存在，则创建文件夹
            if (formFile.getParentFile().exists() == false) {
                formFile.getParentFile().mkdirs();
                // 第一次创建文件夹，需要等待100毫秒，以便后续缓存能创建成功
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
                
            }
            //先删除临时对象
            if(formFile.exists()){
            	formFile.delete();
            }
            
            // 将缓存文件从临时文件命名为正式文件
            tempFile.renameTo(formFile);
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            tempFile = null;
            formFile = null;
            try {
                if (objOutputStream != null) {
                    objOutputStream.close();
                }
            } catch (Exception e) {
            }
        }
    }
    
    
    /**
     * 获取缓存文件名
     * 
     * @param cacheName
     * @param type
     * @return
     * @throws Exception
     */
    private static String getCacheFileName(String cacheType, String cacheName )
        throws Exception {
        return cacheDir + cacheType + "/" + cacheName;
    }
    
  
   
    
    public static void main(String[] args) {

        try {
             
            String cacheName = "testcache";
           
            long size = 0;
            
            ArrayList tempList = new ArrayList();
            
            for (int i = 0; i < 64; i++) {
                HashMap map = new HashMap();
                tempList.add(map);
                
                for (int j = 0; j < 5; j++) {
                    map.put(i + "_" + j, i + "_" + j + "_dfdddfffffff");
                    
                    size = size + (i + "_" + j).length();
                    size = size + (i + "_" + j + "_dfdddfffffff").length();
                }
            }
            
            String typeName="facet";
            
            long s1 = System.currentTimeMillis();
            saveObjectToFile(typeName, cacheName, tempList);
            long e1 = System.currentTimeMillis();
            
            System.out.println("save:" + (e1 - s1));
            
            long start = System.currentTimeMillis();
            Object result = getObjectFromFile(typeName, cacheName,0);
            long end = System.currentTimeMillis();
            System.out.println("read:" + (end - start));
            
            ArrayList re = (ArrayList) result;
            
            System.out.println(re);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    //获取当前线程的临时文件编号
    private static ThreadLocal<String> threadLocal_FileName = new ThreadLocal<String>();
	private static  AtomicLong numberLong =new AtomicLong();
	
	//获取临时文件名
	public static String getTmpFileName () throws Exception{
		String tmpFileName = (String)threadLocal_FileName.get();
		
		if(tmpFileName == null){
			int random =(int) Math.random() *1000;
			tmpFileName = "TempFile_" + random+"_" + numberLong.incrementAndGet() ;
			threadLocal_FileName.set(tmpFileName);
		}
		
		return tmpFileName;
	}
}
