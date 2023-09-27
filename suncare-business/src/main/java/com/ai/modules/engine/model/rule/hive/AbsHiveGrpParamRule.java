/**
 * AbsHiveParamRule.java	  V1.0   2022年11月14日 上午10:29:50
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.rule.hive;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;

public abstract class AbsHiveGrpParamRule extends AbsHiveParamRule {
	
	public AbsHiveGrpParamRule(String colName, String compareValue, String fromTable) {
		super(colName, compareValue, fromTable);
	}
	
	public AbsHiveGrpParamRule(String tableName, String colName, String compareValue, String fromTable) {
		super(tableName, colName, "=", compareValue, fromTable);
	}

	/**
	 * 
	 * 功能描述：参数存在分组，|符号分组，组内and关系，组与组之间or关联
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	public String parseGroupInIsAndGroupOutIsOutWhere(Class<?> clazz) {
		StringBuilder sb = new StringBuilder();
		List<WithTableModel> groupWithTableList = new ArrayList<WithTableModel>();
		String[] groups = StringUtils.split(compareValue, "|");		
		for(int groupNo=0, len=groups.length; groupNo<len; groupNo++) {
			String table = fromTable;
			String group = groups[groupNo];
			String[] values = StringUtils.split(group, ",");
			List<WithTableModel> withTableList = new ArrayList<WithTableModel>();
			int index = 0;
			for(String value : values) {
				value = value.trim();
				String withTable = "table_"+clazz.getSimpleName()+"_"+groupNo+index;
				String sql = simplex(table, value);
				withTableList.add(new WithTableModel(withTable, sql));
				table = withTable;
				index++;
			}
			for(int i=0,size=withTableList.size(); i<size; i++) {
				WithTableModel bean = withTableList.get(i);
				if(groupNo>0 || i>0) {
					sb.append("\n,");
				}
				if(groupNo==0 && i==0) {
					sb.append("with ");
				}
				sb.append(bean.getAlias()).append(" as(").append(bean.getSql()).append(")");
				if(i==size-1) {
					groupWithTableList.add(bean);
				}
			}
		}
		sb.append("\n,");
		String alias = "table_"+this.getClass().getSimpleName();
		sb.append(alias).append(" as(");
		for(int i=0,len=groupWithTableList.size(); i<len; i++) {
			WithTableModel bean = groupWithTableList.get(i);
			if(i>0) {
				sb.append(" union \n");
			}
			sb.append("select $field from ").append(bean.getAlias());
		}
		sb.append(")");
		sb.append("\n");
		sb.append("select * from ").append(fromTable);
		sb.append(" where $field");
		if(reverse) {
			sb.append(" not ");
		}
		sb.append(" in (select x1.$field from ").append(alias).append(" x1)");
		String sql = sb.toString();
		String field = "visitid";
		if(patient) {
			field = "clientid";
		}
		sql = StringUtils.replace(sql, "$field", field);
		return sql;
	}
	
	/**
	 * 
	 * 功能描述：参数存在分组，逗号符号分组，组内or关系，组与组之间and关联
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	public String parseGroupInIsOrGroupOutIsAndWhere(Class<?> clazz) {
		StringBuilder sb = new StringBuilder();
		List<WithTableModel> groupWithTableList = new ArrayList<WithTableModel>();
		String[] groups = StringUtils.split(compareValue, ",");
		String table = fromTable;
		for(int num=0, len=groups.length; num<len; num++) {
			String group = groups[num];
			String withTable = "table_"+clazz.getSimpleName()+"_"+num;
			String sql = simplex(table, group);
			groupWithTableList.add(new WithTableModel(withTable, sql));
			table = withTable;
		}
		for(int i=0,size=groupWithTableList.size(); i<size; i++) {
			WithTableModel bean = groupWithTableList.get(i);
			if(i>0) {
				sb.append("\n,");
			}
			if(i==0) {
				sb.append("with ");
			}
			sb.append(bean.getAlias()).append(" as(").append(bean.getSql()).append(")");
		}
		WithTableModel prev = groupWithTableList.get(groups.length-1);
		sb.append("\n");
		sb.append("select * from ").append(fromTable);
		sb.append(" where $field");
		if(reverse) {
			sb.append(" not ");
		}
		sb.append(" in (select x1.$field from ").append(prev.getAlias()).append(" x1)");
		String sql = sb.toString();
		String field = "visitid";
		if(patient) {
			field = "clientid";
		}
		sql = StringUtils.replace(sql, "$field", field);
		return sql;
	}
	
	public abstract String simplex(String table, String compareValue);
}
