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

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineConstant;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineNodeRuleGrp;
import com.ai.modules.engine.util.EngineUtil;

public class DwsNodeHandle extends AbsNodeHandle {

	public DwsNodeHandle(EngineNode node) {
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
		boolean diam = node.getNodeType().toLowerCase().startsWith(EngineUtil.NODE_TYPE_CONDITIONAL); //是否条件节点
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
				condition = parseConditionExpression(grp);
				sb.append(condition).append(" ");
			}
			sb.deleteCharAt(sb.length()-1);
		}

		if(size>1) {
			sb.append(")");
		}
		return sb.toString();
	}
	
	/**
	 *
	 * 功能描述：流程节点单条规则组解析成查询条件字符串
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年11月28日 下午5:04:32</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private String parseConditionExpression(EngineNodeRuleGrp grp) {
		if(grp.mergeRuleEnabled()) {
			//组内查询条件允许合并
			return parseConditionExpressionMerge(grp);
		}

		StringBuilder sb = new StringBuilder();

		if(StringUtils.isNotBlank(grp.getLogic())) {
			sb.append(grp.getLogic().toUpperCase()).append(" ");
		}

		int size = grp.getRuleList().size();
		if(size>1) {
			sb.append("(");
		}
		for(EngineNodeRule rule : grp.getRuleList()) {
			String tableName = rule.getTableName().toUpperCase();
			DwsNodeRuleHandle handle = new DwsNodeRuleHandle(rule);
			if(EngineConstant.ENGINE_DWS_MAPPING.containsKey(tableName) || EngineConstant.ENGINE_MIDDLE_DWS_MAPPING.containsKey(tableName)) {
				//需要解析节点查询条件获得数据周期关联字段
				String durationType = this.parseNodeDurationType(grp);
				handle.setDurationType(durationType);
			}
			sb.append(handle.where(false));
			sb.append(" ");
		}
		sb.deleteCharAt(sb.length()-1);
		if(size>1) {
			sb.append(")");
		}

		return sb.toString();
	}
	
	/**
	 * 
	 * 功能描述：组内条件合并
	 *
	 * @author  zhangly
	 *
	 * @param grp
	 * @return
	 */
	private String parseConditionExpressionMerge(EngineNodeRuleGrp grp) {
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotBlank(grp.getLogic())) {
			sb.append(grp.getLogic().toUpperCase()).append(" ");
		}
		//节点所有的查询条件合并
		EngineNodeRule first = node.getWheres().get(0).getRuleList().get(0);
		String tableName = first.getTableName().toUpperCase();
		DwsNodeRuleHandle handle = new DwsNodeRuleHandle(first);
		if(EngineConstant.ENGINE_DWS_MAPPING.containsKey(tableName) || EngineConstant.ENGINE_MIDDLE_DWS_MAPPING.containsKey(tableName)) {
			//需要解析节点查询条件获得数据周期关联字段
			String durationType = this.parseNodeDurationType(grp);
			handle.setDurationType(durationType);
		}
		String template = handle.template();
		template = StringUtils.replace(template, "$where", mergeWhere(grp));
		sb.append(template);
		return sb.toString();
	}
	
	protected String mergeWhere(EngineNodeRuleGrp grp) {
		StringBuilder sb = new StringBuilder();
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
	private String parseNodeDurationType(EngineNodeRuleGrp grp) {
		String durationType = null;
		for(EngineNodeRule rule : grp.getRuleList()) {		
			String value = rule.getCompareValue();
			if("DURATIONTYPE".equals(rule.getColName())) {
				if("日".equals(value)) {
					durationType = "D";
					break;
				} else if("周".equals(value)) {
					durationType = "W";
					break;
				} else if("月".equals(value)) {
					durationType = "M";
					break;
				} else if("季".equals(value)) {
					durationType = "Q";
					break;
				} else if("年".equals(value)) {
					durationType = "Y";
					break;
				}
			}
		}
		if(durationType==null) {
			//未找到周期，从整个节点条件查找
			for(EngineNodeRuleGrp ruleGrp : node.getWheres()) {
				for(EngineNodeRule rule : ruleGrp.getRuleList()) {
					String value = rule.getCompareValue();
					if("DURATIONTYPE".equals(rule.getColName())) {
						if("日".equals(value)) {
							durationType = "D";
							break;
						} else if("周".equals(value)) {
							durationType = "W";
							break;
						} else if("月".equals(value)) {
							durationType = "M";
							break;
						} else if("季".equals(value)) {
							durationType = "Q";
							break;
						} else if("年".equals(value)) {
							durationType = "Y";
							break;
						}
					}
				}
			}
		}
		return durationType;
	}
}
