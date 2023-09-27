/**
 * BaseEngineParamRule.java	  V1.0   2019年12月31日 下午5:36:05
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.rule;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.util.EngineUtil;

import lombok.Data;

@Data
public class BaseEngineParamRule extends AbsEngineParamRule {
	
	public BaseEngineParamRule() {
		
	}
	
	public BaseEngineParamRule(String colName, String compareType, String compareValue) {
		super(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, colName, compareType, compareValue);
	}
	
	public BaseEngineParamRule(String colName, String compareValue) {
		super(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, colName, "=", compareValue);
	}
	
	/**
	 * 
	 * 功能描述：解析查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年12月31日 下午5:37:17</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public String where() {
		if(StringUtils.isBlank(compareValue)) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
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
			sb.append(colName).append(":[* TO ").append(compareValue).append("]");
		} else if (compareType.equals("<>")) {
			// 不等于
			sb.append("-(").append(colName).append(":").append(compareValue).append(")");
		} else {
			sb.append(colName).append(":").append(compareValue);
		}
		
		if(reverse) {
			return "*:* -("+sb.toString()+")";
		} else {
			return sb.toString();
		}
	}
}
