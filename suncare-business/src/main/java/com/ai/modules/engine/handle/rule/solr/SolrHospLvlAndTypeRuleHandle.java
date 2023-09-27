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
import com.ai.modules.engine.model.rule.EngineParamOrgRule;
import com.ai.modules.engine.model.rule.EngineRuleMasterInfo;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 
 * 功能描述：医疗机构级别类别规则
 *
 * @author  zhangly
 * Date: 2021年3月24日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class SolrHospLvlAndTypeRuleHandle extends SolrRuleHandle {
	
	public SolrHospLvlAndTypeRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, MedicalRuleConfig rule,
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
    		String condition = StringUtils.join(wheres, " OR ");
        	//黑名单取反
    		condition = "*:* -(" + condition + ")";
    		return condition;
    	}    	
    	return null;
	}
	
	private String parseCondition(MedicalRuleConditionSet bean) {
		StringBuilder sb = new StringBuilder();
		String condiType = bean.getField();
		if(AbsRuleParser.RULE_CONDI_HOSPLEVELTYPE.equals(condiType)) {
			if(StringUtils.isNotBlank(bean.getExt1())) {
				EngineRuleMasterInfo paramRule = new EngineRuleMasterInfo();
				paramRule.setYyjb(bean.getExt1());
				sb.append(paramRule.where());
			}
			if(StringUtils.isNotBlank(bean.getExt3())) {				
				if(sb.length()>0 && StringUtils.isNotBlank(bean.getExt2())) {
					sb.append(" ").append(bean.getExt2()).append(" ");
				}
				EngineParamOrgRule paramRule = new EngineParamOrgRule("ORGTYPE_CODE", bean.getExt3());
				sb.append(paramRule.where());
			}
		}
        return "(" + sb.toString() + ")";
	}
}
