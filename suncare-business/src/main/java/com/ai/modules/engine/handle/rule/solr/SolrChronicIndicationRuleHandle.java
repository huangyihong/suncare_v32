/**
 * SolrChronicIndicationRuleHandle.java	  V1.0   2021年6月17日 下午2:18:32
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

import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.model.rule.AbsEngineParamRule;
import com.ai.modules.engine.model.rule.EngineParamIndicationRule;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 
 * 功能描述：门慢适应症
 *
 * @author  zhangly
 * Date: 2021年6月17日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class SolrChronicIndicationRuleHandle extends SolrRuleHandle {

	public SolrChronicIndicationRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail,
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, rule, ruleConditionList);
	}

	@Override
	protected String parseAccessCondition() {
		SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_MASTER_INFO", "VISITID", "VISITID");
		return plugin.parse() + "VISITTYPE_ID:(MZ01.01.12 OR MZ01.01.15)";
	}
	
	/**
	 * 解析判断条件
	 */
	@Override
	protected String parseJudgeCondition() {
		Set<String> exclude = new HashSet<String>();
    	List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", exclude);
    	if(judgeList!=null) {
    		List<String> wheres = new ArrayList<String>();
    		for(MedicalRuleConditionSet judge : judgeList) {
    			String where = this.parseCondition(judge);
    			if(StringUtils.isNotBlank(where)) {
    				wheres.add(where);
    			}
    		}
    		//慢病病种病人
    		wheres.add(this.filterChronicCondition(judgeList));
    		//组与组之间默认or关系
    		String condition = StringUtils.join(wheres, " OR ");
    		//黑名单取反
    		condition = "*:* -(" + condition + ")";
    		return condition;
    	}
    	return null;
	}

	private String parseCondition(MedicalRuleConditionSet bean) {
		AbsEngineParamRule paramRule = null;
		String condiType = bean.getField();
		if(AbsRuleParser.RULE_CONDI_DISEASEGRP.equals(condiType)) {
			//疾病组
			paramRule = new EngineParamIndicationRule("DIAGGROUP_CODE", bean.getExt1());
			paramRule.setPatient(true);
		} else {
			throw new RuntimeException("未找到判断条件类型！");
		}
        return paramRule.where();		
	}
	
	/**
	 * 
	 * 功能描述：过滤掉慢病病种病人
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年7月27日 上午10:54:43</p>
	 *
	 * @param judgeList
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private String filterChronicCondition(List<MedicalRuleConditionSet> judgeList) {
		Set<String> codeSet = new HashSet<String>();
		for (MedicalRuleConditionSet bean : judgeList) {
			if (StringUtils.isNotBlank(bean.getExt1())) {
				String code = bean.getExt1();
				code = StringUtils.replace(code, ",", "|");
				codeSet.add(code);
			}			
		}
		String codes = StringUtils.join(codeSet, "|");
		codes = "(" + StringUtils.replace(codes, "|", " OR ") + ")";
		StringBuilder sb = new StringBuilder();
		SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_CHRONIC_PATIENT", "CLIENTID", "CLIENTID");		
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
		return sb.toString();
	}
}
