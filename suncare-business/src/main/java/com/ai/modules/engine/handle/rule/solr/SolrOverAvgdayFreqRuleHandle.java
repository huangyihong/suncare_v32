/**
 * SolrIndicationRuleHandle.java	  V1.0   2021年3月22日 下午3:12:07
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.solr;

import java.util.List;

import com.ai.modules.engine.model.rule.EngineRuleMasterInfo;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 
 * 功能描述：日均限频次规则
 *
 * @author  zhangly
 * Date: 2021年3月24日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class SolrOverAvgdayFreqRuleHandle extends SolrRuleHandle {
	
	public SolrOverAvgdayFreqRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, MedicalRuleConfig rule,
			List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, rule, ruleConditionList);
	}

	@Override
	protected List<String> parseCondition() {
		List<String> wheres = super.parseCondition();
		//追加就诊类型=住院过滤条件
	    EngineRuleMasterInfo paramRule = new EngineRuleMasterInfo();
	    paramRule.setJzlx("ZY01");
	    wheres.add(paramRule.where());
		return wheres;
	}
}
