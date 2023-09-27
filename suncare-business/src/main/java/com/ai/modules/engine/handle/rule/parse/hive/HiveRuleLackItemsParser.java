/**
 * RuleParser.java	  V1.0   2020年12月18日 下午5:59:21
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.parse.hive;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.hive.WithTableUtil;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.model.rule.hive.HiveParamGrpRule;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;

/**
 * 
 * 功能描述：药品使用缺少必要药品或项目规则解析
 *
 * @author  zhangly
 * Date: 2021年1月15日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HiveRuleLackItemsParser extends AbsHiveRuleParser {
	//是否检查病人以往历史病例
	private boolean patient = false;

	public HiveRuleLackItemsParser(MedicalRuleConfig rule, MedicalRuleConditionSet condition, String fromTable) {
		super(rule, condition, fromTable);
	}

	@Override
	public WithTableModel parseCondition() {
		List<WithTableModel> withTableList = new ArrayList<WithTableModel>();
		if(StringUtils.isNotBlank(condition.getExt1())) {
			//项目组
			HiveParamGrpRule paramRule = new HiveParamGrpRule("STD_TREATGROUP", "TREATGROUP_CODE", condition.getExt1(), fromTable);
			paramRule.setPatient(patient);
			String alias = "table_"+this.getClass().getSimpleName()+"_treat";
			WithTableModel withTable = new WithTableModel(alias, paramRule.where());
			withTableList.add(withTable);
		}
		if(StringUtils.isNotBlank(condition.getExt3())) {
			String logic = "AND";
			if(StringUtils.isNotBlank(condition.getExt1())) {
				logic = "OR";
			}
			//药品组
			HiveParamGrpRule paramRule = new HiveParamGrpRule("STD_DRUGGROUP", "DRUGGROUP_CODE", condition.getExt3(), fromTable);
			paramRule.setPatient(patient);
			String alias = "table_"+this.getClass().getSimpleName()+"_drug";
			WithTableModel withTable = new WithTableModel(alias, paramRule.where(), logic);
			withTableList.add(withTable);
		}
		String sql = WithTableUtil.parseWithTableList(fromTable, withTableList);
		String alias = AbsHiveRuleHandle.WITH_TABLE_JUDGE+"_"+condition.getGroupNo();
		return new WithTableModel(alias, sql);
	}

	public boolean isPatient() {
		return patient;
	}

	public void setPatient(boolean patient) {
		this.patient = patient;
	}
}
