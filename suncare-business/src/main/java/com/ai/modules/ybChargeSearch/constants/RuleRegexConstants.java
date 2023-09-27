/**
 * RuleRegexConstants.java	  V1.0   2023年2月16日 上午9:26:28
 *
 * Copyright (c) 2023 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.ybChargeSearch.constants;

import org.apache.commons.lang3.StringUtils;

public enum RuleRegexConstants {
	
	AGE(DcConstants.RULE_LIMIT_AGE, "(>|>=|<|<=|=)\\d+&&(\\(|\\[)\\d+,\\d+(\\)|\\])&&(>|>=|<|<=|=)\\d+,(>|>=|<|<=|=)\\d+", "年龄限制内容格式不符，格式如：=2|>2|>=2|<14|<=14|(2,18)|(0,28]|[15,60)|[6,14]|>=18,<=60"),
	DAYAGE(DcConstants.RULE_LIMIT_DAYAGE, "(>|>=|<|<=|=)\\d+&&(\\(|\\[)\\d+,\\d+(\\)|\\])&&(>|>=|<|<=|=)\\d+,(>|>=|<|<=|=)\\d+", "天龄限制内容格式不符，格式如：=2|>2|>=2|<14|<=14|(2,18)|(0,28]|[15,60)|[6,14]|>=18,<=60"),
	HOSPLEVEL(DcConstants.RULE_LIMIT_HOSPLEVEL, "(>|>=|<|<=|=)\\d+", "医院级别限制内容格式不符，格式如：>2|>=2|<2|<=2"),
	SEX(DcConstants.RULE_LIMIT_SEX, "(男|女)(性)?", "性别限制内容格式不符，取值范围（男，女，男性，女性）"),
	RANGE(DcConstants.RULE_LIMIT_RANGE, "(>|>=|<|<=|=)\\d+&&(\\(|\\[)\\d+,\\d+(\\)|\\])&&(>|>=|<|<=|=)\\d+,(>|>=|<|<=|=)\\d+", "限制内容格式不符，格式如：=2|>2|>=2|<14|<=14|(2,18)|(0,28]|[15,60)|[6,14]|>=18,<=60"),
	WEIGHT(DcConstants.RULE_LIMIT_WEIGHT, "(>|>=|<|<=|=)\\d+&&(\\(|\\[)\\d+,\\d+(\\)|\\])&&(>|>=|<|<=|=)\\d+,(>|>=|<|<=|=)\\d+", "出生体重限制内容格式不符，格式如：=2|>2|>=2|<14|<=14|(2,18)|(0,28]|[15,60)|[6,14]|>=18,<=60"),
	ZYDAYS(DcConstants.RULE_LIMIT_ZYDAYS, "(>|>=|<|<=|=)\\d+&&(\\(|\\[)\\d+,\\d+(\\)|\\])&&(>|>=|<|<=|=)\\d+,(>|>=|<|<=|=)\\d+", "住院天数限制内容格式不符，格式如：=2|>2|>=2|<14|<=14|(2,18)|(0,28]|[15,60)|[6,14]|>=18,<=60");

	private String code;
	private String regex;
	private String message;
	
	private RuleRegexConstants(String code, String regex, String message) {
		this.code = code;
		this.regex = regex;
		this.message = message;
	}
	
	public static RuleRegexConstants getByCode(String code){
    	if(StringUtils.isBlank(code)) {
    		return null;
    	}
        for(RuleRegexConstants bean : values()){
            if (bean.getCode().equals(code)){
                return bean;
            }
        }
        return null;
    }

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
