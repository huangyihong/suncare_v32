/**
 * DrugSecondLineHandle.java	  V1.0   2020年11月4日 下午4:09:05
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.hive;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.model.rule.hive.AbsHiveParamRule;
import com.ai.modules.engine.model.rule.hive.BaseHiveParamRule;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 
 * 功能描述：合理诊疗一日就诊限频次规则
 *
 * @author  zhangly
 * Date: 2020年11月4日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HiveTreatOverFreqRuleHandle extends HiveOverFreqRuleHandle {
		
	public HiveTreatOverFreqRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, String datasource,
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, datasource, rule, ruleConditionList);
	}

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
		return new WithTableModel(AbsHiveRuleHandle.WITH_TABLE_RESULT, sql);
	}
	
	private WithTableModel parseCondition(MedicalRuleConditionSet condition, String fromTable) {
		List<WithTableModel> withTableList = new ArrayList<WithTableModel>();
		String compare = condition.getCompare();
		String frequency = condition.getExt2();
		//一日就诊限频次
		AbsHiveParamRule paramRule = new BaseHiveParamRule("ITEM_QTY", compare, frequency, fromTable);
		//黑名单取反
		paramRule.setReverse(true);
		String sql = "select * from "+fromTable+" where "+paramRule.where();
		
		withTableList.add(new WithTableModel(this.getClass().getSimpleName()+"_frequency", sql));
		if(StringUtils.isNotBlank(condition.getExt4())) {
			//项目组前提条件
			boolean reverse = "≠".equals(condition.getExt3());
			sql = this.parseTreat(fromTable, condition.getExt4(), reverse);
			withTableList.add(new WithTableModel(this.getClass().getSimpleName()+"_treat", sql));
		}
		String alias = WithTableUtil.buildWithTable(condition);
		return new WithTableModel(alias, WithTableUtil.parseWithTableList(fromTable, withTableList, "id"));
	}
	
	/**
	 * 
	 * 功能描述：同一天项目条件
	 *
	 * @author  zhangly
	 *
	 * @param table
	 * @param compareValue
	 * @param reverse
	 * @return
	 */
	private String parseTreat(String table, String compareValue, boolean reverse) {
		StringBuilder sb = new StringBuilder();
		sb.append("select * from $table where");
		if(reverse) {
			sb.append(" not ");
		}
		String value = "('" + StringUtils.replace(compareValue, "|", "','") + "')";
		sb.append(" exists(select 1 from dws_patient_1visit_1day_itemsum x1");
		sb.append(" join medical_gbdp.STD_TREATGROUP x2 on x1.ITEMCODE=x2.TREATCODE");
		sb.append(" where");
		sb.append(" $table.visitid=x1.visitid");
		sb.append(" and $table.PRESCRIPTTIME_DAY=x1.PRESCRIPTTIME_DAY");
		sb.append(" and $table.itemcode<>x1.itemcode");
		sb.append(" and x1.ITEM_QTY>0");
		sb.append(" and x1.ITEM_AMT>0");
		sb.append(" and ");
		sb.append("x2.TREATGROUP_CODE in").append(value);
		sb.append(")");
		String sql = sb.toString();
		sql = StringUtils.replace(sql, "$table", table);
		return sql;
	}
}
