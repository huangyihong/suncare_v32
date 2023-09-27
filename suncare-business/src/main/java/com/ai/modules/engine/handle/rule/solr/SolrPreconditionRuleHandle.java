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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.rule.EngineParamGrpRule;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 
 * 功能描述：必要前提条件规则
 *
 * @author  zhangly
 * Date: 2021年3月24日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class SolrPreconditionRuleHandle extends SolrRuleHandle {
	
	public SolrPreconditionRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, MedicalRuleConfig rule,
			List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, rule, ruleConditionList);
	}
	
	/**
	 * 解析判断条件
	 */
	@Override
	protected String parseJudgeCondition() {
		Set<String> exclude = new HashSet<String>();
    	exclude.add("fitTimeRange");
		List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", exclude);
    	if(judgeList!=null) {
    		List<String> wheres = new ArrayList<String>();
    		for(MedicalRuleConditionSet judge : judgeList) {
    			String where = this.parseCondition(judge);
    			if(StringUtils.isNotBlank(where)) {
    				wheres.add(where);
    			}
    		}
    		//组与组之间默认or关系
    		String condition = StringUtils.join(wheres, " OR ");
    		//黑名单取反
			condition = "*:* -(" + condition + ")";
    		return condition;
    	}
    	return null;
	}

	private String parseCondition(MedicalRuleConditionSet bean) {
		List<String> wheres = new ArrayList<String>();
		if(StringUtils.isNotBlank(bean.getExt1())) {
			EngineParamGrpRule paramRule = new EngineParamGrpRule("STD_TREATGROUP", "TREATGROUP_CODE", bean.getExt1());
			paramRule.setRelation("2");
			wheres.add(paramRule.where());
		}
		if(StringUtils.isNotBlank(bean.getExt3())) {
			EngineParamGrpRule paramRule = new EngineParamGrpRule("STD_DRUGGROUP", "DRUGGROUP_CODE", bean.getExt3());
			wheres.add(paramRule.where());
		}
		return StringUtils.join(wheres, " OR ");
	}
}
