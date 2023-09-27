/**
 * BaseEngineParamRule.java	  V1.0   2019年12月31日 下午5:36:05
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.rule.hive;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.util.EngineUtil;

import lombok.Data;

@Data
public class BaseHiveParamRule extends AbsHiveParamRule {
	
	public BaseHiveParamRule() {
		
	}
	
	public BaseHiveParamRule(String colName, String compareType, String compareValue, String fromTabel) {
		super(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, colName, compareType, compareValue, fromTabel);
	}
	
	public BaseHiveParamRule(String colName, String compareValue, String fromTabel) {
		super(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, colName, compareValue, fromTabel);
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
				sb.append(colName);
				if(reverse) {
					sb.append("<>");
				} else {
					sb.append("=");
				}
				sb.append("'").append(compareValue).append("'");
			} else {
				String value = "('" + StringUtils.split(compareValue, "','") + "')";
				sb.append(colName);
				if(reverse) {
					sb.append(" not");
				}
				sb.append(" in").append(value);
			}
		} else if (compareType.equals(">")) {
			// 大于
			sb.append(colName);
			if(reverse) {
				sb.append("<=");
			} else {
				sb.append(">");
			}
			sb.append(compareValue);
		} else if (compareType.equals(">=")) {
			// 大于等于
			sb.append(colName);
			if(reverse) {
				sb.append("<");
			} else {
				sb.append(">=");
			}
			sb.append(">=").append(compareValue);
		} else if (compareType.equals("<")) {
			// 小于
			sb.append(colName);
			if(reverse) {
				sb.append(">=");
			} else {
				sb.append("<");
			}
			sb.append(compareValue);
		} else if (compareType.equals("<=")) {
			// 小于等于
			sb.append(colName);
			if(reverse) {
				sb.append(">");
			} else {
				sb.append("<=");
			}
			sb.append(compareValue);
		} else if (compareType.equals("<>")) {
			// 不等于
			sb.append(colName);
			if(reverse) {
				sb.append("=");
			} else {
				sb.append("<>");
			}
			sb.append(compareValue);
		} else {
			sb.append(colName);
			if(reverse) {
				sb.append("<>");
			} else {
				sb.append("=");
			}
			sb.append("'").append(compareValue).append("'");
		}
		
		return sb.toString();
	}
}
