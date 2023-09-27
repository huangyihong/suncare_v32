package com.ai.common.utils;

import java.util.UUID;

/**
 * @Auther: zhangpeng
 * @Date: 2019/11/22 15
 * @Description:
 */
public class IdUtils {
    public static String uuid(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
