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
import org.springframework.beans.BeanUtils;

import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.hive.WithTableUtil;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.model.EngineConstant;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineNodeRuleGrp;
import com.ai.modules.engine.parse.node.AbsHiveNodeParser;
import com.ai.modules.engine.parse.node.AbsHiveNodeRuleParser;
import com.ai.modules.engine.parse.node.HiveNodeParser;
import com.ai.modules.engine.parse.node.HiveNodeRuleParser;
import com.ai.modules.engine.util.EngineUtil;

public class HiveDwsNodeHandle extends AbsHiveNodeHandle {

	public HiveDwsNodeHandle(EngineNode node, String fromTable) {
		super(node, fromTable);
	}

	@Override
	public WithTableModel parseWithTableModel() {
		if(node.mergeRuleEnabled()) {
			String sql = parseScriptNodeRuleMerge(node, fromTable);
			String alias = CaseWithTableUtil.buildWithTableAlias(node);
			return new WithTableModel(alias, sql);
		}
		List<WithTableModel> withTableList = new ArrayList<WithTableModel>();
		for(EngineNodeRuleGrp grp : node.getWheres()) {
			String script = parseScriptNodeRule(node.getOrderNo(), grp, fromTable);
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
	private String parseScriptNodeRule(int nodeNo, EngineNodeRuleGrp grp, String fromTable) {
		if(grp.mergeRuleEnabled()) {
			return parseScriptNodeRuleMerge(grp, fromTable);
		}
		List<WithTableModel> withTableList = new ArrayList<WithTableModel>();
		for(EngineNodeRule rule : grp.getRuleList()) {
			AbsHiveDwsNodeRuleHandle handle = HiveNodeHandleFactory.getHiveDwsNodeRuleHandle(rule, fromTable, rule.getTableName());
			String durationType = this.parseNodeDurationType();
			handle.setDurationType(durationType);
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
	public String parseScriptNodeRuleMerge(EngineNode node, String fromTable) {
		EngineNodeRule first = node.getWheres().get(0).getRuleList().get(0);
		String tableName = first.getTableName().toUpperCase();
		HiveDwsNodeRuleHandle handle = new HiveDwsNodeRuleHandle(first, fromTable);
		if(EngineConstant.ENGINE_DWS_MAPPING.containsKey(tableName) || EngineConstant.ENGINE_MIDDLE_DWS_MAPPING.containsKey(tableName)) {
			//需要解析节点查询条件获得数据周期关联字段
			String durationType = this.parseNodeDurationType();
			handle.setDurationType(durationType);
		}
		String sql = handle.template();
		AbsHiveNodeParser parser = new HiveNodeParser(node, false, tableName);
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
	private String parseScriptNodeRuleMerge(EngineNodeRuleGrp grp, String fromTable) {
		EngineNodeRule first = new EngineNodeRule();
		BeanUtils.copyProperties(grp.getRuleList().get(0), first);
		boolean notExists = "notlike".equals(first.getCompareType()) || "<>".equals(first.getCompareType());
		String tableName = first.getTableName().toUpperCase();
		HiveDwsNodeRuleHandle handle = new HiveDwsNodeRuleHandle(first, fromTable);
		if(EngineConstant.ENGINE_DWS_MAPPING.containsKey(tableName) || EngineConstant.ENGINE_MIDDLE_DWS_MAPPING.containsKey(tableName)) {
			//需要解析节点查询条件获得数据周期关联字段
			String durationType = this.parseNodeDurationType();
			handle.setDurationType(durationType);
		}
		String sql = handle.template();
		StringBuilder whereSb = new StringBuilder();
		whereSb.append("(");
		for(EngineNodeRule nodeRule : grp.getRuleList()) {
			AbsHiveNodeRuleParser parser = new HiveNodeRuleParser(nodeRule, false, tableName);
			if(StringUtils.isNotBlank(nodeRule.getLogic())) {
				String logic = nodeRule.getLogic().toUpperCase();
				if(notExists) {
					//不包含条件合并后组内之间关系改成or
					logic = "OR";
				}
				whereSb.append(" ").append(logic).append(" ");
			}
			whereSb.append(parser.handler());
		}
		whereSb.append(")");
		sql = StringUtils.replace(sql, "$where", whereSb.toString());
		return sql;
	}
	
	/**
	 * 
	 * 功能描述：解析节点查询条件获得筛查数据周期类型
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	private String parseNodeDurationType() {
		String durationType = null;
		for(EngineNodeRuleGrp ruleGrp : node.getWheres()) {
			for(EngineNodeRule rule : ruleGrp.getRuleList()) {
				if("DURATIONTYPE".equals(rule.getColName())) {
					durationType = rule.getCompareValue();
				}
			}
		}
		return durationType;
	}
}
