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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.rule.EngineParamGrpRule;
import com.ai.modules.engine.model.rule.EngineParamIndicationRule;
import com.ai.modules.engine.model.rule.EngineRuleTest;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;

/**
 * 
 * 功能描述：适应症规则解析
 *
 * @author  zhangly
 * Date: 2021年1月15日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class RuleIndicationParser extends AbsRuleParser {
	//是否检查病人以往历史病例
	private boolean patient = false;

	public RuleIndicationParser(MedicalRuleConfig rule, MedicalRuleConditionSet condition) {
		super(rule, condition);
	}

	@Override
	public String parseCondition() {
		List<String> wheres = new ArrayList<String>();
		if(StringUtils.isNotBlank(condition.getExt2())) {
			//疾病组
			EngineParamIndicationRule paramRule = new EngineParamIndicationRule("DIAGGROUP_CODE", condition.getExt2());
			paramRule.setPatient(patient);
			wheres.add(paramRule.where());
		}
		if(StringUtils.isNotBlank(condition.getExt3())) {
			//项目组
			EngineParamGrpRule paramRule = new EngineParamGrpRule("STD_TREATGROUP", "TREATGROUP_CODE", condition.getExt3());
			wheres.add(paramRule.where());
		}
		if(StringUtils.isNotBlank(condition.getExt4())) {
			//化验结果
			EngineRuleTest paramRule = new EngineRuleTest("ITEMCODE", condition.getExt4());
			wheres.add(paramRule.where());
		}
		if(StringUtils.isNotBlank(condition.getExt5())) {
			//药品组
			EngineParamGrpRule paramRule = new EngineParamGrpRule("STD_DRUGGROUP", "DRUGGROUP_CODE", condition.getExt5());
			wheres.add(paramRule.where());
		}
		String condition = StringUtils.join(wheres, " AND ");
		int size = wheres.size();
		if(size>1) {
			condition = "(" + condition + ")";
		}
		return condition;
	}

	public boolean isPatient() {
		return patient;
	}

	public void setPatient(boolean patient) {
		this.patient = patient;
	}
}
