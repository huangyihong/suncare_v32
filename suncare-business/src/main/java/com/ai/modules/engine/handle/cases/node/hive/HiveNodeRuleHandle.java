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

import com.ai.modules.engine.model.EngineConstant;
import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.util.EngineUtil;

public class HiveNodeRuleHandle extends AbsHiveNodeRuleHandle {

	public HiveNodeRuleHandle(EngineNodeRule rule, String fromTable) {
		super(rule, fromTable);
	}

	@Override
	public String script() {
		String sql = this.template();
		boolean master = isJoin();
		sql = StringUtils.replace(sql, "$where", this.where(master));
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
		StringBuilder sb = new StringBuilder();
		String tableName = rule.getTableName().toUpperCase();
		if(EngineUtil.DWB_MASTER_INFO.equals(tableName)) {
			sb.append("select * from $table where $where");
		} else {
			EngineMapping mapping = EngineConstant.ENGINE_MAPPING.get(tableName);
			if(mapping==null) {
				mapping = new EngineMapping(tableName, "VISITID", "VISITID");
			}
			sb.append("select * from $table");
			sb.append(" where");
			if(hasReverse()) {
				sb.append(" not");
			}
			sb.append(" exists(select 1");
			sb.append(" from ").append(tableName);
			sb.append(" where ");
			sb.append("$table.").append(mapping.getTo()).append("=").append(tableName).append(".").append(mapping.getFrom());
			sb.append(" and $where");
			sb.append(")");
		}
		String sql = sb.toString();
		sql = StringUtils.replace(sql, "$table", fromTable);
		return sql;
	}
}
