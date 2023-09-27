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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 *
 * 功能描述：合理用药二次处理排除慢性病种病人
 *
 * @author  zhangly
 * Date: 2021年3月23日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class RuleChronicHandle extends AbsRuleSecondHandle {

	public RuleChronicHandle(TaskProject task, TaskProjectBatch batch, MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList,
			Boolean trail) {
		super(task, batch, rule, ruleConditionList, trail);
	}

	@Override
	public void execute() throws Exception {
		Set<String> codeSet = new HashSet<String>();
		for (MedicalRuleConditionSet bean : ruleConditionList) {
			if (StringUtils.isNotBlank(bean.getExt2())) {
				String code = bean.getExt2();
				code = StringUtils.replace(code, ",", "|");
				codeSet.add(code);
			}			
		}
		if (codeSet.size()==0) {
			return;
		}
		String batchId = batch.getBatchId();
		String collection = trail ? EngineUtil.MEDICAL_TRAIL_DRUG_ACTION : EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
		boolean slave = false;

		String codes = StringUtils.join(codeSet, "|");
		codes = "(" + StringUtils.replace(codes, "|", " OR ") + ")";
		String[] array = StringUtils.split(rule.getItemCodes(), ",");
		for (String itemCode : array) {
			//排除慢性病种病人
			List<String> conditionList = new ArrayList<String>();			
			List<String> excludeList = new ArrayList<String>();
			conditionList.clear();
			conditionList.add("ITEMCODE:" + itemCode);
			conditionList.add("RULE_ID:" + rule.getRuleId());
			conditionList.add("BATCH_ID:" + batchId);
			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_CHRONIC_PATIENT", "CLIENTID", "CLIENTID");
			StringBuilder sb = new StringBuilder();
			sb.append("_query_:\"");
			sb.append(plugin.parse());
			sb.append("_query_:\\\"");
			plugin = new SolrJoinParserPlugin("STD_DIAGGROUP", "DISEASECODE", "CHRONICDIS_CODE");
			sb.append(plugin.parse());
			sb.append("DIAGGROUP_CODE").append(":").append(codes);
			sb.append("\\\"");
			sb.append(" OR ");
			sb.append("CHRONICDIS_CODE:").append(codes);
			sb.append("\"");
			conditionList.add(sb.toString());
			SolrUtil.exportDocByPager(conditionList, collection, slave, (doc, index) -> {
				excludeList.add(doc.get("id").toString());
				if(excludeList.size()>=1000) {
					this.deleteSolr(collection, slave, itemCode, excludeList);
					excludeList.clear();
				}
			});
			if(excludeList.size()>0) {
				this.deleteSolr(collection, slave, itemCode, excludeList);
				excludeList.clear();
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
}
