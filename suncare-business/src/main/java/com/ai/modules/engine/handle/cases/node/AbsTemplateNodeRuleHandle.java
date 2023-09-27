/**
 * AbsTemplateNodeRuleHandle.java	  V1.0   2022年12月3日 下午8:41:13
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.node;

import com.ai.modules.engine.model.EngineNodeRule;

public abstract class AbsTemplateNodeRuleHandle extends AbsNodeRuleHandle {
	
	public AbsTemplateNodeRuleHandle(EngineNodeRule rule) {
		super(rule);
	}

	public abstract String template();
	
	protected String condition() {
		StringBuilder sb = new StringBuilder();
		String colName = rule.getColName().toUpperCase();
		String compareType = rule.getCompareType();		
		if("=".equals(compareType) || "regx".equals(compareType)) {
			sb.append(colName).append(":").append(rule.getCompareValue());
		} else if(compareType.equalsIgnoreCase("notin") || compareType.equals("<>")) {
			sb.append(colName).append(":").append(rule.getCompareValue());
		} else if (compareType.equalsIgnoreCase("like")) {
			// 包含
			sb.append(colName).append(":*").append(rule.getCompareValue()).append("*");
		} else if (compareType.equalsIgnoreCase("llike")) {
			// 以..开始
			sb.append(colName).append(":").append(rule.getCompareValue()).append("*");
		} else if (compareType.equalsIgnoreCase("rlike")) {
			// 以..结尾
			sb.append(colName).append(":*").append(rule.getCompareValue());
		} else if (compareType.equalsIgnoreCase("notlike")) {
			// 不包含
			sb.append(colName).append(":*").append(rule.getCompareValue()).append("*");
		}
		return sb.toString();
	}
	
	@Override
	protected boolean hasReverse() {
		String compareType = rule.getCompareType();
		return compareType.equalsIgnoreCase("notin") || compareType.equals("<>") || compareType.equalsIgnoreCase("notlike");
	}
}
