/**
 * HiveNodeHandle.java	  V1.0   2022年12月2日 下午1:08:27
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.node.hive;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.hive.WithTableUtil;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRuleGrp;
import com.ai.modules.engine.util.EngineUtil;

public class HiveNodeHandle extends AbsHiveNodeHandle {

	public HiveNodeHandle(EngineNode node, String fromTable) {
		super(node, fromTable);
	}

	@Override
	public WithTableModel parseWithTableModel() {
		if(node.mergeRuleEnabled()) {
			String sql = CaseWithTableUtil.parseScriptNodeRuleMerge(node, fromTable);
			String alias = CaseWithTableUtil.buildWithTableAlias(node);
			return new WithTableModel(alias, sql);
		}
		List<WithTableModel> withTableList = new ArrayList<WithTableModel>();
		for(EngineNodeRuleGrp grp : node.getWheres()) {
			String script = CaseWithTableUtil.parseScriptNodeRule(node.getOrderNo(), grp, fromTable);
			String logic = "and";
			if(StringUtils.isNotBlank(grp.getLogic())) {
				logic = grp.getLogic();
			}
			String alias = AbsHiveRuleHandle.WITH_TABLE_MASTER+"_"+node.getOrderNo()+"_"+grp.getRuleList().get(0).getGroupNo();
			withTableList.add(new WithTableModel(alias, script, logic));
		}
		String alias = CaseWithTableUtil.buildWithTableAlias(node);
		if(withTableList.size()==1) {
			return new WithTableModel(alias, withTableList.get(0).getSql());
		}
		String sql = WithTableUtil.parseWithTableList(fromTable, withTableList);		
		//是否属于否条件节点
		boolean reverse = EngineUtil.NODE_TYPE_CONDITIONAL.equalsIgnoreCase(node.getNodeType()) && "NO".equalsIgnoreCase(node.getCondition());
		if(reverse) {
			StringBuilder sb = new StringBuilder();
			sb.append("with ").append(alias).append(" as(");
			sb.append(sql);
			sb.append(")");
			sb.append("\n");
			sb.append("select * from ").append(fromTable);
			sb.append(" where visitid not in(");
			sb.append("select visitid from ").append(alias);
			sb.append(")");
			return new WithTableModel(alias, sb.toString());
		}
		return new WithTableModel(alias, sql);
	}

	
}
