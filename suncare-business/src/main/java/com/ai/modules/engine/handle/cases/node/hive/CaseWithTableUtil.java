/**
 * CaseWithTableUtil.java	  V1.0   2022年12月5日 下午3:23:37
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
import org.springframework.beans.BeanUtils;

import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.hive.WithTableUtil;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineNodeRuleGrp;
import com.ai.modules.engine.parse.node.AbsHiveNodeParser;
import com.ai.modules.engine.parse.node.HiveNodeParser;

public class CaseWithTableUtil {

	/**
	 * 
	 * 功能描述：解析节点规则条件合并
	 *
	 * @author  zhangly
	 *
	 * @param node
	 * @param fromTable
	 * @return
	 */
	public static String parseScriptNodeRuleMerge(EngineNode node, String fromTable) {
		EngineNodeRule first = node.getWheres().get(0).getRuleList().get(0);
		AbsHiveNodeRuleHandle handle = HiveNodeHandleFactory.getHiveNodeRuleHandle(first, fromTable);
		String sql = handle.template();
		AbsHiveNodeParser parser = new HiveNodeParser(node, false, null);
		sql = StringUtils.replace(sql, "$where", parser.handler());
		return sql;
	}
	
	/**
	 * 
	 * 功能描述：解析节点某个组内规则条件合并
	 *
	 * @author  zhangly
	 *
	 * @param node
	 * @param fromTable
	 * @return
	 */
	public static String parseScriptNodeRuleMerge(EngineNodeRuleGrp grp, String fromTable) {
		EngineNodeRule first = new EngineNodeRule();
		BeanUtils.copyProperties(grp.getRuleList().get(0), first);
		boolean notExists = "notlike".equals(first.getCompareType()) || "<>".equals(first.getCompareType());
		AbsHiveNodeRuleHandle handle = HiveNodeHandleFactory.getHiveNodeRuleHandle(first, fromTable);
		String sql = handle.template();
		StringBuilder whereSb = new StringBuilder();
		whereSb.append("(");
		for(EngineNodeRule nodeRule : grp.getRuleList()) {
			AbsHiveNodeRuleHandle process = HiveNodeHandleFactory.getHiveNodeRuleHandle(nodeRule, fromTable);
			if(StringUtils.isNotBlank(nodeRule.getLogic())) {
				String logic = nodeRule.getLogic().toUpperCase();
				if(notExists) {
					//不包含条件合并后组内之间关系改成or
					logic = "OR";
				}
				whereSb.append(" ").append(logic).append(" ");
			}
			whereSb.append(process.where(false));
		}
		whereSb.append(")");
		sql = StringUtils.replace(sql, "$where", whereSb.toString());
		return sql;
	}
	
	/**
	 * 
	 * 功能描述：解析组内规则条件
	 *
	 * @author  zhangly
	 *
	 * @param grp
	 * @param fromTable
	 * @return
	 */
	public static String parseScriptNodeRule(int nodeNo, EngineNodeRuleGrp grp, String fromTable) {
		if(grp.mergeRuleEnabled()) {
			return parseScriptNodeRuleMerge(grp, fromTable);
		}
		List<WithTableModel> withTableList = new ArrayList<WithTableModel>();
		for(EngineNodeRule rule : grp.getRuleList()) {
			AbsHiveNodeRuleHandle handle = HiveNodeHandleFactory.getHiveNodeRuleHandle(rule, fromTable);
			String script = handle.script();
			String logic = "and";
			if(StringUtils.isNotBlank(rule.getLogic())) {
				logic = rule.getLogic();
			}
			String alias = AbsHiveRuleHandle.WITH_TABLE_MASTER+"_"+nodeNo+"_"+rule.getGroupNo()+"_"+rule.getOrderNo();
			withTableList.add(new WithTableModel(alias, script, logic));
		}
		return WithTableUtil.parseWithTableList(fromTable, withTableList);
	}
	
	public static String buildWithTableAlias(EngineNode node) {
		return AbsHiveRuleHandle.WITH_TABLE_MASTER+"_"+node.getOrderNo();
	}
	
	public static String buildWithTableAlias(EngineNode node, String fromTable) {
		return fromTable+"_"+node.getOrderNo();
	}
}
