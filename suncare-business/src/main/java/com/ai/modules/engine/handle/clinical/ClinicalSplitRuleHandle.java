/**
 * EngineNodeRuleHandler.java	  V1.0   2020年4月9日 上午11:23:06
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.clinical;

/**
 * 
 * 功能描述：多值
 *
 * @author  zhangly
 * Date: 2020年4月23日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class ClinicalSplitRuleHandle extends AbsClinicalRuleHandle {

	public ClinicalSplitRuleHandle(String tableName, String colName, String compareValue) {
		super(tableName, colName, compareValue);
	}
	
	protected String handler() {
		StringBuilder sb = new StringBuilder();
		
		if(compareValue.indexOf(",")==-1) {
			sb.append(colName).append(":").append(compareValue);
		} else {
			String[] array = compareValue.split(",");
			sb.append(colName).append(":");
			sb.append("(");
			for(int i=0, len=array.length; i<len; i++) {
				if(i>0) {
					sb.append(" OR ");
				}
				sb.append(array[i]);
			}
			sb.append(")");
		}
		return sb.toString();
	}
}
