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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.MD5Util;

import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.hive.model.JudgeWithTableScript;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.model.ColumnType;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.PlaceholderResolverUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.HiveJDBCUtil;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 功能描述：一日互斥规则（互斥项目组中的项目作为违规项目，规则主体项目作为冲突项目）
 *
 * @author  zhangly
 * Date: 2020年11月4日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HiveOnedayMutexRuleHandle extends HiveOnedayRuleHandle {
		
	public HiveOnedayMutexRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, String datasource,
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, datasource, rule, ruleConditionList);
	}
	
	@Override
	protected JudgeWithTableScript judgeWithTableScript(String fromTable) throws Exception {
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
	protected WithTableModel parseJudgeCondition(String fromTable) throws Exception {
		StringBuilder sb = new StringBuilder();
		//互斥项目组
		sb.append("with table_mutex as(");
		sb.append("select dws_patient_1visit_1day_itemsum.* from dws_patient_1visit_1day_itemsum");
		if(this.isProjectGrp()) {
			//项目组
			String where = "itemcode in(select std.TREATCODE from medical_gbdp.STD_TREATGROUP std where TREATGROUP_CODE='%s')";
			where = String.format(where, rule.getItemCodes());
			sb.append(" where ").append(where);
		} else {
			sb.append(" where itemcode='").append(rule.getItemCodes()).append("'");
		}
		sb.append(" and FUND_COVER>0");
		sb.append(" and SELFPAY_PROP_MIN>=0");
		sb.append(" and SELFPAY_PROP_MIN<1");
		sb.append(" and ITEM_QTY>0");
		sb.append(" and ITEM_AMT>0");
		sb.append(this.appendCommonWhere());
		sb.append(")");
		sb.append("\n");
		sb.append("select x1.* from ");
		sb.append(fromTable).append(" x1");
		sb.append(" where");
		sb.append(" exists(select 1 from table_mutex x2 where x1.visitid=x2.visitid and x1.PRESCRIPTTIME_DAY=x2.PRESCRIPTTIME_DAY and x1.itemcode<>x2.itemcode)");
		String alias = AbsHiveRuleHandle.WITH_TABLE_JUDGE;
		return new WithTableModel(alias, sb.toString());
	}
	
	@Override
	protected List<String> parseWhere() {
		String groupCode = parseGroupcode();
		groupCode = "'"+StringUtils.replace(groupCode, "|", "','") + "'";
		List<String> wheres = new ArrayList<String>();
		String sql = "itemcode in(select TREATCODE from medical_gbdp.STD_TREATGROUP std where std.TREATGROUP_CODE in("+groupCode+"))";
		wheres.add(sql);
		return wheres;
	}

	/**
	 * 
	 * 功能描述：互斥项目组
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	private String parseGroupcode() {		
		Set<String> groupcodeSet = new HashSet<String>();
    	List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", null);
    	if(judgeList==null || judgeList.size()==0) {
    		throw new RuntimeException(rule.getItemNames()+"规则未配置判断条件！");
    	}
    	for(MedicalRuleConditionSet bean : judgeList) {
			groupcodeSet.add(bean.getExt1());
		}    
    	String result = StringUtils.join(groupcodeSet, "|");
    	return result;
	}
	
	@Override
	protected Map<String, String> parseUdfFieldMap() throws Exception {
		Map<String, String> fieldMap = super.parseUdfFieldMap();
		//设置互斥字段
		String mutexItemcode = rule.getItemCodes();
		String mutexItemname = rule.getItemCodes().concat(EngineUtil.SPLIT_KEY).concat(rule.getItemNames());
		if(HiveJDBCUtil.isHive()) {
			mutexItemname = StringUtils.replace(mutexItemname, "'", "\\'");
		} else {
			mutexItemname = StringUtils.replace(mutexItemname, "'", "''");
		}
		fieldMap.put("MUTEX_ITEM_CODE", "'"+mutexItemcode+"'");
		fieldMap.put("MUTEX_ITEM_NAME", "'"+mutexItemname+"'");
		fieldMap.put("CHARGEDATE", "y.PRESCRIPTTIME_DAY");
		fieldMap.put("BREAK_RULE_CONTENT", "concat('互斥发生日期：', y.PRESCRIPTTIME_DAY)");
		return fieldMap;
	}

	@Override
	protected JSONObject parseJSONObject(ResultSet rs, Set<ColumnType> columnSet) throws Exception {
		JSONObject jsonObject = super.parseJSONObject(rs, columnSet);
		//重新设置id生成策略
		String template = "${batchId}_${ruleId}_${itemCode}_${visitid}_${mutex_item_code}_${day}";
        Properties properties = new Properties();
        properties.put("batchId", batch.getBatchId());
        properties.put("ruleId", rule.getRuleId());
        properties.put("itemCode", jsonObject.get("ITEMCODE"));
        properties.put("visitid", jsonObject.get("VISITID"));
        properties.put("mutex_item_code", jsonObject.get("MUTEX_ITEM_CODE"));
        properties.put("day", jsonObject.get("CHARGEDATE"));
        template = PlaceholderResolverUtil.replacePlaceholders(template, properties);
        String id = MD5Util.MD5Encode(template, "UTF-8");
        jsonObject.put("id", id);
		return jsonObject;
	}
}
