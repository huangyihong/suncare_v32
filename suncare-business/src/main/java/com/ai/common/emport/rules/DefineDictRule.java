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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * 功能描述：自定义字典数据验证
 *
 * @author  zhangly
 */
public class DefineDictRule extends BaseDictRule {
	
	@Override
	public String findDict() {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> dict = new HashMap<String, String>();
		dict.put("dictKey", "USER_STATUS");
		dict.put("itemKey", "01");
		dict.put("itemValue", "正常");
		list.add(dict);
		dict = new HashMap<String, String>();
		dict.put("dictKey", "USER_STATUS");
		dict.put("itemKey", "02");
		dict.put("itemValue", "锁定");
		list.add(dict);
		if(type!=null && "itemValue".equals(type)) {
			if(list!=null) {
				for(Map<String, String> bean : list) {
					if(StringUtils.equals(bean.get("itemValue"), value)) {
						return bean.get("itemKey");
					}
				}
			}
		} else {
			if(list!=null) {
				for(Map<String, String> bean : list) {
					if(StringUtils.equals(bean.get("itemKey"), value)) {
						return value;
					}
				}
			}
		}
		return null;
	}
}
