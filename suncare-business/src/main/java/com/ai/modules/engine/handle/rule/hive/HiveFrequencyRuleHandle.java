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

import java.util.List;
import java.util.Map;

import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.hive.model.JoinTableModel;
import com.ai.modules.engine.handle.rule.hive.model.JudgeWithTableScript;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.model.rule.hive.BaseHiveParamRule;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 一次就诊限频次、日均限频次规则
 * @author  zhangly
 * Date: 2022年11月15日
 */
public class HiveFrequencyRuleHandle extends HiveRuleHandle {
	
	public HiveFrequencyRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, String datasource,
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, datasource, rule, ruleConditionList);
	}

	@Override
	protected JoinTableModel joinTables() throws Exception {
		JoinTableModel joinTable = super.joinTables();
		List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", null);
    	MedicalRuleConditionSet condition = judgeList.get(0);
		String period = condition.getExt1();
		if("avgday".equals(period)) {
			//日均次，追加就诊类型=住院过滤条件
			joinTable.addMasterWhere("VISITTYPE_ID like 'ZY01%'");
		}
		return joinTable;
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
    	MedicalRuleConditionSet bean = judgeList.get(0);
		return parseCondition(bean, fromTable);
	}
	
	private WithTableModel parseCondition(MedicalRuleConditionSet condition, String fromTable) {
		String period = condition.getExt1();
		String compare = condition.getCompare();
		String frequency = condition.getExt2();
		if("1time".equals(period)) {
			//一次就诊
			BaseHiveParamRule paramRule = new BaseHiveParamRule("ITEM_QTY", compare, frequency, fromTable);
			//黑名单取反
			paramRule.setReverse(true);
			String sql = "select * from "+fromTable+" where "+paramRule.where();
			return new WithTableModel(AbsHiveRuleHandle.WITH_TABLE_RESULT, sql);
		} else if("avgday".equals(period)) {
			//日均次
			BaseHiveParamRule paramRule = new BaseHiveParamRule("ITEM_DAYAVG_QTY", compare, frequency, fromTable);
			//黑名单取反
			paramRule.setReverse(true);
			String sql = "select * from "+fromTable+" where "+paramRule.where();
			return new WithTableModel(AbsHiveRuleHandle.WITH_TABLE_RESULT, sql);
		}
		return null;	
	}
	
	@Override
	protected Map<String, String> parseUdfFieldMap() throws Exception {
		Map<String, String> fieldMap = super.parseUdfFieldMap();
		//重新设置违规金额、违规基金支出金额等字段取值
		List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", null);
    	MedicalRuleConditionSet condition = judgeList.get(0);
		String period = condition.getExt1();
		String frequency = condition.getExt2();
		if("1time".equals(period)) {
			//一次就诊
			fieldMap.put("AI_ITEM_CNT", "y.ITEM_QTY");
			String text = "y.ITEM_QTY-"+frequency;
			fieldMap.put("AI_OUT_CNT", text);
			text = "(y.ITEM_QTY-"+frequency+")*y.ITEMPRICE_MAX";
			fieldMap.put("MIN_MONEY", text);
			fieldMap.put("MAX_MONEY", text);
			text = "(y.FUND_COVER/y.ITEM_QTY)*(y.ITEM_QTY-"+frequency+")";
			fieldMap.put("ACTION_MONEY", text);
			fieldMap.put("MAX_ACTION_MONEY", text);
		} else if("avgday".equals(period)) {
			//日均次
			fieldMap.put("AI_ITEM_CNT", "y.ITEM_QTY");
			String text = "y.ITEM_QTY-"+frequency+"*y.ZY_DAYS_CALCULATE";//超出使用数量=项目总数量-限制频次*住院天数
			fieldMap.put("AI_OUT_CNT", text);
			text = "(y.ITEM_QTY-"+frequency+"*y.ZY_DAYS_CALCULATE)*ITEMPRICE_MAX";
			fieldMap.put("MIN_MONEY", text);
			fieldMap.put("MAX_MONEY", text);
			text = "(y.FUND_COVER/y.ITEM_QTY)*(y.ITEM_QTY-"+frequency+"*y.ZY_DAYS_CALCULATE)";
			fieldMap.put("ACTION_MONEY", text);
			fieldMap.put("MAX_ACTION_MONEY", text);
		}
		return fieldMap;
	}
}
