/**
 * JobParserUtil.java	  V1.0   2020年10月16日 下午3:45:00
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.job.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ai.modules.engine.job.meta.BaseMeta;

public class JobParserUtil {
	
	public static BaseMeta parse(String[] args) {
		if(args==null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for(String arg : args) {
			sb.append(arg).append(" ");
		}
		sb.deleteCharAt(sb.length() - 1);
		
		String param = sb.toString();
		Map<String, String> params = parseArgs(param);
		BaseMeta meta = new BaseMeta(params);
		return meta;
	}
	
	public static Map<String, String> parseArgs(String param) {
		Map<String, String> result = new HashMap<String, String>();
		param = replaceMultiSpaceToOneSpace(param);
		String[] params = param.split(" ");
		
		for (int i = 0, len = params.length; i < params.length; i++) {
			String key = params[i];
			if(i+1>=len) {
				break;
			}
			String value = params[i+1];
			if(!value.startsWith("-")) {				
				if("-ds".equals(key)) {
					//solr数据源参数
					result.put("datasource", value);
				} else if("-serial".equals(key) || "-s".equals(key)) {
					//流水号参数
					result.put("serialNo", value);
				} else if("-f".equals(key)) {
					//任务处理程序
					result.put("func", value);
				}
				key = key.substring(1);
				result.put(key, value);
				i++;
			}			
		}
		
		return result;
	}
	
	/**
	 * 
	 * 功能描述：字符串中多个空格替换成一个空格
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月16日 下午3:46:58</p>
	 *
	 * @param str
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public static String replaceMultiSpaceToOneSpace(String str) {
	    Pattern pattern = Pattern.compile(" {2,}");
	    Matcher matcher = pattern.matcher(str);
	    String result = matcher.replaceAll(" ");

	    return result.trim();
	}
	
	public static void main(String[] args) throws Exception {
		StringBuilder sb = new StringBuilder();
		for(String arg : args) {
			sb.append(arg).append(" ");
		}
		sb.deleteCharAt(sb.length() - 1);
		
		String paramStr = sb.toString();
		paramStr = replaceMultiSpaceToOneSpace(paramStr);
		Map<String, String> params = parseArgs(paramStr);
		System.out.println(params);
	}
}
