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
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.modules.config.service.IMedicalOtherDictService;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 
 * 功能描述：医保限定用药量规则
 *
 * @author  zhangly
 * Date: 2021年3月24日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class SolrDosageRuleHandle extends SolrRuleHandle {
	
	public SolrDosageRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, MedicalRuleConfig rule,
			List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, rule, ruleConditionList);
	}
	
	@Override
	public void generateUnreasonableAction() throws Exception {
		boolean isVisit = this.isVisit();
		if(isVisit) {
			super.generateUnreasonableAction();
		}
	}
	
	@Override
	protected String parseJudgeCondition() {
		//限定条件
		Set<String> exclude = new HashSet<String>();
		exclude.add("durationType");
    	List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", exclude);
    	if(judgeList!=null) {
    		List<String> wheres = new ArrayList<String>();
    		for(MedicalRuleConditionSet bean : judgeList) {
    			wheres.add(this.parseCondition(bean));
    		}
    		String condition = StringUtils.join(wheres, " AND ");
        	return condition;
    	}
    	return null;
	}
	
	private String parseCondition(MedicalRuleConditionSet bean) {
		StringBuilder sb = new StringBuilder();
		String condiType = bean.getField();
		if(AbsRuleParser.RULE_CONDI_DOSAGE.equals(condiType)) {
			//黑名单查询条件
			sb.append("ITEM_QTY:{").append(bean.getExt1()).append(" TO *}");
			ApplicationContext context = SpringContextUtils.getApplicationContext();
			IMedicalOtherDictService dictSV = context.getBean(IMedicalOtherDictService.class);
			String valueUnit = dictSV.getValueByCode("dosage_unit", bean.getExt2());
			String where = "_query_:\"%sITEMCODE:%s AND CHARGEUNIT:%s\"";
			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_CHARGE_DETAIL", "VISITID", "VISITID");
			where = String.format(where, plugin.parse(), rule.getItemCodes(), valueUnit);
			sb.append(" AND ").append(where);
		}
        return sb.toString();
	}
	
	/**
	 * 
	 * 功能描述：判断是否一次就诊限定用药量
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年4月16日 下午2:18:07</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private boolean isVisit() {
		//限定条件
		Set<String> exclude = new HashSet<String>();
		exclude.add("dosage");
    	List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", exclude);
    	for(MedicalRuleConditionSet bean : judgeList) {
			if("durationType".equals(bean.getField()) && "1time".equals(bean.getExt1())) {
				return true;
			}
		}
    	return false;
	}	
}
