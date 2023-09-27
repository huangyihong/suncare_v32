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

public class JoinNodeRuleHandle extends AbsTemplateNodeRuleHandle {
	public JoinNodeRuleHandle(EngineNodeRule rule) {
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
		EngineMapping mapping = EngineUtil.ENGIME_MAPPING.get(rule.getTableName().toUpperCase());
		if(mapping==null) {
			mapping = new EngineMapping(rule.getTableName().toUpperCase(), "VISITID", "VISITID");
		}
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
		return condition();
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
		EngineMapping mapping = EngineUtil.ENGIME_MAPPING.get(rule.getTableName().toUpperCase());
		if(mapping==null) {
			mapping = new EngineMapping(rule.getTableName().toUpperCase(), "VISITID", "VISITID");
		}
		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
		sb.append(plugin.parse());
		sb.append("$where");
		sb.append("\"");
		if (hasReverse()) {
			sb.append(")");
		}
		return sb.toString();
	}
}
