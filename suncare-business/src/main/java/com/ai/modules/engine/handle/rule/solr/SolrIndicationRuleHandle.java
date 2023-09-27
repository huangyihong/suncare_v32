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
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.handle.rule.parse.RuleIndicationParser;
import com.ai.modules.engine.handle.rule.parse.RuleMasterInfoParser;
import com.ai.modules.engine.handle.rule.parse.RuleParser;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 
 * 功能描述：适应症、禁忌症
 *
 * @author  zhangly
 * Date: 2021年3月24日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class SolrIndicationRuleHandle extends SolrRuleHandle {
	
	public SolrIndicationRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, MedicalRuleConfig rule,
			List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, rule, ruleConditionList);
	}
	
	/**
	 * 解析判断条件
	 */
	@Override
	protected String parseJudgeCondition() {
		boolean isPatient = this.isPatient(); //是否检查病人以往历史病例
		Set<String> exclude = new HashSet<String>();
    	exclude.add("reviewHisDisease");
    	exclude.add("excludeInHosp");
    	List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", exclude);
    	if(judgeList!=null) {
    		List<String> wheres = new ArrayList<String>();
    		for(MedicalRuleConditionSet judge : judgeList) {
    			String where = this.parseCondition(judge, isPatient);
    			if(StringUtils.isNotBlank(where)) {
    				wheres.add(where);
    			}
    		}
    		//组与组之间默认or关系
    		String condition = StringUtils.join(wheres, " OR ");
    		if(AbsRuleParser.RULE_LIMIT_INDICATION.equals(rule.getRuleLimit())
    				|| AbsRuleParser.RULE_LIMIT_INDICATION1.equals(rule.getRuleLimit())) {
    			//适应症黑名单取反
    			condition = "*:* -(" + condition + ")";
    		}
    		String filter = this.filterCondition();
    		if(StringUtils.isNotBlank(filter)) {
    			condition = condition + " AND " + filter;
    		}
    		return condition;
    	}
    	return null;
	}

	private String parseCondition(MedicalRuleConditionSet bean, boolean isPatient) {
		AbsRuleParser parser = null;
		String condiType = bean.getField();
		if(AbsRuleParser.RULE_MASTER_SET.contains(condiType)) {
			//关联master_info
			parser = new RuleMasterInfoParser(rule, bean);
		} else if(AbsRuleParser.RULE_CONDI_INDICATION.equals(condiType)
				|| AbsRuleParser.RULE_CONDI_UNINDICATION.equals(condiType)) {
			//适应症、禁忌症
			RuleIndicationParser ruleParser = new RuleIndicationParser(rule, bean);
			ruleParser.setPatient(isPatient);
			parser = ruleParser;
		} else {
			parser = new RuleParser(rule, bean);
		}
		return parser.parseCondition();		
	}
	
	/**
	 * 
	 * 功能描述：是否检查病人以往历史病例
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年3月22日 下午3:33:02</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private boolean isPatient() {
		List<MedicalRuleConditionSet> conditionList = ruleConditionList.stream().filter(s->"reviewHisDisease".equals(s.getField())).collect(Collectors.toList());
		if(conditionList!=null && conditionList.size()>0) {
			MedicalRuleConditionSet bean = conditionList.get(0);
			return "1".equals(bean.getExt1()) ? true : false;
		}
		return false;
	}
	
	/**
	 * 
	 * 功能描述：是否排除住院数据
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年8月11日 上午9:11:31</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private String filterCondition() {
		List<MedicalRuleConditionSet> conditionList = ruleConditionList.stream().filter(s->"excludeInHosp".equals(s.getField())).collect(Collectors.toList());
		if(conditionList!=null && conditionList.size()>0) {
			MedicalRuleConditionSet bean = conditionList.get(0);
			boolean flag = "1".equals(bean.getExt1()) ? true : false;
			if(flag) {
				SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_MASTER_INFO", "VISITID", "VISITID");
				return plugin.parse() + "-VISITTYPE_ID:ZY*";
			}
		}
		return null;
	}
}
