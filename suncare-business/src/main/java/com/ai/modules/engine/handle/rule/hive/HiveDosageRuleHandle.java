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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.modules.config.service.IMedicalOtherDictService;
import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.hive.model.JudgeWithTableScript;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 医保限定用药量规则
 * @author  zhangly
 * Date: 2022年11月15日
 */
public class HiveDosageRuleHandle extends HiveRuleHandle {
	
	public HiveDosageRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, String datasource,
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, datasource, rule, ruleConditionList);
	}

	@Override
	public void generateUnreasonableAction() throws Exception {
		boolean isVisit = this.isVisit();
		if(isVisit) {
			super.generateUnreasonableAction();
		}
	}
	
	@Override
	protected JudgeWithTableScript judgeWithTableScript(String fromTable) {
		StringBuilder sql = new StringBuilder();
		//筛选后的结果表已经是黑名单
		WithTableModel judgeTable = this.parseJudgeCondition(fromTable);
		if(judgeTable!=null) {
			sql.append(judgeTable.getAlias()).append(" as (").append(judgeTable.getSql()).append(")");
			return new JudgeWithTableScript(sql.toString(), judgeTable.getAlias());
		}
		return null;
	}
	
	@Override
	protected WithTableModel parseJudgeCondition(String fromTable) {
		//限定条件
		Set<String> exclude = new HashSet<String>();
		exclude.add("durationType");
    	List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", exclude);
    	if(judgeList==null || judgeList.size()==0) {
    		throw new RuntimeException(rule.getItemNames()+"规则未配置判断条件！");
    	}
    	MedicalRuleConditionSet bean = judgeList.get(0);
		return parseCondition(bean, fromTable);
	}
	
	private WithTableModel parseCondition(MedicalRuleConditionSet bean, String fromTable) {
		String condiType = bean.getField();
		if(AbsRuleParser.RULE_CONDI_DOSAGE.equals(condiType)) {
			//黑名单查询条件
			int limit = Integer.parseInt(bean.getExt1());
			ApplicationContext context = SpringContextUtils.getApplicationContext();
			IMedicalOtherDictService dictSV = context.getBean(IMedicalOtherDictService.class);
			String valueUnit = dictSV.getValueByCode("dosage_unit", bean.getExt2());
			StringBuilder sb = new StringBuilder();
			sb.append("select *");
			sb.append(",ITEM_QTY-").append(limit).append(" AI_OUT_CNT");
			sb.append(",(ITEM_QTY-").append(limit).append(")*ITEMPRICE_MAX MIN_MONEY");
			sb.append(",(ITEM_QTY-").append(limit).append(")*ITEMPRICE_MAX MAX_MONEY");
			sb.append(",(FUND_COVER/ITEM_QTY)*(ITEM_QTY-").append(limit).append(") ACTION_MONEY");
			sb.append(" from ").append(fromTable);
			sb.append(" where exists(select 1 from DWB_CHARGE_DETAIL x1");
			sb.append(" where ").append(fromTable).append(".visitid=x1.visitid");
			sb.append(" and ").append(fromTable).append(".itemcode=x1.itemcode");
			sb.append(" and x1.itemcode='").append(rule.getItemCodes()).append("'");
			sb.append(" and x1.CHARGEUNIT='").append(valueUnit).append("'");
			sb.append(")");
			sb.append(" and ITEM_QTY>").append(limit);
			return new WithTableModel(AbsHiveRuleHandle.WITH_TABLE_JUDGE, sb.toString());
		}
        return null;
	}
	
	@Override
	protected Map<String, String> parseUdfFieldMap() throws Exception {
		Map<String, String> fieldMap = super.parseUdfFieldMap();
		//重新设置违规金额、违规基金支出金额等字段取值
		fieldMap.put("AI_OUT_CNT", "y.AI_OUT_CNT");
		fieldMap.put("MIN_MONEY", "y.MIN_MONEY");
		fieldMap.put("MAX_MONEY", "y.MAX_MONEY");
		fieldMap.put("ACTION_MONEY", "y.ACTION_MONEY");
		fieldMap.put("MAX_ACTION_MONEY", "y.ACTION_MONEY");
		return fieldMap;
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
