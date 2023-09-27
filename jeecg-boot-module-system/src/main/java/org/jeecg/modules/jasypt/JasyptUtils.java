/**
 * JasyptUtils.java	  V1.0   2022年7月5日 上午10:53:10
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package org.jeecg.modules.jasypt;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

public class JasyptUtils {
	
	private static final String ALGORITHM = "PBEWITHHMACSHA512ANDAES_256";
	private static final String PASSWORD = "Asiainfo@202207.ybfk";

	/**
     * Jasypt生成加密结果
     *
     * @param value 待加密值
     * @return
     */
    public static String encrypt(String value) {
    	// 1. 创建加解密工具实例
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        // 2. 加解密配置
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(PASSWORD);
        config.setAlgorithm(ALGORITHM);
        config.setKeyObtentionIterations( "1000");
        config.setPoolSize("1");
        encryptor.setConfig(config);
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        // 3. 加密
        return encryptor.encrypt(value);
    }
 
    /**
     * 解密
     *
     * @param value 待解密密文
     * @return
     */
    public static String decrypt(String value) {
    	// 1. 创建加解密工具实例
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        // 2. 加解密配置
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(PASSWORD);
        config.setAlgorithm(ALGORITHM);
        config.setKeyObtentionIterations( "1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        // 3. 解密
        return encryptor.decrypt(value);
    }
    
    public static void main(String[] args) throws Exception {
    	String value = "A20220628eO0E0dfu38d3iS105";
    	System.out.println("加密前："+value);
    	String encrypt = encrypt(value);
    	System.out.println("加密后："+encrypt);
    	System.out.println("解密后："+decrypt(encrypt));
    }
}
