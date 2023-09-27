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

import com.ai.modules.engine.model.rule.AbsEngineParamRule;
import com.ai.modules.engine.model.rule.BaseEngineParamRule;
import com.ai.modules.engine.model.rule.EngineParamChargeGrpRule;
import com.ai.modules.engine.model.rule.EngineParamGrpRule;
import com.ai.modules.engine.model.rule.EngineParamIndicationRule;
import com.ai.modules.engine.model.rule.EngineParamOrgRule;
import com.ai.modules.engine.model.rule.EngineParamSelfJoinRule;
import com.ai.modules.engine.model.rule.EngineRuleDrugGrp;
import com.ai.modules.engine.model.rule.EngineRuleDrugusage;
import com.ai.modules.engine.model.rule.EngineRuleFrequency;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;

public class RuleParser extends AbsRuleParser {

	public RuleParser(MedicalRuleConfig rule, MedicalRuleConditionSet condition) {
		super(rule, condition);
	}

	@Override
	public String parseCondition() {
		AbsEngineParamRule paramRule = null;
		String compare = condition.getCompare();
		String condiType = condition.getField();
		if(RULE_CONDI_PROJGRP.equals(condiType)
				|| RULE_CONDI_ACCESS_PROJGRP.equals(condiType)
				|| RULE_CONDI_FITGROUPS.equals(condiType)) {
			//项目组
			EngineParamGrpRule sub = new EngineParamGrpRule("STD_TREATGROUP", "TREATGROUP_CODE", condition.getExt1());
			if(RULE_CONDI_FITGROUPS.equals(condiType)) {
				//依赖项目组
				sub.setRelation("2");
			}
			paramRule = sub;
		} else if(RULE_CONDI_DRUGGRP.equals(condiType)
				|| RULE_CONDI_ACCESS_DRUGGRP.equals(condiType)) { 
			//药品组
			paramRule = new EngineParamGrpRule("STD_DRUGGROUP", "DRUGGROUP_CODE", condition.getExt1());
		} else if(RULE_CONDI_PROJ.equals(condiType)) {
			//项目
			paramRule = new EngineParamGrpRule("STD_TREATGROUP", "TREATGROUP_CODE", condition.getExt1());
		} else if(RULE_CONDI_HISGROUPS.equals(condiType)) {
			//历史项目组
			paramRule = new EngineParamGrpRule("STD_TREATGROUP", "TREATGROUP_CODE", condition.getExt1());
            paramRule.setPatient(true);
            paramRule.setReverse(true);
		} else if(RULE_CONDI_DISEASEGRP.equals(condiType)
				|| RULE_CONDI_ACCESS_DISEASEGRP.equals(condiType)) {
			//疾病组
			paramRule = new EngineParamIndicationRule("DIAGGROUP_CODE", condition.getExt1());
		} else if(RULE_CONDI_DISEASE.equals(condiType)) {
			//疾病
			paramRule = new EngineParamIndicationRule("DIAGGROUP_CODE", condition.getExt1());
		} else if(RULE_CONDI_FREQUENCY.equals(condiType)) {
			//频次
			paramRule = new EngineRuleFrequency(rule, condition);
		} else if(RULE_CONDI_ORGTYPE.equals(condiType)
				|| RULE_CONDI_ACCESS_ORGTYPE.equals(condiType)) {
			//医院类别
			paramRule = new EngineParamOrgRule("ORGTYPE_CODE", condition.getExt1());
		} else if(RULE_CONDI_UNFITGROUPS.equals(condiType) 
				|| RULE_CONDI_UNFITGROUPSDAY.equals(condiType)) {
			//互斥项目组，一日互斥项目组，特殊处理：先筛选出存在互斥项目组的数据，后续再过滤掉非同日内互斥的数据
			EngineParamChargeGrpRule sub = new EngineParamChargeGrpRule(rule.getItemCodes(), "STD_TREATGROUP", "TREATGROUP_CODE", condition.getExt1());
			sub.setReverse(true);
			sub.addCondition("FUND_COVER:{0 TO *}");
			sub.addCondition("SELFPAY_PROP_MIN:[0 TO 1}");
			paramRule = sub;
		} else if(RULE_CONDI_UNEXPENSE.equals(condiType)) {
			//不能报销
			paramRule = new BaseEngineParamRule("FUND_COVER", "=", "0");
		} else if(RULE_CONDI_UNCHARGE.equals(condiType)) {
			//不能收费
			paramRule = new BaseEngineParamRule("ITEM_AMT", "=", "0");
		} else if(RULE_CONDI_SECDRUG.equals(condiType)) {
			//二线用药
			paramRule = new EngineParamSelfJoinRule("ITEMCODE", condition.getExt1());
            paramRule.setPatient(true);
		} else if(RULE_CONDI_UNPAYDRUG.equals(condiType)) {
			//合用不予支付
			if("DRUGGROUP".equals(condition.getExt2())) {
				EngineRuleDrugGrp sub = new EngineRuleDrugGrp(condition.getExt1());
				sub.setItemcode(rule.getItemCodes());
				sub.setReverse(true);
				paramRule = sub;
			} else {
				EngineParamSelfJoinRule sub = new EngineParamSelfJoinRule("ITEMCODE", condition.getExt1());
				sub.setReverse(true);
				//排除主体药品
				sub.addCondition("-ITEMCODE:"+rule.getItemCodes());
				sub.addCondition("FUND_COVER:{0 TO *}");
				sub.addCondition("SELFPAY_PROP_MIN:[0 TO 1}");
				paramRule = sub;
			}
		} else if(RULE_CONDI_DRUGUSAGE.equals(condiType)) {
			//给药途径
			paramRule = new EngineRuleDrugusage(rule, condition);
		}
		if(!RULE_CONDI_FREQUENCY.equals(condiType)) {
			if(paramRule!=null && "≠".equals(compare)) {
				paramRule.setReverse(true);
			}
		}
		
		return paramRule!=null ? paramRule.where() : null;
	}
}
