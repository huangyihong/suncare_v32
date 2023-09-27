/**
 * SolrRuleHandleFactory.java	  V1.0   2021年3月22日 下午2:50:27
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.solr;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ai.common.MedicalConstant;
import com.ai.modules.engine.handle.rule.AbsRuleHandle;
import com.ai.modules.engine.handle.rule.AbsSolrRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HiveOnedayMutexRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HiveRuleHandle;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.handle.secondary.AbsRuleSecondHandle;
import com.ai.modules.engine.handle.secondary.RuleChronicHandle;
import com.ai.modules.engine.handle.secondary.RuleDosageHandle;
import com.ai.modules.engine.handle.secondary.RuleItemNomatchHandle;
import com.ai.modules.engine.handle.secondary.RuleMutexUnpayHandle;
import com.ai.modules.engine.handle.secondary.RuleOnedayMutex2Handle;
import com.ai.modules.engine.handle.secondary.RuleOnedayRelyHandle;
import com.ai.modules.engine.handle.secondary.RuleOverFrequency;
import com.ai.modules.engine.handle.secondary.RuleOverFrequencyFromDetail;
import com.ai.modules.engine.handle.secondary.RuleUnindicationHandle;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

public class SolrRuleHandleFactory {
	private TaskProject task;
	//任务批次
	private TaskProjectBatch batch;
	//是否试算
	private Boolean trail;
	//规则对象
	private MedicalRuleConfig rule;
	//规则条件
	private List<MedicalRuleConditionSet> ruleConditionList;
	
	//规则引擎处理类映射
	private static final Map<String, Class<? extends AbsRuleHandle>> HANDLE_MAPPING = new HashMap<String, Class<? extends AbsRuleHandle>>();
	//规则引擎二次处理类映射
	private static final Map<String, Class<? extends AbsRuleSecondHandle>> SEC_HANDLE_MAPPING = new HashMap<String, Class<? extends AbsRuleSecondHandle>>();
	static {
		//日均限频次规则
		HANDLE_MAPPING.put("freq3", SolrOverAvgdayFreqRuleHandle.class);
		//收费合规-一日限频次
		HANDLE_MAPPING.put("freq2", SolrOverFreqRuleHandle.class);
		//收费合规-一日互斥规则
		HANDLE_MAPPING.put("dayUnfitGroups1", SolrOnedayMutexRuleHandle.class);
		//合理诊疗-一日互斥规则
		HANDLE_MAPPING.put("YRCFSF1", SolrOnedayMutexRuleHandle.class);
		//一次就诊互斥规则
		HANDLE_MAPPING.put("unfitGroups1", SolrMutexRuleHandle.class);
		//必要前提条件规则
		HANDLE_MAPPING.put("fitGroups1", SolrPreconditionRuleHandle.class);
		//适应症
		HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_INDICATION, SolrIndicationRuleHandle.class);
		HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_INDICATION1, SolrIndicationRuleHandle.class);
		//门慢适应症
		HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_CHRONICINDICATION, SolrChronicIndicationRuleHandle.class);
		//禁忌症
		HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_UNINDICATION, SolrIndicationRuleHandle.class);
		//项目与既往项目不符
		HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_ITEMWRONG, SolrItemNomatchRuleHandle.class);
		//疾病与既往项目不符
		HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_DIAGWRONG, SolrItemNomatchRuleHandle.class);
		//医保限定卫生机构类别用药
		HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_HOSPLEVELTYPE, SolrHospLvlAndTypeRuleHandle.class);
		//医保限定用药时限
		HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_DRUGDURATION, SolrDrugDurationRuleHandle.class);
		//医保限定用药量
		HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_DOSAGE, SolrDosageRuleHandle.class);
		//医保药品超过最大持续使用时间
		HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_PAYDURATION, SolrPayDurationRuleHandle.class);
		//药品使用缺少必要药品或项目
		HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_LACKITEMS, SolrLackItemsRuleHandle.class);
		//限特定人群
		HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_XTDRQ, SolrXtdrqRuleHandle.class);
		
		//以下是二次处理映射关系
		//限频次规则
		for(String key : AbsRuleHandle.FREQUENCY_ACTION_SET) {
			SEC_HANDLE_MAPPING.put(key, RuleOverFrequency.class);
		}
		//一日限频次规则
		SEC_HANDLE_MAPPING.put("freq2", RuleOverFrequencyFromDetail.class);
		//收费合规-一日互斥规则
		SEC_HANDLE_MAPPING.put("dayUnfitGroups1", RuleOnedayMutex2Handle.class);
		//合理诊疗-一日互斥规则
		SEC_HANDLE_MAPPING.put("YRCFSF1", RuleOnedayMutex2Handle.class);
		//必要前提条件规则-一日依赖项目组规则
		SEC_HANDLE_MAPPING.put("fitGroups1", RuleOnedayRelyHandle.class);
		//禁忌症规则
		SEC_HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_UNINDICATION, RuleUnindicationHandle.class);
		//项目与既往项目不符规则
		SEC_HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_ITEMWRONG, RuleItemNomatchHandle.class);
		//疾病与既往项目不符规则
		SEC_HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_DIAGWRONG, RuleItemNomatchHandle.class);
		//医保合用不予支付药品
		SEC_HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_UNPAYDRUG, RuleMutexUnpayHandle.class);
		//医保限定用药量
		SEC_HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_DOSAGE, RuleDosageHandle.class);
	}
	
	public SolrRuleHandleFactory(TaskProject task, TaskProjectBatch batch, boolean trail, 
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		this.task = task;
		this.batch = batch;
		this.trail = trail;
		this.rule = rule;
		this.ruleConditionList = ruleConditionList;
	}
	
	public SolrRuleHandleFactory(TaskProject task, TaskProjectBatch batch, 
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		this(task, batch, false, rule, ruleConditionList);
	}
	
	public AbsRuleHandle build() throws Exception {
		AbsRuleHandle handle = null;
		String ruleType = rule.getRuleType();
		String ruleLimit = rule.getRuleLimit();
		if("TREAT".equals(ruleType) && "freq2".equals(ruleLimit)) {
			//合理诊疗一日就诊限频次规则
			return new SolrTreatOverFreqRuleHandle(task, batch, trail, rule, ruleConditionList);
		}
		if(HANDLE_MAPPING.containsKey(ruleLimit)) {
			Class<? extends AbsRuleHandle> clazz = HANDLE_MAPPING.get(ruleLimit);			
			if(AbsSolrRuleHandle.class.isAssignableFrom(clazz)) {
				//solr模式
				Constructor<?> constructor = clazz.getConstructor(new Class[] {TaskProject.class, TaskProjectBatch.class, 
						Boolean.class, MedicalRuleConfig.class, List.class});
				handle = (SolrRuleHandle) constructor.newInstance(task, batch, trail, rule, ruleConditionList);
			} else {
				//hive模式
				Constructor<?> constructor = clazz.getConstructor(new Class[] {TaskProject.class, TaskProjectBatch.class, 
						Boolean.class, String.class, MedicalRuleConfig.class, List.class});
				handle = (HiveRuleHandle) constructor.newInstance(task, batch, trail, task.getDataSource(), rule, ruleConditionList);
			}
		} else {
			handle = new SolrRuleHandle(task, batch, trail, rule, ruleConditionList);
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
		String ruleType = rule.getRuleType();
		String ruleLimit = rule.getRuleLimit();
		if("TREAT".equals(ruleType) && "freq2".equals(ruleLimit)) {
			//合理诊疗一日就诊限频次规则
			/*AbsRuleSecondHandle handle = new TreatRuleOverFreq(task, batch, rule, ruleConditionList, trail);
			result.add(handle);*/
		} else if(SEC_HANDLE_MAPPING.containsKey(ruleLimit)) {
			Class<? extends AbsRuleSecondHandle> clazz = SEC_HANDLE_MAPPING.get(ruleLimit);
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
