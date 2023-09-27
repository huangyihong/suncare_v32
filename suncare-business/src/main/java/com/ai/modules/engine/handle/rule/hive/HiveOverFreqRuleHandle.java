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

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jeecg.common.util.MD5Util;

import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.hive.model.JudgeWithTableScript;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.model.ColumnType;
import com.ai.modules.engine.model.rule.hive.BaseHiveParamRule;
import com.ai.modules.engine.model.rule.hive.HiveRuleMasterInfo;
import com.ai.modules.engine.util.PlaceholderResolverUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 功能描述：收费合规一日就诊限频次规则
 *
 * @author  zhangly
 * Date: 2020年11月4日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HiveOverFreqRuleHandle extends HiveOnedayRuleHandle {
		
	public HiveOverFreqRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, String datasource,
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
	
	@Override
	protected WithTableModel parseAccessCondition() throws Exception {
		List<WithTableModel> withTableList = new ArrayList<WithTableModel>();
		WithTableModel withTable = super.parseAccessCondition();
		if(withTable!=null) {
			withTable.setAlias(AbsHiveRuleHandle.WITH_TABLE_ACCESS+"_0");
			withTableList.add(withTable);
		}
		String fromTable = AbsHiveRuleHandle.WITH_TABLE_ONEDAY;
		//追加就诊类型=住院过滤条件
	    HiveRuleMasterInfo paramRule = new HiveRuleMasterInfo(fromTable);
	    paramRule.setJzlx("ZY01");
	    withTable = new WithTableModel(AbsHiveRuleHandle.WITH_TABLE_ACCESS+"_1", paramRule.where());
	    withTableList.add(withTable);
	    String sql = WithTableUtil.parseWithTableList(fromTable, withTableList);
	    return new WithTableModel(AbsHiveRuleHandle.WITH_TABLE_ACCESS, sql);
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
		String compare = condition.getCompare();
		String frequency = condition.getExt2();
		//一日就诊限频次
		BaseHiveParamRule paramRule = new BaseHiveParamRule("ITEM_QTY", compare, frequency, fromTable);
		//黑名单取反
		paramRule.setReverse(true);
		String sql = "select * from "+fromTable+" where "+paramRule.where();
		return new WithTableModel(AbsHiveRuleHandle.WITH_TABLE_RESULT, sql);	
	}
	
	@Override
	protected Map<String, String> parseUdfFieldMap() throws Exception {
		Map<String, String> fieldMap = super.parseUdfFieldMap();
		//重新设置违规金额、违规基金支出金额等字段取值
		List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", null);
    	MedicalRuleConditionSet condition = judgeList.get(0);
		String frequency = condition.getExt2();
		fieldMap.put("AI_ITEM_CNT", "y.ITEM_QTY");
		String text = "y.ITEM_QTY-"+frequency;
		fieldMap.put("AI_OUT_CNT", text);
		text = "(y.ITEM_QTY-"+frequency+")*y.ITEMPRICE_MAX";
		fieldMap.put("MIN_MONEY", text);
		fieldMap.put("MAX_MONEY", text);
		text = "(y.FUND_COVER/y.ITEM_QTY)*(y.ITEM_QTY-"+frequency+")";
		fieldMap.put("ACTION_MONEY", text);
		fieldMap.put("MAX_ACTION_MONEY", text);
		fieldMap.put("CHARGEDATE", "y.PRESCRIPTTIME_DAY");
		fieldMap.put("BREAK_RULE_CONTENT", "concat('超频次/数量发生时间：',y.PRESCRIPTTIME_DAY)");
		return fieldMap;
	}
	
	@Override
	protected JSONObject parseJSONObject(ResultSet rs, Set<ColumnType> columnSet) throws Exception {
		JSONObject jsonObject = super.parseJSONObject(rs, columnSet);
		//重新设置id生成策略
		String template = "${batchId}_${ruleId}_${itemCode}_${visitid}_${day}";
        Properties properties = new Properties();
        properties.put("batchId", batch.getBatchId());
        properties.put("ruleId", rule.getRuleId());
        properties.put("itemCode", jsonObject.get("ITEMCODE"));
        properties.put("visitid", jsonObject.get("VISITID"));
        properties.put("day", jsonObject.get("CHARGEDATE"));
        template = PlaceholderResolverUtil.replacePlaceholders(template, properties);
        String id = MD5Util.MD5Encode(template, "UTF-8");
        jsonObject.put("id", id);
		return jsonObject;
	}
}
