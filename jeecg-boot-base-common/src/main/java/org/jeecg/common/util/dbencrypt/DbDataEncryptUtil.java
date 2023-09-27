package org.jeecg.common.util.dbencrypt;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.encryption.AesEncryptUtil;

import java.sql.SQLException;

public class DbDataEncryptUtil {
    public final static String factor1 = "ybfk.0601.AES!@$";
    private final static String prefix1 = "O1:";

    /**
     * 对数据库存储的数据进行加密
     * @param originStr
     * @return
     * @throws SQLException
     */
    public static String dbDataEncryptString(String originStr)  {
        //如果数据为空，则直接返回
        if (StringUtils.isBlank(originStr)){
            return  originStr;
        }


        //如果数据是加密格式，则直接返回
        if(originStr.startsWith(prefix1) == true){
            return originStr;
        }

        //加密，前缀用O1:
        return prefix1 + AesEncryptUtil.encrypt(originStr, factor1);
    }

    /**
     * 解密数据库加密存储的字段
     * @param encryptStr
     * @return
     * @throws SQLException
     */
    public static String dbDataDecryptString(String encryptStr)  {
        //如果数据为空，则直接返回
        if (StringUtils.isBlank(encryptStr)){
            return  encryptStr;
        }

        //如果数据不是加密格式，则直接返回
        if(encryptStr.startsWith(prefix1) == false){
            return encryptStr;
        }

        //去除前缀
        encryptStr = encryptStr.substring(prefix1.length());

        //解密并返回
        String decrypt = AesEncryptUtil.decrypt(encryptStr, factor1);
        return decrypt;
    }
    
    public static String decryptFunc(String field) {
    	String format = "program_decrypt(%s, '%s')";
    	return String.format(format, field, factor1);
    }
}
