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

import com.ai.modules.engine.model.EngineMapping;

/**
 * 
 * 功能描述：药品、收费、临床路径等项目组规则
 *
 * @author  zhangly
 * Date: 2019年12月31日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HiveParamGrpRule extends AbsHiveGrpParamRule {
	//组关联默认第一种{1:组内and关系，组与组之间or关联 2:组内or关系，组与组之间and关联}
	private String relation = "1";
	private List<String> conditionList = null;
	
	public HiveParamGrpRule(String tableName, String colName, String compareValue, String fromTable) {
		super(tableName, colName, compareValue, fromTable);
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
			if("1".equals(relation)) {
				//参数存在分组，|符号分组，组内and关系，组与组之间or关联
				return parseGroupInIsAndGroupOutIsOutWhere(this.getClass());
			} else {
				//参数存在分组，逗号分组，组内or关系，组与组之间and关联
				return parseGroupInIsOrGroupOutIsAndWhere(this.getClass());
			}
		} else {
			//参数中不存在‘,’分隔符（即无and关系），仅存在组与组之间or关系
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
		sb.append(" exists(select 1 from DWS_PATIENT_1VISIT_ITEMSUM x1");
		EngineMapping mapping = DWB_CHARGE_DETAIL_MAPPING.get(tableName.toUpperCase());
		sb.append(" join medical_gbdp.").append(tableName).append(" x2 on x1.").append(mapping.getTo()).append("=x2.").append(mapping.getFrom());
		sb.append(" where");
		if(patient) {
			sb.append(" $table.clientid=x1.clientid");
			if(beforeVisit) {
				sb.append(" and $table.visitdate>x1.visitdate");
			}
		} else {
			sb.append(" $table.visitid=x1.visitid");
		}
		sb.append(" and x1.ITEM_QTY>0");
		sb.append(" and x1.ITEM_AMT>0");
		sb.append(" and ");
		sb.append("x2.").append(colName).append(" in").append(value);
		if(conditionList!=null && conditionList.size()>0) {
			sb.append(" and ").append(StringUtils.join(conditionList, " and "));
		}
		sb.append(")");
		String sql = sb.toString();
		sql = StringUtils.replace(sql, "$table", table);
		return sql;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}
}
