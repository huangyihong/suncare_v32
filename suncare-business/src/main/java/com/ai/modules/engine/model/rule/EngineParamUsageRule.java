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
import com.ai.modules.engine.util.EngineUtil;

/**
 * 
 * 功能描述：给药途径规则
 *
 * @author  zhangly
 * Date: 2019年12月31日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineParamUsageRule extends AbsEngineParamRule {	
	public EngineParamUsageRule(String compareValue) {
		this.compareValue = compareValue;
	}

	@Override
	public String where() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append("_query_\"");
		EngineMapping mapping = new EngineMapping(EngineUtil.DWB_CHARGE_DETAIL, "VISITID", "VISITID");
		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
		sb.append(plugin.parse());
		String[] values = StringUtils.split(compareValue, "|");
		sb.append("USAGE:(");
		int index = 0;
		for(String value : values) {
			if(index>0) {
				sb.append(" OR ");
			}
			sb.append(value);
			index++;
		}
		sb.append(")");
		sb.append("\"");
		sb.append(" OR ");
		sb.append("_query_\"");
		mapping = new EngineMapping(EngineUtil.DWB_ORDER, "VISITID", "VISITID");
		plugin = SolrJoinParserPlugin.build(mapping);
		sb.append(plugin.parse());
		sb.append("USETYPE:(");
		index = 0;
		for(String value : values) {
			if(index>0) {
				sb.append(" OR ");
			}
			sb.append(value);
			index++;
		}
		sb.append(")");
		sb.append("\"");
		sb.append(")");
		
		if(reverse) {
			return "*:* -("+sb.toString()+")";
		} else {
			return sb.toString();
		}
	}		
}
