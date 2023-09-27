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

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.hive.model.JudgeWithTableScript;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.HiveJDBCUtil;

/**
 * 限支付时长规则
 * @author  zhangly
 * Date: 2022年11月15日
 */
public class HivePayDurationRuleHandle extends HiveRuleHandle {
	
	public HivePayDurationRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, String datasource,
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
		MedicalRuleConditionSet condition = this.getPayDuration();
		if(condition==null || StringUtils.isBlank(condition.getExt1())) {
			throw new RuntimeException(rule.getItemNames()+"规则未配置限制支出时长");
		}
		return parseCondition(condition, fromTable);
	}	
	
	private WithTableModel parseCondition(MedicalRuleConditionSet condition, String fromTable) {
		//黑名单使用天数>支付时长+1（+1减少假阳性）
  		int limit = Integer.parseInt(condition.getExt1());
		StringBuilder sb = new StringBuilder();
		//先统计药品使用的天数
		String prefix = "table_"+this.getClass().getSimpleName();
		String alias_day = prefix+"_day";
		sb.append("with ").append(alias_day).append(" as(");
		sb.append("select visitid,itemcode,");
		if(HiveJDBCUtil.enabledProcessGp()) {
			sb.append("count(distinct(to_char(prescripttime,'yyyy-mm-dd'))) day_cnt");
		} else {
			sb.append("count(distinct(substr(prescripttime,1,10))) day_cnt");
		}
		sb.append(",sum(AMOUNT) AMOUNT from dwb_charge_detail");
		sb.append(" where visitid in(select visitid from ").append(fromTable).append(")");
		sb.append(" and itemcode='").append(rule.getItemCodes()).append("'");
		sb.append(" group by visitid,itemcode");
		sb.append(")");
		sb.append("\n,");
		//使用次数超出
		String alias_over = prefix+"_over";
		sb.append(alias_over).append(" as(");
		sb.append("select visitid from ").append(fromTable);
		sb.append(" where visitid in(select visitid from ").append(alias_day);
		sb.append(" where day_cnt>").append(limit).append(" and AMOUNT>").append(limit).append(")");
		sb.append(")");
		sb.append("\n,");
		//统计药品使用当天之前使用次数
		String alias_before = prefix+"_before";
		sb.append(alias_before).append(" as(");
		sb.append("select x1.visitid,x1.itemcode,");
		if(HiveJDBCUtil.enabledProcessGp()) {
			sb.append("to_char(x1.prescripttime,'yyyy-mm-dd') daytime,to_char(x2.prescripttime,'yyyy-mm-dd') day_before");
		} else {
			sb.append("substr(x1.prescripttime,1,10) daytime,substr(x2.prescripttime,1,10) day_before");
		}
		sb.append(" from dwb_charge_detail x1 join dwb_charge_detail x2");
		sb.append(" on x1.visitid=x2.visitid and x1.itemcode=x2.itemcode and x1.itemcode='").append(rule.getItemCodes()).append("'");
		sb.append(" where x1.visitid in(select visitid from ").append(alias_over).append(")");
		sb.append(" and x1.prescripttime>x2.prescripttime");
		sb.append(")");
		sb.append("\n,");
		String alias_before_tj = prefix+"_before_tj";
		sb.append(alias_before_tj).append(" as(");
		sb.append("select visitid,itemcode,daytime,count(distinct(day_before)) day_before_cnt from ").append(alias_before);
		sb.append(" where visitid in(select visitid from ").append(alias_before).append(")");
		sb.append(" group by visitid,itemcode,daytime");
		sb.append(")");
		//计算超出的次数、金额等
		sb.append("\n,");
		sb.append("tmp_dwb_charge_detail as(");
		sb.append("select x1.visitid,x1.itemcode,");
		if(HiveJDBCUtil.enabledProcessGp()) {
			sb.append("string_agg(distinct x1.itemname_src,',') itemname_src,string_agg(distinct to_char(x1.PRESCRIPTTIME,'yyyy-mm-dd'),',') PRESCRIPTTIME");
		} else {
			sb.append("group_concat(distinct x1.itemname_src) itemname_src,group_concat(distinct x1.PRESCRIPTTIME) PRESCRIPTTIME");
		}
		sb.append(",sum(x1.AMOUNT) AMOUNT, sum(x1.FEE) FEE,sum(x1.FUND_COVER) FUND_COVER");
		sb.append(" from dwb_charge_detail x1 join ").append(alias_before_tj).append(" x2 on x1.visitid=x2.visitid and x1.itemcode=x2.itemcode");
		sb.append(" where x1.itemcode='").append(rule.getItemCodes()).append("'");
		if(HiveJDBCUtil.enabledProcessGp()) {
			sb.append(" and to_char(x1.prescripttime,'yyyy-mm-dd')=x2.daytime");
		} else {
			sb.append(" and substr(x1.prescripttime,1,10)=x2.daytime");
		}
		sb.append(" and x2.day_before_cnt>=").append(limit);
		sb.append(" group by x1.visitid,x1.itemcode");
		sb.append(")");
		sb.append("\n");
		sb.append("select x1.*");
		sb.append(",x2.AMOUNT AI_OUT_CNT");
		sb.append(",x2.FEE MIN_MONEY,x2.FEE MAX_MONEY");
		sb.append(",x2.FUND_COVER MIN_ACTION_MONEY,x2.FUND_COVER MAX_ACTION_MONEY");
		sb.append(",concat('超出用药支付时长发生日期：',x2.PRESCRIPTTIME) BREAK_RULE_CONTENT");
		sb.append(" from ");
		sb.append(fromTable).append(" x1 join tmp_dwb_charge_detail x2 on x1.visitid=x2.visitid and x1.itemcode=x2.itemcode");
		sb.append(" where x2.FUND_COVER>0");
		return new WithTableModel(AbsHiveRuleHandle.WITH_TABLE_JUDGE, sb.toString());
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

	private MedicalRuleConditionSet getPayDuration() {
		List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", null);
		if(judgeList==null || judgeList.size()==0) {
    		throw new RuntimeException(rule.getItemNames()+"规则未配置判断条件！");
    	}
    	for(MedicalRuleConditionSet bean : judgeList) {
			if(AbsRuleParser.RULE_CONDI_PAYDURATION.equals(bean.getField())) {
				return bean;
			}
		}
    	return null;
	}
}
