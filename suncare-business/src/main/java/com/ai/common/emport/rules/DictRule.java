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

import org.jeecg.common.util.SpringContextUtils;

import com.ai.modules.config.service.IMedicalDictService;

/**
 * 
 * 功能描述：字典数据验证
 *
 * @author  zhangly
 */
public class DictRule extends BaseDictRule {	

	@Override
	public String findDict() {
		IMedicalDictService dictSV = SpringContextUtils.getApplicationContext().getBean(IMedicalDictService.class);
		if(type!=null && "itemValue".equals(type)) {
			return dictSV.queryDictKeyByText(dictKey, value);
		} else {
			String dict = dictSV.queryDictTextByKey(dictKey, value);
			if(dict!=null) {
				return value;
			}
			return null;
		}
	}
}
