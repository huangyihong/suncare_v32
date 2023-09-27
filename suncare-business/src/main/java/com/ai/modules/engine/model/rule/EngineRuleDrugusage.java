/**
 * RuleParser.java	  V1.0   2020年12月18日 下午5:59:21
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.rule;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.rule.AbsEngineParamRule;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
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
public class EngineRuleDrugusage extends AbsEngineParamRule {
	private MedicalRuleConfig rule;
	private MedicalRuleConditionSet condition;
	
	public EngineRuleDrugusage(MedicalRuleConfig rule, MedicalRuleConditionSet condition) {
		this.rule = rule;
		this.condition = condition;
	}
	
	@Override
	public String where() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		String value = "(" + StringUtils.replace(condition.getExt1(), "|", " OR ") + ")";
		sb.append("_query_\"");
		EngineMapping mapping = new EngineMapping(EngineUtil.DWB_ORDER, "VISITID", "VISITID");
		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
		sb.append(plugin.parse());
		sb.append("USETYPE:").append(value);
		sb.append(" AND ITEMCODE:").append(rule.getItemCodes());
		sb.append("\"");
		if(StringUtils.isNotBlank(condition.getExt3())) {
			sb.append(" ").append(condition.getExt2()).append(" ");
			plugin = new SolrJoinParserPlugin(EngineUtil.DWB_ORDER, "VISITID", "VISITID");
			value = "(*" + StringUtils.replace(condition.getExt3(), "|", "* OR *") + "*)";
			sb.append("ITEMNAME:").append(value);
		}
		sb.append(")");
		
		if(reverse) {
			return "*:* -("+sb.toString()+")";
		} else {
			return sb.toString();
		}
	}
}
