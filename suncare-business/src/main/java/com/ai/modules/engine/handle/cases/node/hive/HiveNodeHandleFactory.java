/**
 * HiveNodeHandleFactory.java	  V1.0   2022年12月2日 下午5:34:41
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.node.hive;

import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;

public class HiveNodeHandleFactory {

	public static AbsHiveNodeHandle getHiveNodeHandle(EngineNode node, String fromTable) {
		if(node.getNodeType().toLowerCase().endsWith("dws")
				|| node.getNodeType().toLowerCase().endsWith("dws_v")) {
			//dws层节点
			return new HiveDwsNodeHandle(node, fromTable);
		}
		return new HiveNodeHandle(node, fromTable);
	}
	
	public static AbsHiveNodeRuleHandle getHiveNodeRuleHandle(EngineNodeRule rule, String fromTable) {
		if("STD_TREATMENT".equalsIgnoreCase(rule.getTableName())) {
			//医疗服务项目分类
			return new HiveStdNodeRuleHandle(rule, fromTable);
		}
		if("STD_TREATGROUP".equalsIgnoreCase(rule.getTableName())) {
			//项目组信息
			return new HiveStdNodeRuleHandle(rule, fromTable);
		}
		if("STD_DRUGGROUP".equalsIgnoreCase(rule.getTableName())) {
			//药品组信息
			return new HiveStdNodeRuleHandle(rule, fromTable);
		}
		if("STD_DIAGGROUP".equalsIgnoreCase(rule.getTableName())) {
			//疾病组信息
			return new HiveStdDiagGrpNodeRuleHandle(rule, fromTable);
		}
		return new HiveNodeRuleHandle(rule, fromTable);
	}
	
	public static AbsHiveDwsNodeRuleHandle getHiveDwsNodeRuleHandle(EngineNodeRule rule, String fromTable, String alias) {
		String table = rule.getTableName();
		if("DWS_DISEASE_TREATDIST".equals(table)) {
			return new HiveDwsDiseaseTreatdistNodeRuleHandle(rule, fromTable, alias);
		}
		return new HiveDwsNodeRuleHandle(rule, fromTable, alias);
	}
}
