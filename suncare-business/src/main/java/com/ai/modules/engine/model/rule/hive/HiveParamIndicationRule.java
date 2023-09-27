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
 * 功能描述：适用症规则（疾病）
 *
 * @author  zhangly
 * Date: 2019年12月31日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HiveParamIndicationRule extends AbsHiveGrpParamRule {
	
	public HiveParamIndicationRule(String colName, String compareValue, String fromTable) {
		super(colName, compareValue, fromTable);
	}

	@Override
	public String where() {	
		if(compareValue.indexOf(",")>-1) {
			//参数存在分组，|符号分组，组内and关系，组与组之间or关联
			return parseGroupInIsAndGroupOutIsOutWhere(this.getClass());
		} else {
			//参数中组内不存在‘,’分隔符（即组内无and关系），仅存在组与组之间or关系
			return simplex(fromTable, compareValue);
		}
	}		
	
	@Override
	public String simplex(String table, String compareValue) {
		StringBuilder sb = new StringBuilder();
		sb.append("select * from $table where");
		if(reverse) {
			sb.append(" not ");
		}
		String value = "('" + StringUtils.replace(compareValue, "|", "','") + "')";
		sb.append(" exists(select 1 from dwb_diag x1 join medical_gbdp.STD_DIAGGROUP x2 on x1.DISEASECODE=x2.DISEASECODE");
		sb.append(" where");
		if(patient) {
			sb.append(" $table.clientid=x1.clientid");
			if(beforeVisit) {
				sb.append(" and $table.visitdate>x1.visitdate");
			}
		} else {
			sb.append(" $table.visitid=x1.visitid");
		}
		sb.append(" and (");
		sb.append("x2.DIAGGROUP_CODE in").append(value).append(" or x2.DISEASECODE in").append(value);
		sb.append(")");
		sb.append(")");
		String sql = sb.toString();
		sql = StringUtils.replace(sql, "$table", table);
		return sql;
	}
}
