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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.handle.rule.parse.hive.AbsHiveRuleParser;
import com.ai.modules.engine.handle.rule.parse.hive.HiveRuleLackItemsParser;
import com.ai.modules.engine.handle.rule.parse.hive.HiveRuleMasterInfoParser;
import com.ai.modules.engine.handle.rule.parse.hive.HiveRuleParser;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 
 * 功能描述：药品使用缺少必要药品或项目
 *
 * @author  zhangly
 * Date: 2021年3月24日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HiveLackItemsRuleHandle extends HiveRuleHandle {
	
	public HiveLackItemsRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, String datasource,
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, datasource, rule, ruleConditionList);
	}
	
	/**
	 * 解析判断条件
	 */
	@Override
	protected WithTableModel parseJudgeCondition(String fromTable) {
		boolean isPatient = this.isPatient(); //是否检查病人以往历史病例
		Set<String> exclude = new HashSet<String>();
    	exclude.add("reviewHisItem");
    	List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", exclude);
    	if(judgeList==null || judgeList.size()==0) {
    		throw new RuntimeException(rule.getItemNames()+"规则未配置判断条件！");
    	}
    	List<WithTableModel> withTableList = new ArrayList<WithTableModel>();
		for(MedicalRuleConditionSet bean : judgeList) {
			WithTableModel withTable = this.parseCondition(bean, isPatient, fromTable);
			withTableList.add(withTable);
		}
		String sql = WithTableUtil.parseWithTableList(fromTable, withTableList);
		return new WithTableModel(AbsHiveRuleHandle.WITH_TABLE_JUDGE, sql);
	}

	private WithTableModel parseCondition(MedicalRuleConditionSet bean, boolean isPatient, String fromTable) {
		AbsHiveRuleParser parser = null;
		String condiType = bean.getField();
		if(AbsRuleParser.RULE_MASTER_SET.contains(condiType)) {
			//关联master_info
			parser = new HiveRuleMasterInfoParser(rule, bean, fromTable);
		} else if(AbsRuleParser.RULE_CONDI_ITEMORDRUGGROUP.equals(condiType)) {
			//药品使用缺少必要药品或项目
			HiveRuleLackItemsParser ruleParser = new HiveRuleLackItemsParser(rule, bean, fromTable);
			ruleParser.setPatient(isPatient);
			parser = ruleParser;
		} else {
			parser = new HiveRuleParser(rule, bean, fromTable);
		}
		WithTableModel model = parser.parseCondition();
		if(StringUtils.isNotBlank(bean.getLogic())) {
			model.setLogic(bean.getLogic());
		}
		return model;		
	}
}
