/**
 * BeanUtil.java	  V1.0   2023年1月31日 下午5:12:43
 *
 * Copyright (c) 2023 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.core.lang.Editor;
import cn.hutool.core.util.StrUtil;

public class BeanUtil extends cn.hutool.core.bean.BeanUtil {

	/**
	 * 
	 * 功能描述：将对象转化为map
	 *
	 * @author  zhangly
	 *
	 * @param <T>
	 * @param bean
	 * @param upper
	 * @return
	 */
    public static <T> Map<String, Object> beanToMap(T bean, boolean upper) {
    	if (bean == null) {
			return null;
		}
    	Map<String, Object> targetMap = new HashMap<String, Object>();
		return beanToMap(bean, targetMap, true, new Editor<String>() {
			@Override
			public String edit(String key) {
				key = StrUtil.toUnderlineCase(key);
				return upper ? key.toUpperCase() : key;
			}
		});
    }

    /**
     * 
     * 功能描述：将List<JavaBean>转化为List<Map<String, Object>>
     *
     * @author  zhangly
     *
     * @param <T>
     * @param objList
     * @param upper
     * @return
     */
    public static <T> List<Map<String, Object>> objectsToMaps(List<T> objList, boolean upper) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (objList != null && objList.size() > 0) {
            Map<String, Object> map = null;
            T bean = null;
            for (int i = 0, size = objList.size(); i < size; i++) {
                bean = objList.get(i);
                map = beanToMap(bean, upper);
                list.add(map);
            }
        }
        return list;
    }
    
    public static <T> List<Map<String, Object>> objectsToMaps(List<T> objList) {
    	return objectsToMaps(objList, true);
    }
}
