/**
 * EngineNodeRuleHandler.java	  V1.0   2020年4月9日 上午11:23:06
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.node;

import com.ai.modules.engine.model.EngineNodeRule;

public class ExpressionNodeRuleHandle extends AbsNodeRuleHandle {
	
	public ExpressionNodeRuleHandle(EngineNodeRule rule) {
		super(rule);
	}
	
	public String handler() {
		StringBuilder sb = new StringBuilder();
		
		String colName = rule.getColName().toUpperCase();
		String compareType = rule.getCompareType();
		//虚拟字段
		if("=".equals(compareType)) {
			sb.append("_val_:");
			if(this.isJoin()) {
				sb.append("\\");
			}
			sb.append("\"{!frange l=").append(rule.getCompareValue()).append(" u=").append(rule.getCompareValue()).append(" incu=true}").append(rule.getColConfig().getColValueExpressionSolr());
			if(this.isJoin()) {
				sb.append("\\");
			}
			sb.append("\"");
		} else if (compareType.equals(">")) {
			// 大于
			sb.append("_val_:");
			if(this.isJoin()) {
				sb.append("\\");
			}
			sb.append("\"{!frange l=").append(rule.getCompareValue()).append(" incl=false}").append(rule.getColConfig().getColValueExpressionSolr());
			if(this.isJoin()) {
				sb.append("\\");
			}
			sb.append("\"");
		} else if (compareType.equals(">=")) {
			// 大于等于
			sb.append("_val_:");
			if(this.isJoin()) {
				sb.append("\\");
			}
			sb.append("\"{!frange l=").append(rule.getCompareValue()).append("}").append(rule.getColConfig().getColValueExpressionSolr());
			if(this.isJoin()) {
				sb.append("\\");
			}
			sb.append("\"");
		} else if (compareType.equals("<")) {
			// 小于
			sb.append("_val_:");
			if(this.isJoin()) {
				sb.append("\\");
			}
			sb.append("\"{!frange u=").append(rule.getCompareValue()).append("}").append(rule.getColConfig().getColValueExpressionSolr());
			if(this.isJoin()) {
				sb.append("\\");
			}
			sb.append("\"");
		} else if (compareType.equals("<=")) {
			// 小于等于
			sb.append("_val_:");
			if(this.isJoin()) {
				sb.append("\\");
			}
			sb.append("\"{!frange u=").append(rule.getCompareValue()).append(" incu=true}").append(rule.getColConfig().getColValueExpressionSolr());
			if(this.isJoin()) {
				sb.append("\\");
			}
			sb.append("\"");
		} else {
			sb.append("*:*");
		}
		return sb.toString();
	}
}
