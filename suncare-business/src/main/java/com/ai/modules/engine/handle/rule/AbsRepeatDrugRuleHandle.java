/**
 * AbsRepeatDrugRuleHandle.java	  V1.0   2022年11月18日 上午10:50:32
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jeecg.common.util.SpringContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ai.modules.config.entity.MedicalYbDrug;
import com.ai.modules.engine.service.IEngineActionService;
import com.ai.modules.engine.service.api.IApiDictService;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 重复用药规则计算引擎
 * @author  zhangly
 * Date: 2022年11月18日
 */
public abstract class AbsRepeatDrugRuleHandle {
	protected static final Logger logger = LoggerFactory.getLogger(AbsRepeatDrugRuleHandle.class);
	public static final String RULE_TYPE = "DRUGREPEAT";
	
	protected IEngineActionService engineActionService = SpringContextUtils.getApplicationContext().getBean(IEngineActionService.class);
	protected IApiDictService dictSV = SpringContextUtils.getApplicationContext().getBean(IApiDictService.class);

	//任务
	protected TaskProject task;
	//任务批次
	protected TaskProjectBatch batch;
	//重复用药药品集合
	protected List<MedicalYbDrug> drugList;
	
	public AbsRepeatDrugRuleHandle(TaskProject task, TaskProjectBatch batch, List<MedicalYbDrug> drugList) {
		this.task = task;
		this.batch = batch;
		this.drugList = drugList;
	}
	
	public abstract void generateUnreasonableAction() throws Exception;
	
	/**
	 * 
	 * 功能描述：同一剂型分组
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	protected Map<String, Set<String>> dosageGrouping() {
		Map<String, List<MedicalYbDrug>> drugListMap = drugList.stream().collect(Collectors.groupingBy(item-> item.getDosageCode()==null? "0" : item.getDosageCode()));
        //同一剂型药品集合
        Map<String, Set<String>> dosageMap = new HashMap<String, Set<String>>();
        for(Map.Entry<String, List<MedicalYbDrug>> entry : drugListMap.entrySet()) {
        	if(entry.getValue().size()>1) {
        		//同一剂型多种药品
        		Set<String> set = new HashSet<String>();
        		for(MedicalYbDrug drug : entry.getValue()) {
        			set.add(drug.getCode());
        		}
        		dosageMap.put(entry.getKey(), set);
        	}
        }
        return dosageMap;
	}
}
