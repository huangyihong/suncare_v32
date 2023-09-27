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

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.MD5Util;

import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.hive.model.JudgeWithTableScript;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.model.ColumnType;
import com.ai.modules.engine.model.rule.hive.AbsHiveParamRule;
import com.ai.modules.engine.model.rule.hive.HiveParamGrpRule;
import com.ai.modules.engine.model.rule.hive.HiveParamSelfJoinRule;
import com.ai.modules.engine.util.PlaceholderResolverUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;

/**
 * 合用不予支付规则
 * @author  zhangly
 * Date: 2022年11月15日
 */
public class HiveMutexUnpayRuleHandle extends HiveRuleHandle {
	
	public HiveMutexUnpayRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, String datasource,
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
			sql.append("\n,");
			WithTableModel mutexWithTable = this.mutexWithTable(fromTable);
			sql.append(mutexWithTable.getAlias()).append(" as (").append(mutexWithTable.getSql()).append(")");
			//创建结果表
			sql.append("\n,");
			sql.append(AbsHiveRuleHandle.WITH_TABLE_RESULT).append(" as (");
			sql.append("select x1.*");
			sql.append(",x2.MUTEX_ITEM_CODE");
			sql.append(",x2.MUTEX_ITEM_NAME");
			sql.append(" from ").append(judgeTable.getAlias()).append(" x1 join ").append(mutexWithTable.getAlias()).append(" x2");
			sql.append(" on x1.visitid=x2.visitid");
			sql.append(")");
			return new JudgeWithTableScript(sql.toString(), AbsHiveRuleHandle.WITH_TABLE_RESULT);
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

	private WithTableModel parseCondition(MedicalRuleConditionSet condition, String fromTable) {
		AbsHiveParamRule paramRule = null;
		String condiType = condition.getField();
		if(AbsRuleParser.RULE_CONDI_UNPAYDRUG.equals(condiType)) {
			//合用不予支付
			if("DRUGGROUP".equals(condition.getExt2())) {
				//药品组
				HiveParamGrpRule sub = new HiveParamGrpRule("STD_DRUGGROUP", "DRUGGROUP_CODE", condition.getExt1(), fromTable);
				sub.addCondition("ATC_DRUGCODE<>'"+rule.getItemCodes()+"'");
				paramRule = sub;
			} else {
				//药品
				HiveParamSelfJoinRule sub = new HiveParamSelfJoinRule("ITEMCODE", condition.getExt1(), fromTable);
				//排除主体药品
				sub.addCondition("ITEMCODE<>'"+rule.getItemCodes()+"'");
				sub.addCondition("FUND_COVER>0");
				sub.addCondition("SELFPAY_PROP_MIN>=0");
				sub.addCondition("SELFPAY_PROP_MIN<1");
				paramRule = sub;
			}
		}
		String sql = paramRule.where();
		return new WithTableModel(WithTableUtil.buildWithTable(condition), sql);
	}
	
	/**
	 * 
	 * 功能描述：统计冲突项目with脚本
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	private WithTableModel mutexWithTable(String fromTable) {
		List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", null);
		MedicalRuleConditionSet condition = judgeList.get(0);
		StringBuilder sql = new StringBuilder();
		String alias = "table_mutex";
		sql.append("select visitid, group_concat(distinct(itemcode)) MUTEX_ITEM_CODE,group_concat(distinct(concat(itemcode,'::',itemname))) MUTEX_ITEM_NAME from dws_patient_1visit_itemsum");
		sql.append(" where visitid in(select visitid from ").append(fromTable).append(")");
		String condiType = condition.getField();
		if(AbsRuleParser.RULE_CONDI_UNPAYDRUG.equals(condiType)) {
			//合用不予支付
			if("DRUGGROUP".equals(condition.getExt2())) {
				//药品组
				String where = " and itemcode in(select ATC_DRUGCODE from STD_DRUGGROUP where DRUGGROUP_CODE='%s')";
				where = String.format(where, condition.getExt1());
				sql.append(where);
				sql.append(" and itemcode<>'").append(rule.getItemCodes()).append("'");
				sql.append(" and ITEM_QTY>0");
				sql.append(" and ITEM_AMT>0");
				sql.append(" and FUND_COVER>0");
			} else {
				//药品
				sql.append(" and itemcode='").append(condition.getExt1()).append("'");
				sql.append(" and ITEM_QTY>0");
				sql.append(" and ITEM_AMT>0");
				sql.append(" and FUND_COVER>0");
			}
		}
		sql.append(" group by visitid");
		return new WithTableModel(alias, sql.toString());
	}

	@Override
	protected Map<String, String> parseUdfFieldMap() throws Exception {
		Map<String, String> fieldMap = super.parseUdfFieldMap();
		//设置互斥字段
		fieldMap.put("MUTEX_ITEM_CODE", "y.MUTEX_ITEM_CODE");
		fieldMap.put("MUTEX_ITEM_NAME", "y.MUTEX_ITEM_NAME");
		return fieldMap;
	}

	@Override
	protected JSONObject parseJSONObject(ResultSet rs, Set<ColumnType> columnSet) throws Exception {
		JSONObject jsonObject = super.parseJSONObject(rs, columnSet);
		//重新设置id生成策略
		String mutexItemcode = jsonObject.getString("MUTEX_ITEM_CODE");
		String template = "${batchId}_${ruleId}_${itemCode}_${visitid}_${mutex_item_code}";
        Properties properties = new Properties();
        properties.put("batchId", batch.getBatchId());
        properties.put("ruleId", rule.getRuleId());
        properties.put("itemCode", jsonObject.get("ITEMCODE"));
        properties.put("visitid", jsonObject.get("VISITID"));
        properties.put("mutex_item_code", this.sortMutexItemcode(mutexItemcode));
        template = PlaceholderResolverUtil.replacePlaceholders(template, properties);
        String id = MD5Util.MD5Encode(template, "UTF-8");
        jsonObject.put("id", id);
        String[] arraycode = StringUtils.split(mutexItemcode, ",");
		jsonObject.put("MUTEX_ITEM_CODE", arraycode);
        String mutexItemname = jsonObject.getString("MUTEX_ITEM_NAME");
		String[] arrayname = StringUtils.split(mutexItemname, ",");
		jsonObject.put("MUTEX_ITEM_NAME", arrayname);
		return jsonObject;
	}
}
