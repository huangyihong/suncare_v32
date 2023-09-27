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
import java.util.stream.Collectors;

import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 适应症、禁忌症规则
 * @author  zhangly
 * Date: 2022年11月15日
 */
public class HiveIndicationRuleHandle extends HiveRuleHandle {
	
	public HiveIndicationRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, String datasource,
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, datasource, rule, ruleConditionList);
	}

	@Override
	protected String masterInfoJoinDwsChargeSql(boolean impala) throws Exception {
		String sql = super.masterInfoJoinDwsChargeSql(impala);
		if(filterInHosp()) {
			//排除住院数据
			sql = sql + " and x.VISITTYPE_ID not like 'ZY%'";
		}
		return sql;
	}
	
	/**
	 * 
	 * 功能描述：是否排除住院数据
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	private boolean filterInHosp() {
		List<MedicalRuleConditionSet> conditionList = ruleConditionList.stream().filter(s->"excludeInHosp".equals(s.getField())).collect(Collectors.toList());
		if(conditionList!=null && conditionList.size()>0) {
			MedicalRuleConditionSet bean = conditionList.get(0);
			boolean flag = "1".equals(bean.getExt1()) ? true : false;
			return flag;
		}
		return false;
	}
}
