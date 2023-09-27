/**
 * HiveRuleParser.java	  V1.0   2022年11月10日 上午10:24:52
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.parse.hive;

import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.model.rule.AbsEngineParamRule;
import com.ai.modules.engine.model.rule.hive.HiveParamGrpRule;
import com.ai.modules.engine.model.rule.hive.HiveParamIndicationRule;
import com.ai.modules.engine.model.rule.hive.HiveParamOrgLvlAndTypeRule;
import com.ai.modules.engine.model.rule.hive.HiveParamOrgRule;
import com.ai.modules.engine.model.rule.hive.HiveParamSelfJoinRule;
import com.ai.modules.engine.model.rule.hive.HiveRuleDrugusage;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;

public class HiveRuleParser extends AbsHiveRuleParser {

	public HiveRuleParser(MedicalRuleConfig rule, MedicalRuleConditionSet condition, String fromTable) {
		super(rule, condition, fromTable);
	}

	@Override
	public WithTableModel parseCondition() {
		AbsEngineParamRule paramRule = null;
		String compare = condition.getCompare();
		String condiType = condition.getField();
		if(AbsRuleParser.RULE_CONDI_PROJGRP.equals(condiType)
				|| AbsRuleParser.RULE_CONDI_ACCESS_PROJGRP.equals(condiType)
				|| AbsRuleParser.RULE_CONDI_FITGROUPS.equals(condiType)) {
			//项目组
			HiveParamGrpRule sub = new HiveParamGrpRule("STD_TREATGROUP", "TREATGROUP_CODE", condition.getExt1(), fromTable);
			if(AbsRuleParser.RULE_CONDI_FITGROUPS.equals(condiType)) {
				//依赖项目组
				sub.setRelation("2");
			}
			paramRule = sub;
		} else if(AbsRuleParser.RULE_CONDI_DRUGGRP.equals(condiType)
				|| AbsRuleParser.RULE_CONDI_ACCESS_DRUGGRP.equals(condiType)) { 
			//药品组
			paramRule = new HiveParamGrpRule("STD_DRUGGROUP", "DRUGGROUP_CODE", condition.getExt1(), fromTable);
		} else if(AbsRuleParser.RULE_CONDI_PROJ.equals(condiType)) {
			//项目
			paramRule = new HiveParamGrpRule("STD_TREATGROUP", "TREATGROUP_CODE", condition.getExt1(), fromTable);
		} else if(AbsRuleParser.RULE_CONDI_HISGROUPS.equals(condiType)) {
			//历史项目组
			paramRule = new HiveParamGrpRule("STD_TREATGROUP", "TREATGROUP_CODE", condition.getExt1(), fromTable);
            paramRule.setPatient(true);
            paramRule.setReverse(true);
		} else if(AbsRuleParser.RULE_CONDI_DISEASEGRP.equals(condiType)
				|| AbsRuleParser.RULE_CONDI_ACCESS_DISEASEGRP.equals(condiType)) {
			//疾病组
			paramRule = new HiveParamIndicationRule("DIAGGROUP_CODE", condition.getExt1(), fromTable);
		} else if(AbsRuleParser.RULE_CONDI_DISEASE.equals(condiType)) {
			//疾病
			paramRule = new HiveParamIndicationRule("DIAGGROUP_CODE", condition.getExt1(), fromTable);
		} else if(AbsRuleParser.RULE_CONDI_FREQUENCY.equals(condiType)) {
			//频次
			
		} else if(AbsRuleParser.RULE_CONDI_ORGTYPE.equals(condiType)
				|| AbsRuleParser.RULE_CONDI_ACCESS_ORGTYPE.equals(condiType)) {
			//医院类别
			paramRule = new HiveParamOrgRule("ORGTYPE_CODE", condition.getExt1(), fromTable);
		} else if(AbsRuleParser.RULE_CONDI_HOSPLEVELTYPE.equals(condiType)) {
			//医院级别+类型
			paramRule = new HiveParamOrgLvlAndTypeRule(rule, condition, fromTable);
		} else if(AbsRuleParser.RULE_CONDI_UNFITGROUPS.equals(condiType) 
				|| AbsRuleParser.RULE_CONDI_UNFITGROUPSDAY.equals(condiType)) {
			//互斥项目组，一日互斥项目组，特殊处理：先筛选出存在互斥项目组的数据，后续再过滤掉非同日内互斥的数据
			
		} else if(AbsRuleParser.RULE_CONDI_UNEXPENSE.equals(condiType)) {
			//不能报销
			
		} else if(AbsRuleParser.RULE_CONDI_UNCHARGE.equals(condiType)) {
			//不能收费
			
		} else if(AbsRuleParser.RULE_CONDI_SECDRUG.equals(condiType)) {
			//二线用药
			paramRule = new HiveParamSelfJoinRule("ITEMCODE", condition.getExt1(), fromTable);
            paramRule.setPatient(true);
		} else if(AbsRuleParser.RULE_CONDI_UNPAYDRUG.equals(condiType)) {
			//合用不予支付
			if("DRUGGROUP".equals(condition.getExt2())) {
				//药品组
				HiveParamGrpRule sub = new HiveParamGrpRule("STD_DRUGGROUP", "DRUGGROUP_CODE", condition.getExt1(), fromTable);
				sub.addCondition("ATC_DRUGCODE<>'"+rule.getItemCodes()+"'");
				sub.setReverse(true);
				paramRule = sub;
			} else {
				//药品
				HiveParamSelfJoinRule sub = new HiveParamSelfJoinRule("ITEMCODE", condition.getExt1(), fromTable);
				sub.setReverse(true);
				//排除主体药品
				sub.addCondition("ITEMCODE<>'"+rule.getItemCodes()+"'");
				sub.addCondition("FUND_COVER>0");
				sub.addCondition("SELFPAY_PROP_MIN>=0");
				sub.addCondition("SELFPAY_PROP_MIN<1");
				paramRule = sub;
			}
		} else if(AbsRuleParser.RULE_CONDI_DRUGUSAGE.equals(condiType)) {
			//给药途径
			paramRule = new HiveRuleDrugusage(rule, condition, fromTable);
		}
		if(paramRule!=null) {
			if(!AbsRuleParser.RULE_CONDI_FREQUENCY.equals(condiType) && "≠".equals(compare)) {
				paramRule.setReverse(true);
			}
		}
		String sql = paramRule!=null ? paramRule.where() : null;
		return new WithTableModel(this.buildWithTable(), sql);
	}

}
