/**
 * RuleRegexParser.java	  V1.0   2023年2月16日 上午9:32:22
 *
 * Copyright (c) 2023 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.ybChargeSearch.handle.rule.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ai.modules.ybChargeSearch.constants.DcConstants;
import org.apache.commons.lang3.StringUtils;

import com.ai.modules.ybChargeSearch.constants.RuleRegexConstants;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleLimitModel;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleRegexResult;

public class RuleRegexParser extends AbsRuleRegexParser {

	public RuleRegexParser(RuleLimitModel ruleLimitModel) {
		super(ruleLimitModel);
	}

	@Override
	public RuleRegexResult validate() {
		RuleRegexConstants ruleRegex = RuleRegexConstants.getByCode(ruleLimitModel.getLimitType());
		if(ruleRegex==null) {
			return RuleRegexResult.ok();
		}
		String regex = ruleRegex.getRegex();
		String text = ruleLimitModel.getLimitText();
		text = replaceCharacter(text);
		String[] array = StringUtils.split(regex, "&&");
		for(String reg : array) {
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher = pattern.matcher(text);
			if(matcher.matches()) {
				return RuleRegexResult.ok();
			}
		}
		return RuleRegexResult.error(ruleRegex);
	}

	private String replaceCharacter(String text) {
		text = StringUtils.replace(text, " ", "");
		text = StringUtils.replace(text, "，", ",");
		String limitType = ruleLimitModel.getLimitType();
		if(DcConstants.RULE_LIMIT_AGE.equals(limitType)
			|| DcConstants.RULE_LIMIT_DAYAGE.equals(limitType)) {
			text = StringUtils.replace(text, "【", "[");
			text = StringUtils.replace(text, "【", "]");
			text = StringUtils.replace(text, "（", "(");
			text = StringUtils.replace(text, "）", ")");
		}
		return text;
	}
}
