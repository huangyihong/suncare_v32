/**
 * NodeRuleHandleFactory.java	  V1.0   2020年4月9日 上午11:35:20
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.parse.node.solr;

import com.ai.modules.engine.model.EngineNodeRule;

public class SolrNodeRuleHandleFactory {
	public static AbsSolrNodeRuleHandle getNodeRuleHandle(EngineNodeRule rule) {

		if("STD_TREATGROUP".equalsIgnoreCase(rule.getTableName())) {
			//项目组
			return new SolrStdTreatgrpNodeRuleHandle(rule);
		} else if("STD_DRUGGROUP".equalsIgnoreCase(rule.getTableName())) {
			//药品组
			return new SolrStdDruggrpNodeRuleHandle(rule);
		} else if("STD_DIAGGROUP".equalsIgnoreCase(rule.getTableName())) {
			//疾病组
			return new SolrStdDiaggrpNodeRuleHandle(rule);
		}
		return new SolrNodeRuleHandle(rule);	
	}
}
