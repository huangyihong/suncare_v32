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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;

/**
 * 
 * 功能描述：自关联查询条件
 *
 * @author  zhangly
 * Date: 2019年12月31日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HiveParamSelfJoinRule extends AbsHiveGrpParamRule {
	private List<String> conditionList = null;
	
	public HiveParamSelfJoinRule(String colName, String compareValue, String fromTable) {
		super(colName, compareValue, fromTable);
	}
	
	public void addCondition(String where) {
		if(conditionList==null) {
			conditionList = new ArrayList<String>();
		}
		conditionList.add(where);
	}

	@Override
	public String where() {
		if(compareValue.indexOf(",")>-1) {
			//参数存在分组，|符号分组，组内and关系，组与组之间or关联
			return parseGroupInIsAndGroupOutIsOutWhere(this.getClass());
		} else {
			return simplex(AbsHiveRuleHandle.WITH_TABLE, compareValue);
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
		sb.append(" exists(select 1 from DWS_PATIENT_1VISIT_ITEMSUM x1");
		sb.append(" where");
		if(patient) {
			sb.append(" $table.clientid=x1.clientid");
			if(beforeVisit) {
				sb.append(" and $table.visitdate>x1.visitdate");
			}
		} else {
			sb.append(" $table.visitid=x1.visitid");
		}
		sb.append(" and ").append(colName).append(" in").append(value);
		sb.append(" and ITEM_QTY>0 and ITEM_AMT>0");
		if(conditionList!=null && conditionList.size()>0) {
			sb.append(" and ").append(StringUtils.join(conditionList, " and "));
		}
		sb.append(")");
		String sql = sb.toString();
		sql = StringUtils.replace(sql, "$table", table);
		return sql;
	}
}
