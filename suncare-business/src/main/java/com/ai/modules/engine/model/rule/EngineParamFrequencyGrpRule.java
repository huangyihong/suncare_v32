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

import com.ai.modules.engine.parse.SolrJoinParserPlugin;

/**
 * 
 * 功能描述：频次疾病组规则
 *
 * @author  zhangly
 * Date: 2019年12月31日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineParamFrequencyGrpRule extends AbsEngineParamRule {	
	public EngineParamFrequencyGrpRule(String colName, String compareValue) {
		super(colName, compareValue);
	}

	@Override
	public String where() {
		StringBuilder sb = new StringBuilder();
		sb.append("_query_:\"");
		SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_DIAG", "VISITID", "VISITID");
		sb.append(plugin.parse());
		plugin = new SolrJoinParserPlugin("STD_DIAGGROUP", "DISEASECODE", "DISEASECODE");
		sb.append(plugin.parse());
		sb.append("DIAGGROUP_CODE").append(":");
		
		String[] values = StringUtils.split(compareValue, "|");
		if(values.length==1) {
			sb.append(values[0]);
		} else {
			int index = 0;
			sb.append("(");
			for(String value : values) {			
				if(index>0) {
					sb.append(" OR ");
				}
				sb.append(value);						
				index++;						
			}
			sb.append(")");
		}		
		sb.append("\"");
		if(reverse) {
			return "*:* -("+sb.toString()+")";
		} else {
			return sb.toString();
		}
	}		
}
