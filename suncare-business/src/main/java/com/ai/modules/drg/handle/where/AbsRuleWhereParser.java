/**
 * AbsRuleScriptHandler.java	  V1.0   2023年2月16日 上午9:01:31
 *
 * Copyright (c) 2023 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.drg.handle.where;

import com.ai.modules.drg.entity.DrgRuleLimites;
import com.ai.modules.ybChargeSearch.constants.DcConstants;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleWhere;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.superSearch.QueryRuleEnum;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsRuleWhereParser {

	protected DrgRuleLimites ruleLimites;

	public AbsRuleWhereParser(DrgRuleLimites ruleLimites) {
		this.ruleLimites = ruleLimites;
	}
	
	public String parse() {
		List<String> result = new ArrayList<>();
		List<RuleWhere> whereList = parseRuleWhere();
		for(RuleWhere ruleWhere : whereList) {
			result.add(this.parseRuleWhere(ruleWhere));
		}
		String logic = "and";
		if(StringUtils.isNotBlank(ruleLimites.getCompareLogic())) {
			logic = ruleLimites.getCompareLogic();
		}
		String where = StringUtils.join(result, " "+logic+" ");
		if(result.size()>1) {
			where = "(" + where + ")";
		}
		return where;
	}

	/**
	 *
	 * 功能描述：规则限制内容解析
	 * @author zhangly
	 * @date 2023-02-16 14:58:25
	 *
	 * @param
	 *
	 * @return java.util.List<com.ai.modules.ybChargeSearch.handle.rule.model.RuleWhere>
	 *
	 */
	protected abstract List<RuleWhere> parseRuleWhere();

	/**
	 *
	 * 功能描述：规则查询条件
	 * @author zhangly
	 * @date 2023-02-16 14:57:28
	 *
	 * @param ruleWhere
	 *
	 * @return java.lang.String
	 *
	 */
	protected String parseRuleWhere(RuleWhere ruleWhere) {
		StringBuilder sb = new StringBuilder();
		String column = ruleWhere.getColumn();
		String value = ruleWhere.getValue();
		String valueType = ruleWhere.getValueType();
		sb.append(column);
		QueryRuleEnum compare = ruleWhere.getCompare();
		String operator = compare.getValue();
		if(QueryRuleEnum.EQ.equals(compare)
				|| QueryRuleEnum.GT.equals(compare)
				|| QueryRuleEnum.GE.equals(compare)
				|| QueryRuleEnum.LT.equals(compare)
				|| QueryRuleEnum.LE.equals(compare)
				|| QueryRuleEnum.NE.equals(compare)) {
			//=,>,>=,<,<=
			sb.append(operator);
			if(DcConstants.TYPE_STRING.equals(valueType)) {
				sb.append("'");
			}
			sb.append(value);
			if(DcConstants.TYPE_STRING.equals(valueType)) {
				sb.append("'");
			}
		} else if(QueryRuleEnum.IN.equals(compare)) {
			value = StringUtils.replace(value, " ", "");
			if(DcConstants.TYPE_STRING.equals(valueType)) {
				value = StringUtils.replace(value, ",", "','");
			}
			sb.append(" ").append(operator).append("(").append(value).append(")");
		} else if(QueryRuleEnum.LIKE.equals(compare)) {
			//like
			sb.append(" ").append(operator).append(" '%").append(value).append("%'");
		} else if(QueryRuleEnum.RIGHT_LIKE.equals(compare)) {
			//rlike
			sb.append(" ").append(operator).append(" '").append(value).append("%'");
		} else if(QueryRuleEnum.LEFT_LIKE.equals(compare)) {
			//llike
			sb.append(" ").append(operator).append(" '%").append(value).append("'");
		} else if(QueryRuleEnum.SQL_RULES.equals(compare)) {
			//自定义比较符
			if(DcConstants.RULE_OPERATOR_REGEXLIKE.equals(operator)) {
				value = StringUtils.replace(value, ",", "|");
				sb.append(" ~ '").append(value).append("'");
			} else {
				sb.append(" ").append(compare.getMsg()).append("'").append(value).append("'");
			}
		} else {
			sb.append(operator);
			if(DcConstants.TYPE_STRING.equals(valueType)) {
				sb.append("'");
			}
			sb.append(value);
			if(DcConstants.TYPE_STRING.equals(valueType)) {
				sb.append("'");
			}
		}
		return sb.toString();
	}
}
