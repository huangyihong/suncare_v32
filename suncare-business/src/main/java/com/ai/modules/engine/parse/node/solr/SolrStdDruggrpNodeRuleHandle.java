/**
 * SolrStdTreatGroupNodeRuleHandle.java	  V1.0   2021年1月27日 下午12:51:07
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.parse.node.solr;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;

public class SolrStdDruggrpNodeRuleHandle extends SolrNodeRuleHandle {

	public SolrStdDruggrpNodeRuleHandle(EngineNodeRule rule) {
		super(rule);
	}
	
	@Override
	protected String where(boolean ignoreJoin) {
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotBlank(rule.getLogic())) {
			sb.append(rule.getLogic().toUpperCase()).append(" ");
		}
				
		if (this.hasReverse()) {
			sb.append("(*:* -");
		}
		
		sb.append(handler());
		
		if (this.hasReverse()) {
			sb.append(")");
		}
		return sb.toString();
	}

	public String handler() {
		StringBuilder sb = new StringBuilder();
		
		String colName = rule.getColName().toUpperCase();
		String compareType = rule.getCompareType();
		
		sb.append("_query_:");
		EngineMapping mapping = new EngineMapping("STD_DRUGGROUP", "ATC_DRUGCODE", "ITEMCODE");
		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
		sb.append(plugin.parse());
		
		String field = colName;
		if("=".equals(compareType) || "regx".equals(compareType)) {
			sb.append(field).append(":").append(rule.getCompareValue());
		} else if(compareType.equalsIgnoreCase("notin") || compareType.equals("<>")) {
			sb.append(field).append(":").append(rule.getCompareValue());
		} else if (compareType.equalsIgnoreCase("like")) {
			// 包含
			sb.append(field).append(":*").append(rule.getCompareValue()).append("*");
		} else if (compareType.equalsIgnoreCase("llike")) {
			// 以..开始
			sb.append(field).append(":").append(rule.getCompareValue()).append("*");
		} else if (compareType.equalsIgnoreCase("rlike")) {
			// 以..结尾
			sb.append(field).append(":*").append(rule.getCompareValue());
		} else if (compareType.equalsIgnoreCase("notlike")) {
			// 不包含
			sb.append(field).append(":*").append(rule.getCompareValue()).append("*");
		}
		return sb.toString();
	}
}
