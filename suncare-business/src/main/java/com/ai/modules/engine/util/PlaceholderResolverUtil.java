/**
 * PlaceholderResolver.java	  V1.0   2020年12月1日 下午7:55:24
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.util;

import java.util.Map;
import java.util.Properties;

import org.springframework.util.Assert;
import org.springframework.util.PropertyPlaceholderHelper;

public class PlaceholderResolverUtil {
	/**
     * 前缀占位符
     */
    public static final String PLACEHOLDER_PREFIX = "${";

    /**
     * 后缀占位符
     */
    public static final String PLACEHOLDER_SUFFIX = "}";
    
    /**
     * 单例解析器
     */
    private static PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper(PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX);
    
    private PlaceholderResolverUtil() {
    	
    }
    
    public static String replacePlaceholders(String text, final Map<String, String> params) {
		Assert.notNull(params, "params must not be null");
		return helper.replacePlaceholders(text, params::get);
	}
    
    public static String replacePlaceholders(String text, Properties properties) {
        return helper.replacePlaceholders(text, properties);
    }
}
