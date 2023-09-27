/**
 * RuleParser.java	  V1.0   2020年12月18日 下午5:59:21
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.rule.hive;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;

/**
 * 
 * 功能描述：给药途径规则解析
 *
 * @author  zhangly
 * Date: 2021年5月27日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HiveRuleDrugusage extends AbsHiveParamRule {
	private MedicalRuleConfig rule;
	private MedicalRuleConditionSet condition;
	
	public HiveRuleDrugusage(MedicalRuleConfig rule, MedicalRuleConditionSet condition, String fromTable) {
		this.rule = rule;
		this.condition = condition;
		this.fromTable = fromTable;
	}
	
	@Override
	public String where() {
		String value = "('" + StringUtils.replace(condition.getExt1(), "|", "','") + "')";
		StringBuilder sb = new StringBuilder();
		sb.append("select * from ").append(fromTable);
		sb.append(" where visitid in(select visitid from ").append(EngineUtil.DWB_ORDER).append(" x1");
		sb.append(" where (USETYPE in").append(value);
		sb.append(" and itemcode='").append(rule.getItemCodes()).append("'");
		sb.append(")");
		if(StringUtils.isNotBlank(condition.getExt3())) {
			sb.append(" ").append(condition.getExt2());
			sb.append(" itemname rlike '").append(value).append("'");
		}
		sb.append(")");
		
		return sb.toString();
	}
}
