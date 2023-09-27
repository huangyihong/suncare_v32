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

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * 功能描述：字典数据验证
 *
 * @author  zhangly
 */
public class BaseDictRule extends BaseRule {	
	protected String dictKey;
	protected String type;
	
	public BaseDictRule() {
		
	}

	@Override
	public RuleMessage validator() {		
		if(StringUtils.isNotBlank(value)) {
			// type:{itemKey:字典编码上传数据，itemValue:字典名称上传数据}
			String find = this.findDict();
			if(StringUtils.isBlank(find)) {
				if(type!=null && "itemValue".equals(type)) {
					return RuleMessage.newFail(String.format("%s未能找到对应字典编码；", title));
				} else {
					return RuleMessage.newFail(String.format("%s未能找到对应字典项；", title, value));
				}
			}
		}
		return RuleMessage.newOk();
	}
	
	public String findDict() {
		return null;
	}

	public String getDictKey() {
		return dictKey;
	}

	public void setDictKey(String dictKey) {
		this.dictKey = dictKey;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
