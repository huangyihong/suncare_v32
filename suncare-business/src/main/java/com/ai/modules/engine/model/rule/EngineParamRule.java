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
import com.ai.modules.engine.util.EngineUtil;

/**
 * 
 * 功能描述：药品、收费、临床路径等模型参数
 *
 * @author  zhangly
 * Date: 2019年12月31日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineParamRule extends AbsEngineParamRule {	
	public EngineParamRule(String tableName, String colName, String compareValue) {
		super(tableName, colName, compareValue);
	}
	
	public EngineParamRule(String tableName, String colName, String compareType, String compareValue) {
		super(tableName, colName, compareType);
		this.compareValue = compareValue;
	}

	@Override
	public String where() {
		if(StringUtils.isBlank(compareValue)) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		boolean join = isJoin();
		if(join) {
			EngineMapping mapping = DWB_CHARGE_DETAIL_MAPPING.get(tableName.toUpperCase());
			if(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM.equalsIgnoreCase(tableName) && patient) {
				mapping.setFrom("CLIENTID");
				mapping.setTo("CLIENTID");
			}
			sb.append("_query_:\"");
			SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
			sb.append(plugin.parse());
		}
		if("=".equals(compareType)) {
			if(compareValue.indexOf("|")==-1) {
				sb.append(colName).append(":").append(compareValue);
			} else {
				String[] values = StringUtils.split(compareValue, "|");
				sb.append(colName).append(":(");
				int index = 0;
				for(String value : values) {
					if(index>0) {
						sb.append(" OR ");
					}
					sb.append(value);
					index++;
				}
				sb.append(")");
			}
		} else if (compareType.equals(">")) {
			// 大于
			sb.append(colName).append(":{").append(compareValue).append(" TO *}");
		} else if (compareType.equals(">=")) {
			// 大于等于
			sb.append(colName).append(":[").append(compareValue).append(" TO *]");
		} else if (compareType.equals("<")) {
			// 小于
			sb.append(colName).append(":{* TO ").append(compareValue).append("}");
		} else if (compareType.equals("<=")) {
			// 小于等于
			sb.append(":[* TO ").append(compareValue).append("]");
		} else if (compareType.equals("<>")) {
			// 不等于
			sb.append("-(").append(colName).append(":").append(compareValue).append(")");
		} else {
			sb.append(colName).append(":").append(compareValue);
		}
		if(join) {
			sb.append("\"");
		}
		
		if(reverse) {
			return "*:* -("+sb.toString()+")";
		} else {
			return sb.toString();
		}
	}	
}
