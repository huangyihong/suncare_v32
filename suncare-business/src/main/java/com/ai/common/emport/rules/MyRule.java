/**
 * MyRule.java	  V1.0   2018年11月30日 下午4:33:10
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.common.emport.rules;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * 功能描述：测试自定义
 *
 * @author  zhangly
 */
public class MyRule extends BaseRule {

	@Override
	public RuleMessage validator() {
		if(StringUtils.isNotBlank(value) && "admin".equals(value)) {
			return RuleMessage.newFail(String.format("%s已存在！", title));
		}
		return RuleMessage.newOk();
	}
}
