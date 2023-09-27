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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.config.entity.MedicalColConfig;
import com.ai.modules.engine.model.EngineConstant;
import com.ai.modules.engine.model.EngineDwsMapping;
import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.EngineMiddleDwsMapping;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.PlaceholderResolverUtil;
import com.ai.solr.HiveJDBCUtil;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.hive.visitor.HiveSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;

public class HiveDwsNodeRuleHandle extends AbsHiveDwsNodeRuleHandle {

	public HiveDwsNodeRuleHandle(EngineNodeRule rule, String fromTable, String alias) {
		super(rule, fromTable, alias);
	}
	
	public HiveDwsNodeRuleHandle(EngineNodeRule rule, String fromTable) {
		super(rule, fromTable);
	}

	@Override
	public String script() {
		String sql = this.template();
		sql = StringUtils.replace(sql, "$where", this.where(false));
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
		if(EngineUtil.ENGIME_MAPPING.containsKey(tableName)) {
			//直接关联dws_master_info
			EngineMapping mapping = EngineUtil.ENGIME_MAPPING.get(tableName);
			sb.append("select * from $table");
			sb.append(" where ");
			if(hasReverse()) {
				sb.append(" not");
			}
			sb.append(" exists(select 1");
			sb.append(" from ").append(tableName);
			sb.append(" where $table.").append(mapping.getTo()).append("=").append(tableName).append(".").append(mapping.getFrom());
			sb.append(" and $where");
			sb.append(")");
		} else if(EngineConstant.ENGINE_DWS_MAPPING.containsKey(tableName)) {
			//关联dws_master_info_ids，需要解析节点查询条件获得数据周期关联字段
			//是否存在出院指标
			boolean leave = false;
			MedicalColConfig config = rule.getColConfig();
			if(config!=null && config.getIsLeaveHospCol()!=null && config.getIsLeaveHospCol()==1) {
				leave = true;
			}
			EngineDwsMapping mapping = EngineConstant.ENGINE_DWS_MAPPING.get(tableName);
			String from = mapping.getFrom();
			from = "ID".equalsIgnoreCase(from) ? "id" : from;
			String to = mapping.getToTemplate();
			Properties properties = new Properties();
		    properties.put("durationType", durationType);
		    String duration = null;
		    if(HiveJDBCUtil.enabledProcessGp()) {
		    	if("日".equals(durationType)) {
			    	duration = "to_char(visitdate, 'yyyy-MM-dd')";
			    } else if("周".equals(durationType)) {
			    	duration = "concat(to_char(visitdate, 'yyyy'), '年第', weekofyear(visitdate), '周')";
			    } else if("月".equals(durationType)) {
			    	duration = "to_char(visitdate, 'yyyy-MM')";
			    } else if("季".equals(durationType)) {
			    	
			    } else if("年".equals(durationType)) {
			    	duration = "to_char(visitdate, 'yyyy')";
			    }
			    if(leave) {
			    	duration = StringUtils.replace(duration, "visitdate", "leavedate");
			    }
			    to = StringUtils.replace(to, "nvl(", "COALESCE(");
		    } else {
		    	if("日".equals(durationType)) {
			    	duration = "from_unixtime(unix_timestamp(visitdate, 'yyyy-MM-dd'), 'yyyy-MM-dd')";
			    } else if("周".equals(durationType)) {
			    	duration = "concat(from_unixtime(unix_timestamp(visitdate, 'yyyy-MM-dd'), 'yyyy'), '年第', weekofyear(visitdate), '周')";
			    } else if("月".equals(durationType)) {
			    	duration = "from_unixtime(unix_timestamp(visitdate, 'yyyy-MM-dd'), 'yyyy-MM')";
			    } else if("季".equals(durationType)) {
			    	
			    } else if("年".equals(durationType)) {
			    	duration = "from_unixtime(unix_timestamp(visitdate, 'yyyy-MM-dd'), 'yyyy')";
			    }
			    if(leave) {
			    	duration = StringUtils.replace(duration, "visitdate", "leavedate");
			    }
		    }
		    properties.put("duration", duration);
		    to = PlaceholderResolverUtil.replacePlaceholders(to, properties);
			sb.append("select * from $table");
			sb.append(" where ");
			if(hasReverse()) {
				sb.append(" not");
			}
			sb.append(" exists(select 1");
			sb.append(" from ").append(tableName);
			sb.append(" where ").append(to).append("=").append(tableName).append(".").append(from);
			sb.append(" and $where");
			sb.append(")");
		} else if(EngineConstant.ENGINE_MIDDLE_DWS_MAPPING.containsKey(tableName)) {
			//中间表关联查询
			EngineMiddleDwsMapping mapping = EngineConstant.ENGINE_MIDDLE_DWS_MAPPING.get(tableName);
			EngineMapping dws = mapping.getDws();
			String to = mapping.getToTemplate();
			if(to==null) {
				EngineMapping middle = mapping.getMiddle();
				sb.append("select * from $table");
				sb.append(" where ");
				if(hasReverse()) {
					sb.append(" not");
				}
				sb.append(" exists(select 1");
				sb.append(" from ").append(middle.getFromIndex()).append(",").append(tableName);
				sb.append(" where $table.").append(middle.getTo()).append("=").append(middle.getFromIndex()).append(".").append(middle.getFrom());
				sb.append(" and ").append(dws.getFromIndex()).append(".").append(dws.getTo()).append("=").append(tableName).append(".").append(dws.getFrom());
				sb.append(" and $where");
				sb.append(")");
			} else {
				String from = dws.getFrom();
				from = "ID".equalsIgnoreCase(from) ? "id" : from;
				Properties properties = new Properties();
			    properties.put("durationType", durationType);
			    String duration = this.parseDuration();
			    if(HiveJDBCUtil.enabledProcessGp()) {
			    	to = StringUtils.replace(to, "nvl(", "COALESCE(");
			    }
			    properties.put("duration", duration);
			    to = PlaceholderResolverUtil.replacePlaceholders(to, properties);
			    //找出dws表id字段模板使用到的列名
			    Set<String> fieldSet = this.parseTemplate(to);
			    for(String field : fieldSet) {
			    	//增加列别名
			    	to = StringUtils.replace(to, field, "dwb_charge_detail.".concat(field));
			    }
			    
				sb.append("select * from $table");
				sb.append(" where ");
				if(hasReverse()) {
					sb.append(" not");
				}
				sb.append(" exists(select 1");
				sb.append(" from dwb_charge_detail,").append(tableName);
				sb.append(" where $table.visitid=dwb_charge_detail.visitid and ").append(to).append("=").append(tableName).append(".").append(from);
				sb.append(" and $where");
				sb.append(")");
			}
		}
		String sql = sb.toString();
		sql = StringUtils.replace(sql, "$table", fromTable);
		return sql;
	}
	
	/**
	 * 
	 * 功能描述：解析sql获取查询的列名
	 *
	 * @author  zhangly
	 *
	 * @param to
	 * @return
	 */
	private Set<String> parseTemplate(String to) {
		Set<String> fieldSet = new HashSet<String>();
		String sql = "select " + to + " from dwb_charge_detail";
		String dbType = JdbcConstants.HIVE;
		List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);
		for (int i = 0; i < stmtList.size(); i++) {
			SQLStatement stmt = stmtList.get(i);
			HiveSchemaStatVisitor visitor = new HiveSchemaStatVisitor();
			stmt.accept(visitor);
			Collection<TableStat.Column> columns = visitor.getColumns();
			columns.stream().forEach(row->{
				if(row.isSelect()) {
					fieldSet.add(row.getName());
				}
			});
		}
		return fieldSet;
	}
}
