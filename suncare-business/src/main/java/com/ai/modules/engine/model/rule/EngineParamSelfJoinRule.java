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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;

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
public class EngineParamSelfJoinRule extends AbsEngineParamRule {
	private List<String> conditionList = null;
	
	public EngineParamSelfJoinRule(String colName, String compareValue) {
		super(colName, compareValue);
	}
	
	public void addCondition(String where) {
		if(conditionList==null) {
			conditionList = new ArrayList<String>();
		}
		conditionList.add(where);
	}

	@Override
	public String where() {
		StringBuilder sb = new StringBuilder();
		//参数存在分组
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
					if(index>0) {
						sb.append(" AND ");
					}
					sb.append(appendCondition(value));
					index++;
				}
				if(values.length>1) {
					sb.append(")");
				}
			}
		} else {
			String value = "(" + StringUtils.replace(compareValue, "|", " OR ") + ")";
			sb.append(appendCondition(value));
		}
		
		if(reverse) {
			return "*:* -("+sb.toString()+")";
		} else {
			return sb.toString();
		}
	}		
	
	private String appendCondition(String value) {
		StringBuilder sb = new StringBuilder();
		sb.append("_query_:\"");
		EngineMapping mapping = new EngineMapping("DWS_PATIENT_1VISIT_ITEMSUM", "VISITID", "VISITID");
		if(patient) {
			mapping.setFrom("CLIENTID");
			mapping.setTo("CLIENTID");
		}
		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
		sb.append(plugin.parse());
		sb.append(colName).append(":").append(value);
		sb.append(" AND ITEM_QTY:{0 TO *} AND ITEM_AMT:{0 TO *}");
		if(conditionList!=null && conditionList.size()>0) {
			for(String where : conditionList) {
				sb.append(" AND ").append(where);
			}
		}
		sb.append("\"");
		return sb.toString();
	}
}
