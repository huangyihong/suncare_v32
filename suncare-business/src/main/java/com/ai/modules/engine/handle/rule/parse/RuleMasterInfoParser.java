/**
 * RuleParser.java	  V1.0   2020年12月18日 下午5:59:21
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.parse;

import com.ai.modules.engine.model.rule.EngineRuleMasterInfo;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;

public class RuleMasterInfoParser extends AbsRuleParser {

	public RuleMasterInfoParser(MedicalRuleConfig rule, MedicalRuleConditionSet condition) {
		super(rule, condition);
	}

	@Override
	public String parseCondition() {
		EngineRuleMasterInfo paramRule = new EngineRuleMasterInfo();
		String compare = condition.getCompare();
		String condiType = condition.getField();
		if(AbsRuleParser.RULE_CONDI_SEX.equals(condiType)
				|| AbsRuleParser.RULE_CONDI_ACCESS_SEX.equals(condiType)) {
			//性别
			paramRule.setSex(condition.getExt1());			
		} else if(AbsRuleParser.RULE_CONDI_AGE.equals(condiType)
				|| AbsRuleParser.RULE_CONDI_ACCESS_AGE.equals(condiType)) {
			//年龄
			paramRule.setAgeUnit(condition.getExt2());
			paramRule.setAgeRange(condition.getExt1());
		} else if(AbsRuleParser.RULE_CONDI_JZLX.equals(condiType)
				|| AbsRuleParser.RULE_CONDI_ACCESS_JZLX.equals(condiType)) {
			//就诊类型
			paramRule.setJzlx(condition.getExt1());
		} else if(AbsRuleParser.RULE_CONDI_YBLX.equals(condiType)
				||AbsRuleParser.RULE_CONDI_ACCESS_YBLX.equals(condiType)) {
			//医保类型
			paramRule.setYblx(condition.getExt1());
		} else if(AbsRuleParser.RULE_CONDI_YYJB.equals(condiType)
				||AbsRuleParser.RULE_CONDI_ACCESS_YYJB.equals(condiType)) {
			//医院级别
			paramRule.setYyjb(condition.getExt1());
		} else if(AbsRuleParser.RULE_CONDI_OFFICE.equals(condiType)
				|| AbsRuleParser.RULE_CONDI_ACCESS_OFFICE.equals(condiType)) {
			//科室
			paramRule.setOffice(condition.getExt1());
		} else if(AbsRuleParser.RULE_CONDI_ORG.equals(condiType)) {
			//医院
			paramRule.setOrg(condition.getExt1());
		}
		if(paramRule!=null && "≠".equals(compare)) {
			paramRule.setReverse(true);
		}		
		return paramRule!=null ? paramRule.where() : null;
	}
}
