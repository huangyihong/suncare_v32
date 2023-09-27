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
 * 功能描述：收费合规项目组规则，组之间仅包含或关系
 *
 * @author  zhangly
 * Date: 2019年12月31日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineParamChargeGrpRule extends AbsEngineParamRule {
	//药品编码或者收费项目编码
	private String itemCode;
	//过滤条件
	private List<String> conditionList = null;
	
	public EngineParamChargeGrpRule(String colName, String compareValue) {
		super(colName, compareValue);
	}
	
	public void addCondition(String where) {
		if(conditionList==null) {
			conditionList = new ArrayList<String>();
		}
		conditionList.add(where);
	}
	
	public EngineParamChargeGrpRule(String itemCode, String tableName, String colName, String compareValue) {
		super(tableName, colName, compareValue);
		this.itemCode = itemCode;
	}

	@Override
	public String where() {
		if(itemCode.indexOf(",")>-1) {
			itemCode = "(" + StringUtils.replace(itemCode, ",", " OR ") + ")";
		}
		boolean join = this.isJoin();
		StringBuilder sb = new StringBuilder();
		sb.append("_query_:\"");
		EngineMapping mapping = new EngineMapping("DWS_PATIENT_1VISIT_ITEMSUM", "VISITID", "VISITID");
		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
		sb.append(plugin.parse());
		if(join) {
			mapping = DWB_CHARGE_DETAIL_MAPPING.get(tableName.toUpperCase());
			sb.append("_query_:\\\"");
			plugin = SolrJoinParserPlugin.build(mapping);
			sb.append(plugin.parse());
		}
		sb.append(colName).append(":");
		String[] values = StringUtils.split(compareValue, "|");
		int index = 0;
		if(values.length>1) {
			sb.append("(");
		}
		for(String value : values) {			
			if(index>0) {
				sb.append(" OR ");
			}
			sb.append(value);						
			index++;						
		}
		if(values.length>1) {
			sb.append(")");
		}		
		sb.append("\\\" AND ITEM_QTY:{0 TO *} AND ITEM_AMT:{0 TO *}");
		if(conditionList!=null && conditionList.size()>0) {
			for(String where : conditionList) {
				sb.append(" AND ").append(where);
			}
		}
		sb.append(" AND -ITEMCODE:").append(itemCode);
		sb.append("\"");
		if(reverse) {
			return "*:* -("+sb.toString()+")";
		} else {
			return sb.toString();
		}
	}

	public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}		
}
