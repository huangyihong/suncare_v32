/**
 * NodeRuleHandleFactory.java	  V1.0   2020年4月9日 上午11:35:20
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.node;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.config.entity.MedicalColConfig;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.util.EngineUtil;

public class NodeRuleHandleFactory {
	public static AbsNodeRuleHandle getNodeRuleHandle(EngineNodeRule rule) {

		if("STD_TREATMENT".equalsIgnoreCase(rule.getTableName())) {
			//医疗服务项目分类
			return new StdTreatmentNodeRuleHandle(rule);
		}
		if("STD_TREATGROUP".equalsIgnoreCase(rule.getTableName())) {
			//项目组
			return new StdTreatgroupNodeRuleHandle(rule);
		}
		if("STD_DRUGGROUP".equalsIgnoreCase(rule.getTableName())) {
			//药品组信息
			return new StdDruggroupNodeRuleHandle(rule);
		}
		if("STD_DIAGGROUP".equalsIgnoreCase(rule.getTableName())) {
			//疾病组信息
			return new StdDiaggroupNodeRuleHandle(rule);
		}
		MedicalColConfig config = rule.getColConfig();
		if(null!= config
				&& StringUtils.isNotBlank(config.getId()) 
				&& config.getColType()==2
				&& StringUtils.isNotBlank(config.getColValueExpressionSolr())
				&& !"VIRTUAL".equals(config.getColValueExpressionSolr())) {
			//虚拟字段-solr计算表达式
			return new ExpressionNodeRuleHandle(rule);
		}
		return new BaseNodeRuleHandle(rule);	
	}
	
	public static AbsNodeHandle getNodeHandle(EngineNode node) {
		if(node.getNodeType().toLowerCase().endsWith("dws")
				|| node.getNodeType().toLowerCase().endsWith("dws_v")
				|| EngineUtil.NODE_TYPE_PATIENT.equalsIgnoreCase(node.getNodeType())
				|| EngineUtil.NODE_TYPE_DOCTOR.equalsIgnoreCase(node.getNodeType())
				|| EngineUtil.NODE_TYPE_ORG.equalsIgnoreCase(node.getNodeType())
				|| EngineUtil.NODE_TYPE_DEPT.equalsIgnoreCase(node.getNodeType())) {
			//dws层节点
			if(node.mergeRuleEnabled()) {
				//合并查询条件
				return new DwsNodeMergeRuleHandle(node);
			}
			return new DwsNodeHandle(node);
		}
		if(node.mergeRuleEnabled()) {
			//合并查询条件
			return new NodeMergeRuleHandle(node);
		}
		return new NodeHandle(node);
	}
}
