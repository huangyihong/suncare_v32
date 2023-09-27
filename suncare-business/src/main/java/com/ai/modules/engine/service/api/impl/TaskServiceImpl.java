/**
 * TaskServiceImpl.java	  V1.0   2020年12月23日 下午3:08:06
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.api.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.ai.modules.engine.service.api.IApiTaskService;
import com.ai.modules.task.entity.TaskBatchBreakRule;
import com.ai.modules.task.entity.TaskBatchBreakRuleDel;
import com.ai.modules.task.entity.TaskBatchBreakRuleLog;
import com.ai.modules.task.entity.TaskBatchStepItem;
import com.ai.modules.task.entity.TaskCommonConditionSet;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.modules.task.service.ITaskBatchBreakRuleDelService;
import com.ai.modules.task.service.ITaskBatchBreakRuleLogService;
import com.ai.modules.task.service.ITaskBatchBreakRuleService;
import com.ai.modules.task.service.ITaskBatchStepItemService;
import com.ai.modules.task.service.ITaskCommonConditionSetService;
import com.ai.modules.task.service.ITaskProjectBatchService;
import com.ai.modules.task.service.ITaskProjectService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

//@Service
public class TaskServiceImpl implements IApiTaskService {
	@Autowired
    private ITaskProjectService taskSV;
	@Autowired
	private ITaskProjectBatchService batchSV;
	@Autowired
    private ITaskBatchStepItemService stepItemService;
	@Autowired
	private ITaskBatchBreakRuleLogService logService;
	@Autowired
    private ITaskBatchBreakRuleService taskBatchRuleSV;
	@Autowired
    private ITaskBatchBreakRuleDelService taskBatchRuleDelSV;
	@Autowired
    private ITaskCommonConditionSetService conditionService;

	@Override
	public TaskProject findTaskProject(String projectId) {
		TaskProject project = taskSV.getById(projectId);
    	return project;
	}

	@Override
	public TaskProjectBatch findTaskProjectBatch(String batchId) {
		TaskProjectBatch batch = batchSV.getById(batchId);
    	return batch;
	}
	
	@Override
	public void updateTaskProjectBatch(TaskProjectBatch batch) {
		batchSV.updateById(batch);
	}
	
	@Override
	public boolean canRun(String batchId, String itemId) {
		QueryWrapper<TaskBatchStepItem> wrapper = new QueryWrapper<TaskBatchStepItem>()
                .eq("BATCH_ID", batchId)
                .eq("STEP", "1")
                .eq("ITEM_ID", itemId);
        List<TaskBatchStepItem> list = stepItemService.list(wrapper);
        if(list!=null && list.size()>0) {
        	TaskBatchStepItem step = list.get(0);
        	if(step!=null && ("running".equals(step.getStatus()) || "wait".equals(step.getStatus()))) {
        		return false;
        	}
        }
    	return true;
	}

	@Override
	public void updateTaskBatchStepItem(String batchId, String stepType, TaskBatchStepItem up) {
		QueryWrapper<TaskBatchStepItem> wrapper = new QueryWrapper<TaskBatchStepItem>()
                .eq("BATCH_ID", batchId)
                .eq("STEP", 1)
                .eq("ITEM_ID", stepType);
        stepItemService.update(up, wrapper);    	
	}
	
	@Override
	public void updateTaskBatchStepItem(String batchId, Set<String> stepTypes, TaskBatchStepItem up) {
		QueryWrapper<TaskBatchStepItem> wrapper = new QueryWrapper<TaskBatchStepItem>()
                .eq("BATCH_ID", batchId)
                .eq("STEP", 1)
                .in("ITEM_ID", stepTypes);
        stepItemService.update(up, wrapper);    	
	}
	
	@Override
	public void updateTaskBatchStepItem(String batchId, String stepType, String datasource, TaskBatchStepItem up) {
		QueryWrapper<TaskBatchStepItem> wrapper = new QueryWrapper<TaskBatchStepItem>()
                .eq("BATCH_ID", batchId)
                .eq("STEP", 1)
                .eq("ITEM_ID", stepType)
                .eq("DATA_SOURCE", datasource);
        stepItemService.update(up, wrapper);    	
	}
	
	@Override
	public void saveTaskBatchStepItem(TaskBatchStepItem step) {
		stepItemService.save(step);
	}
	
	@Override
	public void saveTaskBatchStepItem(List<TaskBatchStepItem> stepList) {
		stepItemService.saveBatch(stepList);
	}
	
	@Override
	public void removeTaskBatchStepItem(String batchId) {
		QueryWrapper<TaskBatchStepItem> wrapper = new QueryWrapper<TaskBatchStepItem>()
                .eq("BATCH_ID", batchId)
                .eq("STEP", 1);
        stepItemService.remove(wrapper);
	}
	
	@Override
	public void removeTaskBatchStepItem(String batchId, String datasource) {
		QueryWrapper<TaskBatchStepItem> wrapper = new QueryWrapper<TaskBatchStepItem>()
                .eq("BATCH_ID", batchId)
                .eq("STEP", 1)
                .eq("DATA_SOURCE", datasource);
        stepItemService.remove(wrapper);
	}
	
	@Override
	public void removeTaskBatchBreakRuleLog(String batchId, String busiType) {
		logService.remove(new QueryWrapper<TaskBatchBreakRuleLog>()
        		.eq("ITEM_TYPE", busiType)
        		.eq("BATCH_ID", batchId));
	}

	@Override
	public void removeTaskBatchBreakRuleLog(String batchId, String busiType, String itemStype) {
		QueryWrapper<TaskBatchBreakRuleLog> wrapper = new QueryWrapper<TaskBatchBreakRuleLog>()
			.eq("ITEM_TYPE", busiType)
			.eq("BATCH_ID", batchId);		
		if(StringUtils.isNotBlank(itemStype)) {
			wrapper.eq("ITEM_STYPE", itemStype);
		}
		logService.remove(wrapper);
	}

	@Override
	public void saveTaskBatchBreakRuleLog(List<TaskBatchBreakRuleLog> logList) {
		logService.saveBatch(logList);
	}
	
	@Override
	public void saveTaskBatchBreakRuleLog(TaskBatchBreakRuleLog log) {
		logService.save(log);
	}
	
	@Override
	public TaskBatchBreakRuleLog findTaskBatchBreakRuleLog(String logId) {
		QueryWrapper<TaskBatchBreakRuleLog> wrapper = new QueryWrapper<TaskBatchBreakRuleLog>()
				.eq("LOG_ID", logId).orderByDesc("CREATE_TIME");
		List<TaskBatchBreakRuleLog> list = logService.list(wrapper);
		if(list!=null && list.size()>0) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public void updateTaskBatchBreakRuleLog(String batchId, String busiType, String itemId, TaskBatchBreakRuleLog up) {
		QueryWrapper<TaskBatchBreakRuleLog> wrapper = new QueryWrapper<TaskBatchBreakRuleLog>()
	            .eq("BATCH_ID", batchId)
	            .eq("ITEM_TYPE", busiType)
	            .eq("ITEM_ID", itemId);
		logService.update(up, wrapper);
	}
	
	@Override
	public void updateTaskBatchBreakRuleLog(String batchId, String busiType, List<String> itemIds, TaskBatchBreakRuleLog up) {
		QueryWrapper<TaskBatchBreakRuleLog> wrapper = new QueryWrapper<TaskBatchBreakRuleLog>()
	            .eq("BATCH_ID", batchId)
	            .eq("ITEM_TYPE", busiType)
	            .in("ITEM_ID", itemIds);
		logService.update(up, wrapper);
	}

	@Override
	public Map<String, Integer> groupByTaskBatchBreakRuleLog(String batchId, String busiType) {
		Map<String, Integer> statusMap = logService.groupByStatus(batchId, busiType);
    	return statusMap;
	}

	@Override
	public void waitTaskBatchBreakRuleLog(String batchId, String busiType, List<String> codes) {
		logService.waitTaskBatchBreakRuleLog(batchId, busiType, codes);
	}

	@Override
	public List<TaskBatchBreakRule> findTaskBatchBreakRule(String batchId, String ruleId) {
		QueryWrapper<TaskBatchBreakRule> wrapper = new QueryWrapper<TaskBatchBreakRule>()
                .eq("BATCH_ID", batchId)
                .eq("RULE_ID", ruleId);
        List<TaskBatchBreakRule> items = taskBatchRuleSV.list(wrapper);
        return items;
	}
	
	@Override
	public List<TaskBatchBreakRule> findTaskBatchBreakRuleByStep(String batchId, String stepType) {
		QueryWrapper<TaskBatchBreakRule> wrapper = new QueryWrapper<TaskBatchBreakRule>()
                .eq("BATCH_ID", batchId)
                .eq("RULE_TYPE", stepType);
        List<TaskBatchBreakRule> items = taskBatchRuleSV.list(wrapper);
        return items;
	}

	@Override
	public List<TaskBatchBreakRuleDel> findTaskBatchBreakRuleDel(String batchId, String stepType) {
		QueryWrapper<TaskBatchBreakRuleDel> wrapper = new QueryWrapper<TaskBatchBreakRuleDel>()
                .eq("BATCH_ID", batchId)
                .eq("RULE_TYPE", stepType);
        List<TaskBatchBreakRuleDel> items = taskBatchRuleDelSV.list(wrapper);
		return items;
	}

	@Override
	public void updateTaskBatchBreakRuleDel(String batchId, String stepType, String caseId, TaskBatchBreakRuleDel up) {
		QueryWrapper<TaskBatchBreakRuleDel> wrapper = new QueryWrapper<TaskBatchBreakRuleDel>()
                .eq("BATCH_ID", batchId)
                .eq("RULE_TYPE", stepType)
                .eq("CASE_ID", caseId);
        taskBatchRuleDelSV.update(up, wrapper);
	}
	
	@Override
	public void updateTaskBatchBreakRuleDel(String batchId, Set<String> stepTypes, String caseId, TaskBatchBreakRuleDel up) {
		QueryWrapper<TaskBatchBreakRuleDel> wrapper = new QueryWrapper<TaskBatchBreakRuleDel>()
                .eq("BATCH_ID", batchId)
                .in("RULE_TYPE", stepTypes)
                .eq("CASE_ID", caseId);
        taskBatchRuleDelSV.update(up, wrapper);
	}

	@Override
	public List<TaskCommonConditionSet> queryTaskCommonConditionSet(String projectId) {
		List<TaskCommonConditionSet> list = conditionService.list(new QueryWrapper<TaskCommonConditionSet>().eq("RULE_ID", projectId));
		return list;
	}
}
