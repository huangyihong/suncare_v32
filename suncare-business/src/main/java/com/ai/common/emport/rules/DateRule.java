/**
 * RequiredRule.java	  V1.0   2018年11月25日 下午3:10:31
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.common.emport.rules;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.DateUtils;

/**
 * 
 * 功能描述：日期格式限制
 *
 * @author  zhangly
 */
public class DateRule extends BaseRule {
	private String format;
	
	public DateRule() {
		
	}
		
	public DateRule(String title, String format) {
		this.title = title;
		this.format = format;
	}

	@Override
	public RuleMessage validator() {
		if(StringUtils.isNotBlank(value)) {
			String regex = "[^y|Y|m|M|d|D|h|H|s|S]+";
			Pattern p = Pattern.compile(regex);
			//是否严格模式，严格模式包含-/年月日特殊、中文等字符，如：yyyy-MM-dd
			boolean strict = p.matcher(format).find();
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			try {
				if(!strict) {
					//剔除非数字的字符
					regex = "[^0-9]+";				
					value = value.replaceAll(regex, "");
				}
				//sdf.setLenient(false);
				sdf.parse(value);
			} catch (Exception e) {
				return RuleMessage.newFail(String.format("%s不是有效的日期或不是正确的日期格式（%s）；", title, format));
			}
		}
		return RuleMessage.newOk();
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
	public static void main(String[] args) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			//sdf.setLenient(false);
			Date date = sdf.parse("2018-11-35");
			System.out.println(DateUtils.formatDate(date, "yyyy-MM-dd"));
		} catch (ParseException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
}
