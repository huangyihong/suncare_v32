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

import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 不能收费规则
 * @author  zhangly
 * Date: 2022年11月15日
 */
public class HiveUnChargeRuleHandle extends HiveRuleHandle {
	
	public HiveUnChargeRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, String datasource,
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, datasource, rule, ruleConditionList);
	}

	@Override
	protected String masterInfoJoinDwsChargeSql(boolean impala) throws Exception {
		String sql = super.masterInfoJoinDwsChargeSql(impala);
		sql = sql + " and y.ITEM_AMT>0";
		return sql;
	}

	@Override
	protected WithTableModel parseAccessCondition() {
		return null;
	}
	
	@Override
	protected WithTableModel parseJudgeCondition(String fromTable) {
		return null;
	}
}
