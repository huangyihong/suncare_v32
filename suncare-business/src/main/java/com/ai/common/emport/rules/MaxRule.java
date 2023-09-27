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

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
/**
 * 
 * 功能描述：最大值限制
 *
 * @author  zhangly
 */
public class MaxRule extends BaseRule {
	private long max;
	
	public MaxRule() {
		
	}
	
	public MaxRule(String title, long max) {
		this.title = title;
		this.max = max;
	}

	@Override
	public RuleMessage validator() {
		if(StringUtils.isBlank(value)) {
			return RuleMessage.newOk();
		}
		if(!RuleUtil.isNumber(value)) {
			return RuleMessage.newFail(String.format("%s不是有效数字；", title));
		}
		if(max>0) {
			BigDecimal b1 = new BigDecimal(value);
			BigDecimal b2 = new BigDecimal(max);
			if(b1.compareTo(b2)>0) {
				return RuleMessage.newFail(String.format("%s最大值不能大于%s；", title, max));
			}
		}
		return RuleMessage.newOk();
	}	

	public long getMax() {
		return max;
	}

	public void setMax(long max) {
		this.max = max;
	}
}
