/**
 * SolrRuleHandleFactory.java	  V1.0   2021年3月22日 下午2:50:27
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import com.ai.common.MedicalConstant;
import com.ai.modules.engine.handle.rule.hive.HiveRuleHandle;
import com.ai.modules.engine.handle.rule.solr.SolrRuleHandle;
import com.ai.modules.engine.handle.secondary.AbsRuleSecondHandle;
import com.ai.modules.engine.handle.secondary.RuleChronicHandle;
import com.ai.modules.engine.model.EngineConstant;
import com.ai.modules.engine.model.EngineHandleMapping;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.HiveJDBCUtil;

public class RuleHandleFactory {
	private TaskProject task;
	//任务批次
	private TaskProjectBatch batch;
	//是否试算
	private Boolean trail;
	//规则对象
	private MedicalRuleConfig rule;
	//规则条件
	private List<MedicalRuleConditionSet> ruleConditionList;
	
	public RuleHandleFactory(TaskProject task, TaskProjectBatch batch, boolean trail, 
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		this.task = task;
		this.batch = batch;
		this.trail = trail;
		this.rule = rule;
		this.ruleConditionList = ruleConditionList;
	}
	
	public RuleHandleFactory(TaskProject task, TaskProjectBatch batch, 
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		this(task, batch, false, rule, ruleConditionList);
	}
	
	public AbsRuleHandle build() throws Exception {
		AbsRuleHandle handle = null;
		String ruleType = rule.getRuleType();
		String ruleLimit = rule.getRuleLimit();
		boolean isSolr = !HiveJDBCUtil.enabledProcessGp(); //是否solr计算引擎
		EngineHandleMapping mapping = EngineConstant.HANDLE_MAPPING.get(ruleLimit);
		if(mapping==null) {
			//可能出现大类对应同一细类规则，计算引擎不同，如一日限频次
			String key = ruleType.concat(".").concat(ruleLimit);
			mapping = EngineConstant.HANDLE_MAPPING.get(key);
		}
		if(isSolr) {
			//solr模式
			if(mapping!=null) {
				Class<? extends AbsRuleHandle> clazz = mapping.getSolrHandleClazz();
				Constructor<?> constructor = clazz.getConstructor(new Class[] {TaskProject.class, TaskProjectBatch.class, 
						Boolean.class, MedicalRuleConfig.class, List.class});
				handle = (SolrRuleHandle) constructor.newInstance(task, batch, trail, rule, ruleConditionList);
			} else {
				handle = new SolrRuleHandle(task, batch, trail, rule, ruleConditionList);
			}
		} else {
			//impala、gp模式
			if(mapping!=null) {
				Class<? extends AbsRuleHandle> clazz = mapping.getHiveHandleClazz();
				Constructor<?> constructor = clazz.getConstructor(new Class[] {TaskProject.class, TaskProjectBatch.class, 
						Boolean.class, String.class, MedicalRuleConfig.class, List.class});
				handle = (HiveRuleHandle) constructor.newInstance(task, batch, trail, task.getDataSource(), rule, ruleConditionList);
			} else {
				handle = new HiveRuleHandle(task, batch, trail, task.getDataSource(), rule, ruleConditionList);
			}
		}
		return handle;
	}
	
	/**
	 * 
	 * 功能描述：规则二次处理程序
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年3月24日 下午3:34:18</p>
	 *
	 * @param actionTypeName
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public List<AbsRuleSecondHandle> buildRuleSecondHandle() throws Exception {
		List<AbsRuleSecondHandle> result = new ArrayList<AbsRuleSecondHandle>();
		String ruleLimit = rule.getRuleLimit();
		if(EngineConstant.SEC_HANDLE_MAPPING.containsKey(ruleLimit)) {
			Class<? extends AbsRuleSecondHandle> clazz = EngineConstant.SEC_HANDLE_MAPPING.get(ruleLimit);
			Constructor<?> constructor = clazz.getConstructor(new Class[] {TaskProject.class, TaskProjectBatch.class, 
					MedicalRuleConfig.class, List.class, Boolean.class});
			AbsRuleSecondHandle handle = (AbsRuleSecondHandle) constructor.newInstance(task, batch, rule, ruleConditionList, trail);
			result.add(handle);
    	}
		if(MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE.equals(rule.getRuleType())) {
    		//合理用药规则，排除慢性病种病人
    		AbsRuleSecondHandle handle = new RuleChronicHandle(task, batch, rule, ruleConditionList, trail);
    		result.add(handle);
    	}
		return result;
	}
}
