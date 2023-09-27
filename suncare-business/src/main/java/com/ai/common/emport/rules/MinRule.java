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
 * 功能描述：最小值限制
 *
 * @author  zhangly
 */
public class MinRule extends BaseRule {
	private int min;
	
	public MinRule() {
		
	}
	
	public MinRule(String title, int min) {
		this.title = title;
		this.min = min;
	}

	@Override
	public RuleMessage validator() {
		if(StringUtils.isBlank(value)) {
			return RuleMessage.newOk();
		}
		if(!RuleUtil.isNumber(value)) {
			return RuleMessage.newFail(String.format("%s不是有效数字；", title));
		}
		BigDecimal b1 = new BigDecimal(value);
		BigDecimal b2 = new BigDecimal(min);
		if(b1.compareTo(b2)<0) {
			return RuleMessage.newFail(String.format("%s最小值不能小于%s；", title, min));
		}
		return RuleMessage.newOk();
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}	
}
