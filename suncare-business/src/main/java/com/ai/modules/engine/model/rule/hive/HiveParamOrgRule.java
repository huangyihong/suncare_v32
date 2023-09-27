/**
 * EngineParamRule.java	  V1.0   2019年12月31日 下午5:23:44
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.rule.hive;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * 功能描述：医疗机构类别规则
 *
 * @author  zhangly
 * Date: 2019年12月31日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HiveParamOrgRule extends AbsHiveParamRule {	
	public HiveParamOrgRule(String colName, String compareValue, String fromTable) {
		super(colName, compareValue, fromTable);
	}

	@Override
	public String where() {
		compareValue = "('" + StringUtils.replace(compareValue, "|", "','") + "')";
		StringBuilder sb = new StringBuilder();
		sb.append("select * from $table where");
		if(reverse) {
			sb.append(" not");
		}
		sb.append(" exists(select 1 from DWB_MASTER_INFO x1 join medical_gbdp.STD_ORGANIZATION x2 on $table.visitid=x1.visitid");
		sb.append(" and x2.ORGTYPE_CODE in").append(compareValue);
		sb.append(")");
		String sql = sb.toString();
		sql = StringUtils.replace(sql, "$table", fromTable);
		return sql;
	}		
}
