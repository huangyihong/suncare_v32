/**
 * EngineParamRule.java	  V1.0   2019年12月31日 下午5:23:44
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.rule;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;

/**
 * 
 * 功能描述：医疗机构类别规则
 *
 * @author  zhangly
 * Date: 2019年12月31日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineParamOrgRule extends AbsEngineParamRule {	
	public EngineParamOrgRule(String colName, String compareValue) {
		super(colName, compareValue);
	}

	@Override
	public String where() {
		if(compareValue.indexOf("|")!=-1) {
			compareValue = "(" + StringUtils.replace(compareValue, "|", " OR ") + ")";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("_query_:\"");
		EngineMapping mapping = new EngineMapping("DWB_MASTER_INFO", "VISITID", "VISITID");
		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
		sb.append(plugin.parse());
		plugin = new SolrJoinParserPlugin("STD_ORGANIZATION", "ORGID", "ORGID");
		sb.append(plugin.parse());
		sb.append(colName).append(":").append(compareValue);
		sb.append("\"");
		
		if(reverse) {
			return "*:* -("+sb.toString()+")";
		} else {
			return sb.toString();
		}
	}		
}
