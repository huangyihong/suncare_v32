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
import com.ai.modules.engine.parse.node.AbsHiveNodeRuleParser;
import com.ai.modules.engine.parse.node.HiveNodeRuleParser;
import com.ai.modules.engine.util.EngineUtil;

public class HiveStdNodeRuleHandle extends HiveNodeRuleHandle {

	public HiveStdNodeRuleHandle(EngineNodeRule rule, String fromTable) {
		super(rule, fromTable);
	}

	@Override
	public String script() {
		String sql = this.template();
		AbsHiveNodeRuleParser parser = new HiveNodeRuleParser(rule, false, rule.getTableName());
		String where = parser.handler();
		sql = StringUtils.replace(sql, "$where", where);
		return sql;
	}
	
	/**
	 * 
	 * 功能描述：sql脚本模板
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	protected String template() {
		String tableName = rule.getTableName();
		StringBuilder sb = new StringBuilder();
		sb.append("select * from $table");
		sb.append(" where");
		sb.append(" $table.visitid");
		if(hasReverse()) {
			sb.append(" not");
		}
		sb.append(" in(select ");
		sb.append(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM).append(".visitid");
		sb.append(" from ").append(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM).append(" join medical_gbdp.").append(tableName).append(" ").append(tableName);
		if("STD_TREATGROUP".equals(tableName)) {
			//诊疗项目组
			sb.append(" on ").append(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM).append(".itemcode=").append(tableName).append(".TREATCODE");
		} else if("STD_DRUGGROUP".equals(tableName)) {
			//药品组
			sb.append(" on ").append(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM).append(".itemcode=").append(tableName).append(".ATC_DRUGCODE");
		} else if("STD_TREATMENT".equals(tableName)) {
			//器材组
			sb.append(" on ").append(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM).append(".itemcode=").append(tableName).append(".TREATCODE");
		}
		sb.append(" where $where");
		sb.append(")");
		String sql = sb.toString();
		sql = StringUtils.replace(sql, "$table", fromTable);
		return sql;
	}
}
