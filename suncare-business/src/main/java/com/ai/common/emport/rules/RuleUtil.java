/**
 * RuleUtil.java	  V1.0   2018年11月28日 下午4:34:27
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.common.emport.rules;

import java.util.regex.Pattern;

public class RuleUtil {
	/**
	 * 
	 * 功能描述：是否整数
	 *
	 * @author  zhangly
	 */
	public static boolean isDigits(String value) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");  
        return pattern.matcher(value).matches(); 
	}
	
	/**
	 * 
	 * 功能描述：是否数字，包含小数
	 *
	 * @author  zhangly
	 */
	public static boolean isNumber(String value) {
		Pattern pattern = Pattern.compile("^[-\\+]?\\d+(\\.\\d+)?$");  
        return pattern.matcher(value).matches(); 
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(RuleUtil.isNumber("3.23"));
	}
}
