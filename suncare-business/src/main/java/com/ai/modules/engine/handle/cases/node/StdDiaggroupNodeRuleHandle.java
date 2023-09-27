/**
 * EngineNodeRuleHandler.java	  V1.0   2020年4月9日 上午11:23:06
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.node;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;

public class StdDiaggroupNodeRuleHandle extends AbsTemplateNodeRuleHandle {
	public StdDiaggroupNodeRuleHandle(EngineNodeRule rule) {
		super(rule);
	}
	
	@Override
	public String where(boolean ignoreJoin) {
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotBlank(rule.getLogic())) {
			sb.append(rule.getLogic().toUpperCase()).append(" ");
		}
		if (hasReverse()) {
			sb.append("(*:* -");
		}
		sb.append("_query_:\"");
		EngineMapping mapping = new EngineMapping(EngineUtil.DWB_DIAG, "VISITID", "VISITID");
		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
		sb.append(plugin.parse());
		
		sb.append(handler());
		
		sb.append("\"");
		if (hasReverse()) {
			sb.append(")");
		}
		return sb.toString();
	}

	public String handler() {
		StringBuilder sb = new StringBuilder();		
		sb.append("_query_:");
		EngineMapping mapping = new EngineMapping("STD_DIAGGROUP", "DISEASECODE", "DISEASECODE");
		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
		sb.append(plugin.parse());

		sb.append(this.condition());
		return sb.toString();
	}
	
	@Override
	public String template() {
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotBlank(rule.getLogic())) {
			sb.append(rule.getLogic().toUpperCase()).append(" ");
		}
		if (hasReverse()) {
			sb.append("(*:* -");
		}
		sb.append("_query_:\"");
		EngineMapping mapping = new EngineMapping(EngineUtil.DWB_DIAG, "VISITID", "VISITID");
		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
		sb.append(plugin.parse());
		sb.append("_query_:\\\"");
		mapping = new EngineMapping("STD_DIAGGROUP", "DISEASECODE", "DISEASECODE");
		plugin = SolrJoinParserPlugin.build(mapping);
		sb.append(plugin.parse());
		sb.append("$where");
		sb.append("\\\"");
		sb.append("\"");
		if (hasReverse()) {
			sb.append(")");
		}
		return sb.toString();
	}
}
