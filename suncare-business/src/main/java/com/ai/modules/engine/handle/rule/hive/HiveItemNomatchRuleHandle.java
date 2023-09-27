/**
 * SolrIndicationRuleHandle.java	  V1.0   2021年3月22日 下午3:12:07
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.hive;

import java.util.ArrayList;
import java.util.List;

import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.hive.model.JudgeWithTableScript;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.model.rule.hive.AbsHiveParamRule;
import com.ai.modules.engine.model.rule.hive.HiveParamGrpRule;
import com.ai.modules.engine.model.rule.hive.HiveParamIndicationRule;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 与既往项目不符规则
 * @author  zhangly
 * Date: 2022年11月22日
 */
public class HiveItemNomatchRuleHandle extends HiveRuleHandle {
	
	public HiveItemNomatchRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, String datasource,
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, datasource, rule, ruleConditionList);
	}
	
	@Override
	protected JudgeWithTableScript judgeWithTableScript(String fromTable) throws Exception {
		StringBuilder sql = new StringBuilder();
		//判断条件过滤后的表（过滤后结果已是黑名单）
		WithTableModel judgeTable = this.parseJudgeCondition(fromTable);
		if(judgeTable!=null) {
			sql.append(judgeTable.getAlias()).append(" as (").append(judgeTable.getSql()).append(")");
			return new JudgeWithTableScript(sql.toString(), judgeTable.getAlias());
		}
		return null;
	}
	
	/**
	 * 解析判断条件
	 */
	@Override
	protected WithTableModel parseJudgeCondition(String fromTable) {
		List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", null);
    	if(judgeList==null || judgeList.size()==0) {
    		throw new RuntimeException(rule.getItemNames()+"规则未配置判断条件！");
    	}
    	List<WithTableModel> withTableList = new ArrayList<WithTableModel>();
		for(MedicalRuleConditionSet bean : judgeList) {
			WithTableModel withTable = this.parseCondition(bean, fromTable);
			withTableList.add(withTable);
		}
		String sql = WithTableUtil.parseWithTableList(fromTable, withTableList);
		return new WithTableModel(AbsHiveRuleHandle.WITH_TABLE_JUDGE, sql);
	}

	private WithTableModel parseCondition(MedicalRuleConditionSet bean, String fromTable) {
		AbsHiveParamRule paramRule = null;
		String condiType = bean.getField();
		if(AbsRuleParser.RULE_CONDI_HISGROUPS.equals(condiType)) {
			//历史项目组
			paramRule = new HiveParamGrpRule("STD_TREATGROUP", "TREATGROUP_CODE", bean.getExt1(), fromTable);
	        paramRule.setPatient(true);
	        paramRule.setBeforeVisit(true);
		} else if(AbsRuleParser.RULE_CONDI_DISEASEGRP.equals(condiType)) {
			//疾病组
			paramRule = new HiveParamIndicationRule("DIAGGROUP_CODE", bean.getExt1(), fromTable);
		}
		String sql = paramRule.where();
		return new WithTableModel(WithTableUtil.buildWithTable(bean), sql);
	}
}
