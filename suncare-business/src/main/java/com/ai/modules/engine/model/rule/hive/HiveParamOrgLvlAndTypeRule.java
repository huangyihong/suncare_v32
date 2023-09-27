/**
 * EngineParamRule.java	  V1.0   2019年12月31日 下午5:23:44
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.rule.hive;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.handle.rule.hive.WithTableUtil;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;

/**
 * 
 * 功能描述：医疗机构类别规则
 *
 * @author  zhangly
 * Date: 2019年12月31日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HiveParamOrgLvlAndTypeRule extends AbsHiveParamRule {	
	
	private MedicalRuleConfig rule;
	private MedicalRuleConditionSet condition;
	public HiveParamOrgLvlAndTypeRule(MedicalRuleConfig rule, MedicalRuleConditionSet condition, String fromTable) {
		this.rule = rule;
		this.condition = condition;
		this.fromTable = fromTable;
	}

	@Override
	public String where() {
		List<WithTableModel> withTableList = new ArrayList<WithTableModel>();
		String prefix = "table_".concat(AbsRuleParser.RULE_CONDI_HOSPLEVELTYPE);
		String condiType = condition.getField();
		if(AbsRuleParser.RULE_CONDI_HOSPLEVELTYPE.equals(condiType)) {
			if(StringUtils.isNotBlank(condition.getExt1())) {
				HiveRuleMasterInfo paramRule = new HiveRuleMasterInfo(fromTable);
				paramRule.setYyjb(condition.getExt1());
				String alias = prefix.concat("_").concat("hosplvl");
				withTableList.add(new WithTableModel(alias, paramRule.where()));
			}
			if(StringUtils.isNotBlank(condition.getExt3())) {
				String logic = "AND";
				if(StringUtils.isNotBlank(condition.getExt1())) {
					logic = condition.getExt2();
				}
				HiveParamOrgRule paramRule = new HiveParamOrgRule("ORGTYPE_CODE", condition.getExt3(), fromTable);
				String alias = prefix.concat("_").concat("type");
				withTableList.add(new WithTableModel(alias, paramRule.where(), logic));
			}
		}
		return WithTableUtil.parseWithTableList(fromTable, withTableList);
	}		
}
