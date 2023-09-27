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
import com.ai.modules.engine.model.rule.AbsEngineParamRule;
import com.ai.modules.engine.model.rule.hive.HiveParamIndicationRule;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 门慢适应症规则
 * @author  zhangly
 * Date: 2022年11月15日
 */
public class HiveChronicIndicationRuleHandle extends HiveRuleHandle {
	
	public HiveChronicIndicationRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, String datasource,
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, datasource, rule, ruleConditionList);
	}

	@Override
	protected String masterInfoJoinDwsChargeSql(boolean impala) throws Exception {
		String sql = super.masterInfoJoinDwsChargeSql(impala);
		//追加查询条件限定就诊类型为慢性病或特殊病门诊
		sql = sql + " and x.VISITTYPE_ID in('MZ01.01.12','MZ01.01.15')";
		return sql;
	}
	
	@Override
	protected WithTableModel parseJudgeCondition(String fromTable) {
		//限定条件
    	List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", null);
    	if(judgeList==null || judgeList.size()==0) {
    		throw new RuntimeException(rule.getItemNames()+"规则未配置判断条件！");
    	}
    	List<WithTableModel> withTableList = new ArrayList<WithTableModel>();
		for(MedicalRuleConditionSet bean : judgeList) {
			WithTableModel withTable = this.parseCondition(bean, fromTable);
			if(withTable!=null) {
				withTableList.add(withTable);
			}
		}
		//慢病病种病人
		withTableList.add(this.filterChronicCondition(judgeList, fromTable));
		String sql = WithTableUtil.parseWithTableList(fromTable, withTableList);
		return new WithTableModel(AbsHiveRuleHandle.WITH_TABLE_JUDGE, sql);
	}
	
	private WithTableModel parseCondition(MedicalRuleConditionSet bean, String fromTable) {
		AbsEngineParamRule paramRule = null;
		String condiType = bean.getField();
		if(AbsRuleParser.RULE_CONDI_DISEASEGRP.equals(condiType)) {
			//疾病组
			paramRule = new HiveParamIndicationRule("DIAGGROUP_CODE", bean.getExt1(), fromTable);
			paramRule.setPatient(true);
		} else {
			throw new RuntimeException("未找到判断条件类型！");
		}
        String sql = paramRule.where();
        return new WithTableModel(WithTableUtil.buildWithTable(bean), sql);
	}
	
	/**
	 * 
	 * 功能描述：过滤掉慢病病种病人
	 *
	 * @author  zhangly
	 *
	 * @param judgeList
	 * @return
	 */
	private WithTableModel filterChronicCondition(List<MedicalRuleConditionSet> judgeList, String fromTable) {
		Set<String> codeSet = new HashSet<String>();
		for (MedicalRuleConditionSet bean : judgeList) {
			if (StringUtils.isNotBlank(bean.getExt1())) {
				String code = bean.getExt1();
				code = StringUtils.replace(code, ",", "|");
				codeSet.add(code);
			}			
		}
		String codes = "('"+StringUtils.join(codeSet, "','")+"')";
		StringBuilder sb = new StringBuilder();
		sb.append("select * from ").append(fromTable);
		sb.append(" where ");
		sb.append(fromTable).append(".clientid in(select clientid from DWB_CHRONIC_PATIENT x1 join STD_DIAGGROUP x2 on x1.CHRONICDIS_CODE=x2.DISEASECODE");
		sb.append(" where x2.DIAGGROUP_CODE in").append(codes);
		sb.append(" or x1.CHRONICDIS_CODE in").append(codes);
		sb.append(")");
		String alias = AbsHiveRuleHandle.WITH_TABLE_JUDGE.concat("_chronic");
		return new WithTableModel(alias, sb.toString(), "or");
	}
}
