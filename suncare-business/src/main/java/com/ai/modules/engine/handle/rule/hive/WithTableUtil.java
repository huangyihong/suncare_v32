/**
 * WithTableUtil.java	  V1.0   2022年11月11日 上午9:52:46
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.hive;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.handle.rule.hive.model.GroupWithTableModel;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.solr.HiveJDBCUtil;

public class WithTableUtil {
	
	/**
	 * 
	 * 功能描述：解析组内查询条件
	 *
	 * @author  zhangly
	 *
	 * @param withTableList
	 * @return
	 */
	public static String parseWithTableList(String fromTable, List<WithTableModel> withTableList, String field) {
		if(withTableList==null || withTableList.size()==0) {
			return null;
		}
		if(withTableList.size()==1) {
			return withTableList.get(0).getSql();
		}
		StringBuilder sb = new StringBuilder();
		Set<String> logicSet = new HashSet<String>();
		for(int i=0,len=withTableList.size(); i<len; i++) {
			WithTableModel bean = withTableList.get(i);
			if(i>0) {
				sb.append("\n,");
				logicSet.add(bean.getLogic().toLowerCase());
			}
			if(i==0) {
				sb.append("with ");
			}
			sb.append(bean.getAlias()).append(" as(").append(bean.getSql()).append(")");
		}
		if(logicSet.size()==1) {
			//组内仅存在一种关系
			String logic = logicSet.iterator().next();
			if("or".equals(logic)) {
				//组内or关系
				sb.append("\n,");
				String alias = fromTable+"_union";
				sb.append(alias).append(" as(");
				for(int i=0,len=withTableList.size(); i<len; i++) {
					WithTableModel bean = withTableList.get(i);
					if(i>0) {
						sb.append(" union ");
					}
					sb.append("select $field from ").append(bean.getAlias());
				}
				sb.append(")");
				sb.append("\n");
				sb.append("select * from ").append(fromTable);
				sb.append(" where ");
				sb.append(fromTable).append(".$field in(");
				sb.append("select $field from ").append(alias);
				sb.append(")");
			} else {
				//组内and关系
				if(HiveJDBCUtil.enabledProcessGp()) {
					//gp计算模式使用交集运算符
					sb.append("\n,");
					String alias = fromTable+"_intersect";
					sb.append(alias).append(" as(");
					for(int i=0,len=withTableList.size(); i<len; i++) {
						WithTableModel bean = withTableList.get(i);
						if(i>0) {
							sb.append(" intersect ");
						}
						sb.append("select $field from ").append(bean.getAlias());
					}
					sb.append(")");
					sb.append("\n");
					sb.append("select * from ").append(fromTable);
					sb.append(" where ");
					sb.append(fromTable).append(".$field in(");
					sb.append("select $field from ").append(alias);
					sb.append(")");
				} else {
					sb.append("\n");
					sb.append("select * from ").append(fromTable);
					sb.append(" where ");
					for(int i=0,len=withTableList.size(); i<len; i++) {
						WithTableModel bean = withTableList.get(i);
						if(i>0) {
							sb.append(" and ");
						}
						sb.append(fromTable).append(".$field in(");
						sb.append("select $field from ").append(bean.getAlias());
						sb.append(")");
					}
				}
			}
		} else {
			String alias = fromTable+"_logic_1";
			sb.append("\n,");
			WithTableModel first = withTableList.get(0);
			WithTableModel bean = withTableList.get(1);
			String logic = bean.getLogic().toLowerCase();
			if("or".equals(logic)) {
				sb.append(alias).append(" as(");
				sb.append("select $field from ").append(first.getAlias());
				sb.append(" intersect ");
				sb.append("select $field from ").append(bean.getAlias());
				sb.append(")");
			} else {
				sb.append(alias).append(" as(");
				sb.append("select $field from ").append(first.getAlias());
				sb.append(" where $field in(");
				sb.append("select $field from ").append(bean.getAlias());
				sb.append(")");
				sb.append(")");
			}
			for(int i=2,len=withTableList.size(); i<len; i++) {
				sb.append("\n,");
				String current = fromTable+"_logic_"+i;
				bean = withTableList.get(i);
				logic = bean.getLogic().toLowerCase();
				if("or".equals(logic)) {
					sb.append(current).append(" as(");
					sb.append("select $field from ").append(alias);
					sb.append(" intersect ");
					sb.append("select $field from ").append(bean.getAlias());
					sb.append(")");
				} else {
					sb.append(current).append(" as(");
					sb.append("select $field from ").append(alias);
					sb.append(" where $field in(");
					sb.append("select $field from ").append(bean.getAlias());
					sb.append(")");
					sb.append(")");
				}
				alias = current;
			}
			sb.append("\n");
			sb.append("select * from ").append(fromTable);
			sb.append(" where ");
			sb.append(fromTable).append(".$field in(");
			sb.append("select $field from ").append(alias);
			sb.append(")");
		}
		String sql = sb.toString();
		sql = StringUtils.replace(sql, "$field", field);
		return sql;
	}
	
	public static String parseWithTableList(String fromTable, List<WithTableModel> withTableList) {
		return parseWithTableList(fromTable, withTableList, "visitid");
	}
	
	public static WithTableModel parseWithTableList(String alias, String logic, String fromTable, List<WithTableModel> withTableList) {
		return new WithTableModel(alias, parseWithTableList(fromTable, withTableList), logic);
	}
	
	/**
	 * 
	 * 功能描述：解析所有组查询条件
	 *
	 * @author  zhangly
	 *
	 * @param groupWithTableList
	 * @return
	 */
	public static String parseGroupWithTableList(String fromTable, List<GroupWithTableModel> groupWithTableList) {
		if(groupWithTableList==null || groupWithTableList.size()==0) {
			return null;
		}
		if(groupWithTableList.size()==1) {
			GroupWithTableModel groupWithTable = groupWithTableList.get(0);
			WithTableModel bean = groupWithTable.parseCondition();
			return bean.getSql();
		}
		Set<String> logicSet = new HashSet<String>();
		StringBuilder sb = new StringBuilder();
		for(int i=0,len=groupWithTableList.size(); i<len; i++) {
			GroupWithTableModel groupWithTable = groupWithTableList.get(i);
			WithTableModel bean = groupWithTable.parseCondition();
			if(i>0) {
				sb.append("\n,");
				logicSet.add(bean.getLogic().toLowerCase());
			}
			if(i==0) {
				sb.append("with ");
			}
			sb.append(bean.getAlias()).append(" as(").append(bean.getSql()).append(")");
		}
		if(logicSet.size()==1) {
			String logic = logicSet.iterator().next();
			if("or".equals(logic)) {
				//组内or关系
				sb.append("\n,");
				GroupWithTableModel groupWithTable = groupWithTableList.get(0);
				WithTableModel bean = groupWithTable.parseCondition();
				String alias = "tmp_".concat(bean.getAlias());
				sb.append(alias).append(" as(");
				for(int i=0,len=groupWithTableList.size(); i<len; i++) {
					groupWithTable = groupWithTableList.get(i);
					bean = groupWithTable.parseCondition();
					if(i>0) {
						sb.append(" union ");
					}
					sb.append(fromTable).append(".$field in(");
					sb.append("select $field from ").append(bean.getAlias());
					sb.append(")");
				}
				sb.append(")");
				sb.append("\n");
				sb.append("select * from ").append(fromTable);
				sb.append(" where ");
				sb.append(fromTable).append(".$field in(");
				sb.append("select $field from ").append(alias);
				sb.append(")");
			} else {
				//组内and关系
				sb.append("select * from ").append(fromTable);
				sb.append(" where ");
				for(int i=0,len=groupWithTableList.size(); i<len; i++) {
					GroupWithTableModel groupWithTable = groupWithTableList.get(i);
					WithTableModel bean = groupWithTable.parseCondition();
					if(i>0) {
						sb.append(" and ");
					}
					sb.append(fromTable).append(".$field in(");
					sb.append("select $field from ").append(bean.getAlias());
					sb.append(")");
				}
			}
		}
		String sql = sb.toString();
		sql = StringUtils.replace(sql, "$field", "visitid");
		return sql;
	}
	
	public static String buildWithTable(MedicalRuleConditionSet condition) {
		Integer groupNo = condition.getGroupNo();
		groupNo = groupNo==null ? 0 : groupNo;
		Integer orderNo = condition.getOrderNo();
		orderNo = orderNo==null ? 0 : orderNo;
		return "table_".concat(condition.getType()).concat("_").concat(groupNo.toString()).concat(orderNo.toString());
	}
}
