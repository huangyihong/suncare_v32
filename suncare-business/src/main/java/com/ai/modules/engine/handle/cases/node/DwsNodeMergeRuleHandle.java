/**
 * NodeSingleRuleHandle.java	  V1.0   2020年4月10日 上午9:23:18
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.node;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineConstant;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineNodeRuleGrp;
import com.ai.modules.engine.util.EngineUtil;

/**
 * 
 * 功能描述：合并dws节点查询条件
 *
 * @author  zhangly
 * Date: 2020年4月10日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class DwsNodeMergeRuleHandle extends AbsNodeHandle {

	public DwsNodeMergeRuleHandle(EngineNode node) {
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
		//解析当前节点的查询条件
		sb.append(this.parseConditionExpressionMerge());
		if(size>1) {
			sb.append(")");
		}
		return sb.toString();
	}

	private String parseConditionExpressionMerge() {
		//节点所有的查询条件合并
		EngineNodeRule first = node.getWheres().get(0).getRuleList().get(0);
		String tableName = first.getTableName().toUpperCase();
		DwsNodeRuleHandle handle = new DwsNodeRuleHandle(first);
		if(EngineConstant.ENGINE_DWS_MAPPING.containsKey(tableName) || EngineConstant.ENGINE_MIDDLE_DWS_MAPPING.containsKey(tableName)) {
			//需要解析节点查询条件获得数据周期关联字段
			String durationType = this.parseNodeDurationType();
			handle.setDurationType(durationType);
		}
		String template = handle.template();
		template = StringUtils.replace(template, "$where", mergeWhere());
		return template;
	}
	
	protected String mergeWhere() {
		StringBuilder sb = new StringBuilder();
		int size = node.getWheres().size();
		if(size>1) {
			sb.append("(");
		}
		//遍历节点组
		for(EngineNodeRuleGrp grp : node.getWheres()) {
			if(StringUtils.isNotBlank(grp.getLogic())) {
				sb.append(" ").append(grp.getLogic().toUpperCase()).append(" ");
			}
			List<EngineNodeRule> ruleList = grp.getRuleList();
			if(ruleList.size()>1) {
				sb.append("(");
			}
			String condition = null;
			for(EngineNodeRule nodeRule : ruleList) {
				AbsNodeRuleHandle handle = new BaseNodeRuleHandle(nodeRule);
				condition = handle.where(true);
				sb.append(condition).append(" ");
			}
			sb.deleteCharAt(sb.length()-1);
			if(ruleList.size()>1) {
				sb.append(")");
			}
		}
		if(size>1) {
			sb.append(")");
		}
		return sb.toString();
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
		String durationType = "D";
		for(EngineNodeRuleGrp grp : node.getWheres()) {
			for(EngineNodeRule rule : grp.getRuleList()) {		
				if("DURATIONTYPE".equals(rule.getColName())) {
					if("日".equals(rule.getCompareValue())) {
						durationType = "D";
						break;
					} else if("周".equals(rule.getCompareValue())) {
						durationType = "W";
						break;
					} else if("月".equals(rule.getCompareValue())) {
						durationType = "M";
						break;
					} else if("季".equals(rule.getCompareValue())) {
						durationType = "Q";
						break;
					} else if("年".equals(rule.getCompareValue())) {
						durationType = "Y";
						break;
					}
				}
			}
		}
		
		return durationType;
	}
}
