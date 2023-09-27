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
 * 功能描述：适用症规则
 *
 * @author  zhangly
 * Date: 2019年12月31日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineParamIndicationRule extends AbsEngineParamRule {	
	public EngineParamIndicationRule(String colName, String compareValue) {
		super(colName, compareValue);
	}

	@Override
	public String where() {
		StringBuilder sb = new StringBuilder();
		//模型参数存在分组
		if(compareValue.indexOf(",")>-1) {
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
					value = value.trim();
					if(index>0) {
						sb.append(" AND ");
					}
					sb.append("_query_:\"");
					EngineMapping mapping = new EngineMapping("DWB_DIAG", "VISITID", "VISITID");
					if(patient) {
						//历史诊断
						mapping.setFrom("CLIENTID");
						mapping.setTo("CLIENTID");
					}
					SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
					sb.append(plugin.parse());
					sb.append("_query_:\\\"");
					plugin = new SolrJoinParserPlugin("STD_DIAGGROUP", "DISEASECODE", "DISEASECODE");
					sb.append(plugin.parse());
					sb.append("DIAGGROUP_CODE").append(":").append(value);
					sb.append("\\\"");
					sb.append(" OR ");
					sb.append("DISEASECODE:").append(value);
					sb.append("\"");
					index++;
				}
				if(values.length>1) {
					sb.append(")");
				}
			}
		} else {
			String value = "(" + StringUtils.replace(compareValue, "|", " OR ") + ")";
			sb.append("_query_:\"");			
			EngineMapping mapping = new EngineMapping("DWB_DIAG", "VISITID", "VISITID");
			if(patient) {
				//历史诊断
				mapping.setFrom("CLIENTID");
				mapping.setTo("CLIENTID");
			}
			SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
			sb.append(plugin.parse());
			sb.append("_query_:\\\"");
			plugin = new SolrJoinParserPlugin("STD_DIAGGROUP", "DISEASECODE", "DISEASECODE");
			sb.append(plugin.parse());
			sb.append("DIAGGROUP_CODE").append(":").append(value);
			sb.append("\\\"");
			sb.append(" OR ");
			sb.append("DISEASECODE:").append(value);
			sb.append("\"");
		}
		
		if(reverse) {
			return "*:* -("+sb.toString()+")";
		} else {
			return sb.toString();
		}
	}		
}
