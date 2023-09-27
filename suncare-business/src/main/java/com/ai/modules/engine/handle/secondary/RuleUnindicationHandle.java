/**
 * SecondLineDrugHandle.java	  V1.0   2020年12月4日 上午10:05:13
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.handle.secondary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.DateUtils;

import com.ai.common.MedicalConstant;
import com.ai.modules.engine.model.rule.EngineParamIndicationRule;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

import cn.hutool.core.date.DateUtil;

/**
 *
 * 功能描述：禁忌症规则二次处理
 *
 * @author  zhangly
 * Date: 2021年3月23日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class RuleUnindicationHandle extends AbsRuleSecondHandle {

	public RuleUnindicationHandle(TaskProject task, TaskProjectBatch batch, MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList,
			Boolean trail) {
		super(task, batch, rule, ruleConditionList, trail);
	}

	@Override
	public void execute() throws Exception {
		boolean exists = false;// 是否是检查病人以往历史病例禁忌症规则
		for (MedicalRuleConditionSet bean : ruleConditionList) {
			if ("reviewHisDisease".equals(bean.getField())) {
				if ("1".equals(bean.getExt1())) {
					exists = true;
				}
			}
		}
		if (!exists) {
			return;
		}
		String batchId = batch.getBatchId();
		String collection = trail ? EngineUtil.MEDICAL_TRAIL_DRUG_ACTION : EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
		boolean slave = false;
		List<MedicalRuleConditionSet> judgeList = ruleConditionList.stream().filter(s->"judge".equals(s.getType())).collect(Collectors.toList());
		judgeList = judgeList.stream().filter(s->!"reviewHisDisease".equals(s.getField())).collect(Collectors.toList());
		List<String> diagWheres = new ArrayList<String>();//疾病组查询条件
		for(MedicalRuleConditionSet bean : judgeList) {
			EngineParamIndicationRule paramRule = new EngineParamIndicationRule("DIAGGROUP_CODE", bean.getExt2());
			diagWheres.add(paramRule.where());
		}
		String batch_endTime = MedicalConstant.DEFAULT_END_TIME;
		batch_endTime = batch.getEndTime()!=null ? DateUtil.format(batch.getEndTime(), "yyyy-MM-dd") : batch_endTime;

		String[] array = StringUtils.split(rule.getItemCodes(), ",");
		for (String itemCode : array) {
			Map<String, String> unindicationDayMap = new HashMap<String, String>();//病人禁忌症出现日期集合
			List<String> conditionList = new ArrayList<String>();
			String where = "_query_:\"%sITEMCODE:%s AND RULE_ID:%s AND BATCH_ID:%s\"";
			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(collection, "CLIENTID", "CLIENTID");
			where = String.format(where, plugin.parse(), itemCode, rule.getRuleId(), batchId);
			conditionList.add(where);
			//任务批次最大时间限制
			where = "VISITDATE:{* TO %s]";
			where = String.format(where, batch_endTime);
			conditionList.add(where);
			//规则最大时间限制
			where = "VISITDATE:{* TO %s]";
			where = String.format(where, DateUtil.format(rule.getEndTime(), "yyyy-MM-dd"));
			conditionList.add(where);
			//疾病组与组之间默认or关系
			conditionList.add(StringUtils.join(diagWheres, " OR "));
			int count = SolrUtil.exportDocByPager(conditionList, EngineUtil.DWB_MASTER_INFO, slave, (doc, index) -> {
				String clientid = doc.get("CLIENTID").toString();
				String day = doc.get("VISITDATE").toString();
				day = DateUtils.dateformat(day, "yyyy-MM-dd");
				if(!unindicationDayMap.containsKey(clientid)) {
					unindicationDayMap.put(clientid, day);
				} else {
					String temp = unindicationDayMap.get(clientid);
					unindicationDayMap.put(clientid, minDay(temp, day));
				}
			});
			if(count>0) {
				//排除病人禁忌症发生日期之前的病例
				List<String> excludeList = new ArrayList<String>();
				conditionList.clear();
				conditionList.add("ITEMCODE:" + itemCode);
				conditionList.add("RULE_ID:" + rule.getRuleId());
				conditionList.add("BATCH_ID:" + batchId);
				SolrUtil.exportDocByPager(conditionList, collection, slave, (doc, index) -> {
					String clientid = doc.get("CLIENTID").toString();
					String day = doc.get("VISITDATE").toString();
					day = DateUtils.dateformat(day, "yyyy-MM-dd");
					if(unindicationDayMap.containsKey(clientid)) {
						String temp = unindicationDayMap.get(clientid);
						if(day.compareTo(temp)<0) {
							//病例在禁忌症日期之前
							excludeList.add(doc.get("id").toString());
						}
						if(excludeList.size()>=1000) {
							this.deleteSolr(collection, slave, itemCode, excludeList);
							excludeList.clear();
						}
					} else {
						excludeList.add(doc.get("id").toString());
					}
				});
				if(excludeList.size()>0) {
					this.deleteSolr(collection, slave, itemCode, excludeList);
					excludeList.clear();
				}
			}
		}
	}

	private void deleteSolr(String collection, boolean slave, String itemCode, List<String> excludeList) {
		String query = "ITEMCODE:%s AND RULE_ID:%s AND BATCH_ID:%s AND id:(%s)";
		query = String.format(query, itemCode, rule.getRuleId(), batch.getBatchId(), StringUtils.join(excludeList, " OR "));
		try {
			SolrUtil.delete(collection, query, slave);
		} catch (Exception e) {
		}
	}

	/**
	 *
	 * 功能描述：取两天中的最小值
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年3月24日 上午10:55:39</p>
	 *
	 * @param day1
	 * @param day2
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private String minDay(String day1, String day2) {
		if(day1.compareTo(day2)>0) {
			return day2;
		}
		return day1;
	}
}
