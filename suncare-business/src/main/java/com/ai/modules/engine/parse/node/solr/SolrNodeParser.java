/**
 * NodeHandle.java	  V1.0   2020年4月10日 上午9:20:42
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.parse.node.solr;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineNodeRuleGrp;
import com.ai.modules.engine.util.EngineUtil;

public class SolrNodeParser extends AbsSolrNodeParser {
	
	public SolrNodeParser(EngineNode node) {
		super(node);
	}

	@Override
	public String handler() {
		if(EngineUtil.NODE_TYPE_START.equalsIgnoreCase(node.getNodeType())
				|| EngineUtil.NODE_TYPE_END.equalsIgnoreCase(node.getNodeType())) {
			//开始或结束节点
			return null;
		}
		if("filter".equals(node.getNodeType())) {
			//虚拟过滤条件节点
			return node.getCondition();
		}
		int size = node.getWheres()==null ? 0 : node.getWheres().size();
		StringBuilder sb = new StringBuilder();		
		boolean diam = EngineUtil.NODE_TYPE_CONDITIONAL.equalsIgnoreCase(node.getNodeType()); //是否条件节点
		if(diam && "NO".equalsIgnoreCase(node.getCondition())) {
			sb.append("*:* -");
		}
		if(size>1) {
			sb.append("(");
		}
		String condition = null;
		//遍历当前节点查询条件
		for(EngineNodeRuleGrp grp : node.getWheres()) {
			condition = this.parseCondition(grp);
			sb.append(condition).append(" ");
		}
		sb.deleteCharAt(sb.length()-1);
		if(size>1) {
			sb.append(")");
		}
		return sb.toString();
	}

	private String parseCondition(EngineNodeRuleGrp grp) {
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotBlank(grp.getLogic())) {
			sb.append(grp.getLogic().toUpperCase()).append(" ");
		}

		int size = grp.getRuleList().size();
		if(size>1) {
			sb.append("(");
		}
		String condition = null;
		for(EngineNodeRule rule : grp.getRuleList()) {
			AbsSolrNodeRuleHandle handle = SolrNodeRuleHandleFactory.getNodeRuleHandle(rule);
			condition = handle.where();
			sb.append(condition).append(" ");
		}
		sb.deleteCharAt(sb.length()-1);
		if(size>1) {
			sb.append(")");
		}
		return sb.toString();
	}
}
