/**
 * RuleIgnoreNullParser.java	  V1.0   2020年12月22日 下午3:06:43
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.parse.hive;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.model.rule.hive.HiveRuleMasterInfo;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;

public class HiveRuleIgnoreNullParser {
	//规则对象
	private MedicalRuleConfig rule;
	//规则条件
	private List<MedicalRuleConditionSet> ruleConditionList;

	public HiveRuleIgnoreNullParser(MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
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
				HiveRuleMasterInfo ruleMaster = null;
				for(MedicalRuleConditionSet bean : ruleConditionList) {
					String condiType = bean.getField();										
					if(AbsRuleParser.RULE_MASTER_SET.contains(condiType)) {
						if(ruleMaster==null) {
							ruleMaster = new HiveRuleMasterInfo();
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
					wheres.add(ruleMaster.ignoreNullWhere("x"));
				}
				if(condiSet.contains(AbsRuleParser.RULE_CONDI_FREQUENCY)) {
					//频次
					wheres.add("y.ITEM_QTY>0");
				}
				if("freq3".equals(rule.getRuleLimit())) {
					//日均次频次
					wheres.add("y.ITEM_DAYAVG_QTY>0");
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
					wheres.add("y.FUND_COVER>0");
				}
				if(condiSet.contains(AbsRuleParser.RULE_CONDI_UNCHARGE)) {
					//不能收费
					wheres.add("y.ITEM_AMT>0");
				}
				if(condiSet.contains(AbsRuleParser.RULE_CONDI_DISEASEGRP)
						|| condiSet.contains(AbsRuleParser.RULE_CONDI_DISEASE)) {
					//疾病组或疾病
		  			wheres.add("x.DISEASECODE is not null");
				}
			}
		}
		return wheres;
	}
}
