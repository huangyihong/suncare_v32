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

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.util.PlaceholderResolverUtil;
import com.ai.solr.HiveJDBCUtil;

public class HiveDwsDiseaseTreatdistNodeRuleHandle extends AbsHiveDwsNodeRuleHandle {

	public HiveDwsDiseaseTreatdistNodeRuleHandle(EngineNodeRule rule, String fromTable, String alias) {
		super(rule, fromTable, alias);
	}
	
	public HiveDwsDiseaseTreatdistNodeRuleHandle(EngineNodeRule rule, String fromTable) {
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
		//中间表关联查询
		String to = "concat_ws('_',nvl(dwb_charge_detail.etl_source,''),'${durationType}',nvl(dwb_diag.diseasecode,''),nvl(dwb_diag.diseasename,''),nvl(dwb_charge_detail.itemcode,''),nvl(dwb_charge_detail.itemname,''))";
		String from = "id";
		Properties properties = new Properties();
	    properties.put("durationType", durationType);
	    String duration = this.parseDuration();
	    duration = StringUtils.replace(duration, "visitdate", "dwb_charge_detail.visitdate");
	    if(HiveJDBCUtil.enabledProcessGp()) {
	    	to = StringUtils.replace(to, "nvl(", "COALESCE(");
	    }
	    properties.put("duration", duration);
	    to = PlaceholderResolverUtil.replacePlaceholders(to, properties);
	    
		sb.append("select * from $table");
		sb.append(" where ");
		if(hasReverse()) {
			sb.append(" not");
		}
		sb.append(" exists(select 1");
		sb.append(" from dwb_diag,dwb_charge_detail,DWS_DISEASE_TREATDIST");
		sb.append(" where $table.visitid=dwb_diag.visitid and $table.visitid=dwb_charge_detail.visitid and ").append(to).append("=").append("DWS_DISEASE_TREATDIST.").append(from);
		sb.append(" and $where");
		sb.append(")");
		String sql = sb.toString();
		sql = StringUtils.replace(sql, "$table", fromTable);
		return sql;
	}
}
