/**
 * BaseHiveNodeRuleHandle.java	  V1.0   2022年12月2日 上午11:18:46
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.node.hive;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineNodeRule;

public class HiveStdDiagGrpNodeRuleHandle extends HiveStdNodeRuleHandle {

	public HiveStdDiagGrpNodeRuleHandle(EngineNodeRule rule, String fromTable) {
		super(rule, fromTable);
	}
	
	@Override
	protected String template() {
		StringBuilder sb = new StringBuilder();
		sb.append("select * from $table");
		sb.append(" where");
		sb.append(" $table.visitid");
		if(hasReverse()) {
			sb.append(" not");
		}
		sb.append(" in(select DWB_DIAG.visitid from DWB_DIAG join medical_gbdp.STD_DIAGGROUP STD_DIAGGROUP");
		sb.append(" on DWB_DIAG.DISEASECODE=STD_DIAGGROUP.DISEASECODE");
		sb.append(" where $where");
		sb.append(")");
		String sql = sb.toString();
		sql = StringUtils.replace(sql, "$table", fromTable);
		return sql;
	}
}
