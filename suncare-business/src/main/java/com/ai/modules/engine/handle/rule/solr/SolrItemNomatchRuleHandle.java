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
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.model.rule.AbsEngineParamRule;
import com.ai.modules.engine.model.rule.EngineParamGrpRule;
import com.ai.modules.engine.model.rule.EngineParamIndicationRule;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 
 * 功能描述：项目与既往项目不符
 *
 * @author  zhangly
 * Date: 2021年3月24日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class SolrItemNomatchRuleHandle extends SolrRuleHandle {
	
	public SolrItemNomatchRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, MedicalRuleConfig rule,
			List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, rule, ruleConditionList);
	}
	
	/**
	 * 
	 * 功能描述：解析判断条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年3月24日 下午3:13:34</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	@Override
	protected String parseJudgeCondition() {
		//限定条件
    	List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", null);
    	if(judgeList!=null) {
    		List<String> wheres = new ArrayList<String>();
    		for(MedicalRuleConditionSet bean : judgeList) {
    			String where = this.parseCondition(bean);
    			if(StringUtils.isNotBlank(where)) {
    				wheres.add(where);
    			}
    		}
    		String condition = StringUtils.join(wheres, " AND ");
        	//禁忌规则不取反
    		return condition;
    	}
    	return null;
	}
	
	private String parseCondition(MedicalRuleConditionSet bean) {
		AbsEngineParamRule paramRule = null;
		String condiType = bean.getField();
		if(AbsRuleParser.RULE_CONDI_HISGROUPS.equals(condiType)) {
			paramRule = new EngineParamGrpRule("STD_TREATGROUP", "TREATGROUP_CODE", bean.getExt1());
	        paramRule.setPatient(true);
		} else if(AbsRuleParser.RULE_CONDI_DISEASEGRP.equals(condiType)) {
			paramRule = new EngineParamIndicationRule("DIAGGROUP_CODE", bean.getExt1());
		}
        return paramRule.where();
	}
}
