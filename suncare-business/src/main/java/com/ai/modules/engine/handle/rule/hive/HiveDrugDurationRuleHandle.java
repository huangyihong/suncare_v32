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
import com.ai.modules.engine.handle.rule.hive.model.JudgeWithTableScript;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.HiveJDBCUtil;

/**
 * 用药时限规则
 * @author  zhangly
 * Date: 2022年11月15日
 */
public class HiveDrugDurationRuleHandle extends HiveRuleHandle {
	
	public HiveDrugDurationRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, String datasource,
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, datasource, rule, ruleConditionList);
	}
	
	
	
	@Override
	protected JudgeWithTableScript judgeWithTableScript(String fromTable) {
		StringBuilder sql = new StringBuilder();
		//筛选后的结果表已经是黑名单
		WithTableModel judgeTable = this.parseJudgeCondition(fromTable);
		if(judgeTable!=null) {
			sql.append(judgeTable.getAlias()).append(" as (").append(judgeTable.getSql()).append(")");
			return new JudgeWithTableScript(sql.toString(), judgeTable.getAlias());
		}
		return null;
	}

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
		StringBuilder sb = new StringBuilder();
		//先统计用药时限之后使用的次数、金额等，prescripttime>(visitdate+限定用药天数+1)
		int duration = this.getDuration(condition) + 1;
		sb.append("with tmp_dwb_charge_detail as(");
		sb.append("select visitid,itemcode,");
		if(HiveJDBCUtil.enabledProcessGp()) {
			sb.append("string_agg(distinct itemname_src,',') itemname_src,string_agg(distinct to_char(PRESCRIPTTIME,'yyyy-mm-dd'),',') PRESCRIPTTIME");
		} else {
			sb.append("group_concat(distinct itemname_src) itemname_src,group_concat(distinct PRESCRIPTTIME) PRESCRIPTTIME");
		}
		sb.append(",sum(AMOUNT) AMOUNT, sum(FEE) FEE,sum(FUND_COVER) FUND_COVER from dwb_charge_detail");
		sb.append(" where visitid=(select visitid from ").append(fromTable).append(")");
		if(HiveJDBCUtil.enabledProcessGp()) {
			sb.append(" and prescripttime>(visitdate+interval '").append(duration).append(" day')");
		} else {
			sb.append(" and prescripttime>days_add(to_timestamp(visitdate,'yyyy-MM-dd'),").append(duration).append(")");
		}
		sb.append(" and itemcode='").append(rule.getItemCodes()).append("'");
		sb.append(" group by visitid,itemcode");
		sb.append(")");
		sb.append("\n");
		sb.append("select x1.*");
		sb.append(",x2.AMOUNT AI_OUT_CNT");
		sb.append(",x2.FEE MIN_MONEY,x2.FEE MAX_MONEY");
		sb.append(",x2.FUND_COVER MIN_ACTION_MONEY,x2.FUND_COVER MAX_ACTION_MONEY");
		sb.append(",concat('超出用药时限发生日期：',x2.PRESCRIPTTIME) BREAK_RULE_CONTENT");
		sb.append(" from ");
		sb.append(fromTable).append(" x1 join tmp_dwb_charge_detail x2 on x1.visitid=x2.visitid and x1.itemcode=x2.itemcode");
		sb.append(" where x2.FUND_COVER>0");
		String alias = AbsHiveRuleHandle.WITH_TABLE_JUDGE;
		return new WithTableModel(alias, sb.toString());
	}
	
	@Override
	protected Map<String, String> parseUdfFieldMap() throws Exception {
		Map<String, String> fieldMap = super.parseUdfFieldMap();
		//重新设置违规金额、违规基金支出金额等字段取值
		fieldMap.put("AI_OUT_CNT", "y.AI_OUT_CNT");
		fieldMap.put("MIN_MONEY", "y.MIN_MONEY");
		fieldMap.put("MAX_MONEY", "y.MAX_MONEY");
		fieldMap.put("ACTION_MONEY", "y.MIN_ACTION_MONEY");
		fieldMap.put("MAX_ACTION_MONEY", "y.MAX_ACTION_MONEY");
		fieldMap.put("BREAK_RULE_CONTENT", "y.BREAK_RULE_CONTENT");
		return fieldMap;
	}

	private Integer getDuration(MedicalRuleConditionSet condition) {
		return Integer.parseInt(condition.getExt1());
	}
}
