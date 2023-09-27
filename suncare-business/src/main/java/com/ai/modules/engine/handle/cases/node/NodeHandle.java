/**
 * NodeHandle.java	  V1.0   2020年4月10日 上午9:20:42
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.handle.cases.node;

import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRuleGrp;
import com.ai.modules.engine.util.EngineUtil;

public class NodeHandle extends AbsNodeHandle {

	public NodeHandle(EngineNode node) {
		super(node);
	}

	@Override
	public String parseConditionExpression() {
		if(EngineUtil.NODE_TYPE_START.equalsIgnoreCase(node.getNodeType())
				|| EngineUtil.NODE_TYPE_END.equalsIgnoreCase(node.getNodeType())) {
			//开始或结束节点
			return null;
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
		if(size > 0){
			for(EngineNodeRuleGrp grp : node.getWheres()) {
				condition = EngineUtil.parseConditionExpression(grp);
				sb.append(condition).append(" ");
			}
		}
		if(sb.length() > 0){
			sb.deleteCharAt(sb.length()-1);
		}

		if(size>1) {
			sb.append(")");
		}
		return sb.toString();
	}

}
