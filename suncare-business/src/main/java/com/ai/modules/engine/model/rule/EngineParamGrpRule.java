/**
 * EngineParamRule.java	  V1.0   2019年12月31日 下午5:23:44
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.rule;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;

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
public class EngineParamGrpRule extends AbsEngineParamRule {
	//组关联默认第一种{1:组内and关系，组与组之间or关联 2:组内or关系，组与组之间and关联}
	private String relation = "1";
	
	public EngineParamGrpRule(String colName, String compareValue) {
		super(colName, compareValue);
	}
	
	public EngineParamGrpRule(String tableName, String colName, String compareValue) {
		super(tableName, colName, compareValue);
	}

	@Override
	public String where() {
		boolean join = this.isJoin();
		StringBuilder sb = new StringBuilder();
		//模型参数存在分组
		if(compareValue.indexOf(",")>-1) {
			if("1".equals(relation)) {
				//组内and关系，组与组之间or关联
				String[] groups = StringUtils.split(compareValue, "|");		
				for(int num=0, len=groups.length; num<len; num++) {
					String group = groups[num];
					if(num>0) {
						sb.append(" OR ");
					}
					String[] values = StringUtils.split(group, ",");
					if(values.length>1) {
						sb.append("(");
					}
					int index = 0;
					for(String value : values) {
						if(index>0) {
							sb.append(" AND ");
						}
						sb.append(appendCondition(join, value));
						index++;
					}
					if(values.length>1) {
						sb.append(")");
					}
				}
			} else {
				//组内or关系，组与组之间and关联
				String[] groups = StringUtils.split(compareValue, ",");		
				for(int num=0, len=groups.length; num<len; num++) {
					String group = groups[num];
					if(num>0) {
						sb.append(" AND ");
					}
					String value = "(" + StringUtils.replace(group, "|", " OR ") + ")";
					sb.append(appendCondition(join, value));					
				}
			}
		} else {
			String value = "(" + StringUtils.replace(compareValue, "|", " OR ") + ")";
			sb.append(appendCondition(join, value));
		}
		
		if(reverse) {
			return "*:* -("+sb.toString()+")";
		} else {
			return sb.toString();
		}
	}		
	
	private String appendCondition(boolean join, String value) {
		StringBuilder sb = new StringBuilder();
		sb.append("_query_:\"");
		EngineMapping mapping = new EngineMapping("DWS_PATIENT_1VISIT_ITEMSUM", "VISITID", "VISITID");
		if(patient) {
			mapping.setFrom("CLIENTID");
			mapping.setTo("CLIENTID");
		}		
		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
		sb.append(plugin.parse());
		if(join) {
			mapping = DWB_CHARGE_DETAIL_MAPPING.get(tableName.toUpperCase());
			sb.append("_query_:\\\"");
			plugin = SolrJoinParserPlugin.build(mapping);
			sb.append(plugin.parse());
		}
		sb.append(colName).append(":").append(value);
		sb.append("\\\" AND ITEM_QTY:{0 TO *} AND ITEM_AMT:{0 TO *}");
		sb.append("\"");
		return sb.toString();
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}
}
