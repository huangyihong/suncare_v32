/**
 * RuleIgnoreNullParser.java	  V1.0   2020年12月22日 下午3:06:43
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.parse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.rule.EngineRuleMasterInfo;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;

public class RuleIgnoreNullParser {
	//规则对象
	private MedicalRuleConfig rule;
	//规则条件
	private List<MedicalRuleConditionSet> ruleConditionList;

	public RuleIgnoreNullParser(MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		this.rule = rule;
		this.ruleConditionList = ruleConditionList;
	}
	
	public List<String> ignoreNullWhere() {
		List<String> wheres = new ArrayList<String>();
		if(ruleConditionList!=null) {
			//限定条件
			List<MedicalRuleConditionSet> judgeList = ruleConditionList.stream().filter(s->"judge".equals(s.getType())).collect(Collectors.toList());
			if(judgeList!=null) {
				//条件包含的限定范围
				Set<String> condiSet = new HashSet<String>();
				//属于master表的筛查
				EngineRuleMasterInfo ruleMaster = null;
				for(MedicalRuleConditionSet bean : ruleConditionList) {
					String condiType = bean.getField();										
					if(AbsRuleParser.RULE_MASTER_SET.contains(condiType)) {
						if(ruleMaster==null) {
							ruleMaster = new EngineRuleMasterInfo();
						}
						ruleMaster.with(bean);
					} else if(AbsRuleParser.RULE_CONDI_INDICATION.equals(condiType)) {
						//适应症
						if(StringUtils.isNotBlank(bean.getExt2())) {
							condiSet.add(AbsRuleParser.RULE_CONDI_DISEASEGRP);
						}
					} else {
						condiSet.add(condiType);
					}
				}
				if(ruleMaster!=null) {
					//master字段忽略空值
					wheres.add(ruleMaster.ignoreNullWhere());
				}
				if(condiSet.contains(AbsRuleParser.RULE_CONDI_FREQUENCY)) {
					//频次
					wheres.add("ITEM_QTY:{0 TO *}");
				}
				if("freq3".equals(rule.getRuleLimit())) {
					//日均次频次
					wheres.add("ITEM_DAYAVG_QTY:{0 TO *}");
				}
				if("freq4".equals(rule.getRuleLimit()) 
						|| "freq5".equals(rule.getRuleLimit()) 
						|| "freq6".equals(rule.getRuleLimit())
						|| "freq7".equals(rule.getRuleLimit())) {
					//周、月、季、年频次
					String fromIndex = "MAPPER_DWS_PATIENT_CHARGEITEM_SUM_"+EngineUtil.getDwsPeriod(rule.getRuleLimit());
					SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(fromIndex, "VISITID", "VISITID");
					wheres.add(plugin.parse()+"*:*");
				}
				if(condiSet.contains(AbsRuleParser.RULE_CONDI_UNEXPENSE)) {
					//不能报销
					wheres.add("FUND_COVER:{0 TO *}");
				}
				if(condiSet.contains(AbsRuleParser.RULE_CONDI_UNCHARGE)) {
					//不能收费
					wheres.add("ITEM_AMT:{0 TO *}");
				}
				if(condiSet.contains(AbsRuleParser.RULE_CONDI_DISEASEGRP)
						|| condiSet.contains(AbsRuleParser.RULE_CONDI_DISEASE)) {
					//疾病组或疾病
					StringBuilder sb = new StringBuilder();
		  			sb.append("_query_:\"");
		  			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_MASTER_INFO", "VISITID", "VISITID");
		  			sb.append(plugin.parse());
		  			sb.append("DISEASECODE:?*");
		  			sb.append("\"");
		  			wheres.add(sb.toString());
				}
			}
		}
		return wheres;
	}
}
