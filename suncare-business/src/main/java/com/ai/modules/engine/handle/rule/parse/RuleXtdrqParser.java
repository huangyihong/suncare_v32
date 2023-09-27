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

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.rule.EngineParamGrpRule;
import com.ai.modules.engine.model.rule.EngineParamIndicationRule;
import com.ai.modules.engine.model.rule.EngineRuleMasterInfo;
import com.ai.modules.engine.model.rule.EngineRuleTest;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;

/**
 * 
 * 功能描述：限特定人群规则解析
 *
 * @author  zhangly
 * Date: 2021年1月15日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class RuleXtdrqParser extends AbsRuleParser {
	
	public RuleXtdrqParser(MedicalRuleConfig rule, MedicalRuleConditionSet condition) {
		super(rule, condition);
	}

	@Override
	public String parseCondition() {
		List<String> wheres = new ArrayList<String>();
		if(StringUtils.isNotBlank(condition.getExt1())) {
			//年龄
			EngineRuleMasterInfo paramRule = new EngineRuleMasterInfo();
			paramRule.setAgeUnit(condition.getExt2());
			paramRule.setAgeRange(condition.getExt1());
			wheres.add(paramRule.where());
		}
		if(StringUtils.isNotBlank(condition.getExt4())) {
			//疾病组
			EngineParamIndicationRule paramRule = new EngineParamIndicationRule("DIAGGROUP_CODE", condition.getExt4());			
			String compare = condition.getExt3();
			if("≠".equals(compare)) {
				paramRule.setReverse(true);
			}
			wheres.add(paramRule.where());
		}
		if(StringUtils.isNotBlank(condition.getExt6())) {
			//药品组
			EngineParamGrpRule paramRule = new EngineParamGrpRule("STD_DRUGGROUP", "DRUGGROUP_CODE", condition.getExt6());
			String compare = condition.getExt5();
			if("≠".equals(compare)) {
				paramRule.setReverse(true);
			}
			wheres.add(paramRule.where());
		}
		if(StringUtils.isNotBlank(condition.getExt8())) {
			//项目组
			EngineParamGrpRule paramRule = new EngineParamGrpRule("STD_TREATGROUP", "TREATGROUP_CODE", condition.getExt8());
			String compare = condition.getExt7();
			if("≠".equals(compare)) {
				paramRule.setReverse(true);
			}
			wheres.add(paramRule.where());
		}
		if(StringUtils.isNotBlank(condition.getExt10())) {
			//医嘱
			StringBuilder sb = new StringBuilder();
			sb.append("_query_:\"");
			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_ORDER", "VISITID", "VISITID");
			sb.append(plugin.parse());
			sb.append("ITEMNAME:*").append(condition.getExt10()).append("*");
			sb.append("\"");
			String compare = condition.getExt9();
			String condition = sb.toString();
			if("notlike".equals(compare)) {
				condition = "*:* -("+sb.toString()+")";
			}
			wheres.add(condition);
		}		
		String condition = StringUtils.join(wheres, " AND ");
		int size = wheres.size();
		if(size>1) {
			condition = "(" + condition + ")";
		}
		return condition;
	}
}
