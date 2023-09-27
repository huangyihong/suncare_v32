/**
 * NodeHandle.java	  V1.0   2020年4月10日 上午9:20:42
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.parse.node;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineNodeRuleGrp;
import com.ai.modules.engine.util.EngineUtil;

public class HiveNodeParser extends AbsHiveNodeParser {

	public HiveNodeParser(EngineNode node, boolean master, String alias) {
		super(node, master, alias);
	}
	
	public HiveNodeParser(EngineNode node, String alias) {
		super(node, alias);
	}

	@Override
	public String handler() {
		if(EngineUtil.NODE_TYPE_START.equalsIgnoreCase(node.getNodeType())
				|| EngineUtil.NODE_TYPE_END.equalsIgnoreCase(node.getNodeType())) {
			//开始或结束节点
			return null;
		}
		int size = node.getWheres()==null ? 0 : node.getWheres().size();
		StringBuilder sb = new StringBuilder();
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
			if(StringUtils.isNotBlank(rule.getLogic())) {
				sb.append(rule.getLogic().toUpperCase()).append(" ");
			}
			AbsHiveNodeRuleParser parser = new HiveNodeRuleParser(rule, master, alias);
			condition = parser.handler();
			sb.append(condition).append(" ");
		}
		sb.deleteCharAt(sb.length()-1);
		if(size>1) {
			sb.append(")");
		}
		return sb.toString();
	}
}
