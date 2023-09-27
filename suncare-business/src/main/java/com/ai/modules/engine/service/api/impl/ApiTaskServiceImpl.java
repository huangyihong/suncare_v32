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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.ai.modules.api.rsp.ApiResponse;
import com.ai.modules.api.util.ApiOauthUtil;
import com.ai.modules.engine.service.api.IApiTaskService;
import com.ai.modules.task.entity.TaskBatchBreakRule;
import com.ai.modules.task.entity.TaskBatchBreakRuleDel;
import com.ai.modules.task.entity.TaskBatchBreakRuleLog;
import com.ai.modules.task.entity.TaskBatchStepItem;
import com.ai.modules.task.entity.TaskCommonConditionSet;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Service
public class ApiTaskServiceImpl implements IApiTaskService {

	@Override
	public TaskProject findTaskProject(String projectId) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("projectId", projectId);
    	TaskProject project = ApiOauthUtil.response("/oauth/api/task/project", busiParams, "post", TaskProject.class);
    	return project;
	}

	@Override
	public TaskProjectBatch findTaskProjectBatch(String batchId) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	TaskProjectBatch batch = ApiOauthUtil.response("/oauth/api/task/batch", busiParams, "post", TaskProjectBatch.class);
    	return batch;
	}
	
	@Override
	public void updateTaskProjectBatch(TaskProjectBatch batch) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("dataJson", JSON.toJSONString(batch));
    	ApiOauthUtil.postSuccess("/oauth/api/task/batch/update", busiParams);
	}
	
	@Override
	public boolean canRun(String batchId, String itemId) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	busiParams.put("itemId", itemId);
    	TaskBatchStepItem step = ApiOauthUtil.response("/oauth/api/task/step/get", busiParams, "post", TaskBatchStepItem.class);
    	if(step!=null && ("running".equals(step.getStatus()) || "wait".equals(step.getStatus()))) {
    		return false;
    	}
    	return true;
	}

	@Override
	public void updateTaskBatchStepItem(String batchId, String stepType, TaskBatchStepItem up) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	busiParams.put("step", "1");
    	busiParams.put("stepType", stepType);
    	busiParams.put("dataJson", JSON.toJSONString(up));
    	ApiOauthUtil.postSuccess("/oauth/api/task/step/update", busiParams);    	
	}
	
	@Override
	public void updateTaskBatchStepItem(String batchId, Set<String> stepTypes, TaskBatchStepItem up) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	busiParams.put("step", "1");
    	busiParams.put("stepType", StringUtils.join(stepTypes, ","));
    	busiParams.put("dataJson", JSON.toJSONString(up));
    	ApiOauthUtil.postSuccess("/oauth/api/task/step/update", busiParams);    	
	}
	
	@Override
	public void updateTaskBatchStepItem(String batchId, String stepType, String datasource, TaskBatchStepItem up) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	busiParams.put("step", "1");
    	busiParams.put("stepType", stepType);
    	busiParams.put("datasource", datasource);
    	busiParams.put("dataJson", JSON.toJSONString(up));
    	ApiOauthUtil.postSuccess("/oauth/api/task/step/update", busiParams);    	
	}
	
	@Override
	public void saveTaskBatchStepItem(TaskBatchStepItem step) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("dataJson", JSON.toJSONString(step));
    	ApiOauthUtil.postSuccess("/oauth/api/task/step/save", busiParams);
	}
	
	@Override
	public void saveTaskBatchStepItem(List<TaskBatchStepItem> stepList) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("dataJson", JSON.toJSONString(stepList));
    	ApiOauthUtil.postSuccess("/oauth/api/task/step/batchSave", busiParams);
	}
	
	@Override
	public void removeTaskBatchStepItem(String batchId) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	ApiOauthUtil.postSuccess("/oauth/api/task/step/remove", busiParams);
	}
	
	@Override
	public void removeTaskBatchStepItem(String batchId, String datasource) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	busiParams.put("datasource", datasource);
    	ApiOauthUtil.postSuccess("/oauth/api/task/step/removeByDs", busiParams);
	}
	
	@Override
	public void removeTaskBatchBreakRuleLog(String batchId, String busiType) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	busiParams.put("busiType", busiType);
    	ApiOauthUtil.postSuccess("/oauth/api/task/batch/log/remove", busiParams);
	}

	@Override
	public void removeTaskBatchBreakRuleLog(String batchId, String busiType, String itemStype) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	busiParams.put("busiType", busiType);
    	if(StringUtils.isNotBlank(itemStype)) {
    		busiParams.put("itemStype", itemStype);
    	}
    	ApiOauthUtil.postSuccess("/oauth/api/task/batch/log/remove", busiParams);
	}

	@Override
	public void saveTaskBatchBreakRuleLog(List<TaskBatchBreakRuleLog> logList) {
		int size = logList.size();
        int pageSize = 1000;
		int pageNum = (size + pageSize - 1) / pageSize;
		//数据分割
		List<List<TaskBatchBreakRuleLog>> mglist = new ArrayList<List<TaskBatchBreakRuleLog>>();
	    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
	    	mglist.add(logList.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toList()));
	    });
		Map<String, String> busiParams = new HashMap<String, String>();
    	for(List<TaskBatchBreakRuleLog> sublist : mglist) {
    		busiParams.clear();
    		busiParams.put("dataJson", JSON.toJSONString(sublist));
        	ApiOauthUtil.postSuccess("/oauth/api/task/batch/log/save", busiParams);
    	}
	}
	
	@Override
	public void saveTaskBatchBreakRuleLog(TaskBatchBreakRuleLog log) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("dataJson", JSON.toJSONString(log));
    	ApiOauthUtil.postSuccess("/oauth/api/task/batch/log/add", busiParams);
	}
	
	@Override
	public TaskBatchBreakRuleLog findTaskBatchBreakRuleLog(String logId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("logId", logId);
		TaskBatchBreakRuleLog log = ApiOauthUtil.response("/oauth/api/task/batch/log/find", busiParams, "post", TaskBatchBreakRuleLog.class);
		return log;
	}

	@Override
	public void updateTaskBatchBreakRuleLog(String batchId, String busiType, String itemId, TaskBatchBreakRuleLog up) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	busiParams.put("busiType", busiType);
    	busiParams.put("itemId", itemId);
    	busiParams.put("dataJson", JSON.toJSONString(up));
    	ApiOauthUtil.postSuccess("/oauth/api/task/batch/log/update", busiParams);
	}
	
	@Override
	public void updateTaskBatchBreakRuleLog(String batchId, String busiType, List<String> itemIds, TaskBatchBreakRuleLog up) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	busiParams.put("busiType", busiType);
    	busiParams.put("itemIds", StringUtils.join(itemIds, ","));
    	busiParams.put("dataJson", JSON.toJSONString(up));
    	ApiOauthUtil.postSuccess("/oauth/api/task/batch/log/updateByItemids", busiParams);
	}

	@Override
	public Map<String, Integer> groupByTaskBatchBreakRuleLog(String batchId, String busiType) {
		Map<String, Integer> statusMap = new HashMap<String, Integer>();
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	busiParams.put("busiType", busiType);
    	ApiResponse<?> rsp = ApiOauthUtil.response("/oauth/api/task/batch/log/groupByStatus", busiParams);
		if(!rsp.isSuccess()) {
			throw new RuntimeException(rsp.getMessage());
		} else {
			if(rsp.getResult()!=null) {
				String text = rsp.getResult().toString();				
				JSONObject jsonObject = JSON.parseObject(text);
				for(Map.Entry<String, Object> entry : jsonObject.entrySet()) {
					statusMap.put(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
				}
			}
		}
    	return statusMap;
	}

	@Override
	public void waitTaskBatchBreakRuleLog(String batchId, String busiType, List<String> codes) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	busiParams.put("busiType", busiType);
    	busiParams.put("codes", StringUtils.join(codes, ","));
    	ApiOauthUtil.postSuccess("/oauth/api/task/batch/log/wait", busiParams);
	}

	@Override
	public List<TaskBatchBreakRule> findTaskBatchBreakRule(String batchId, String ruleId) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	busiParams.put("ruleId", ruleId);
    	List<TaskBatchBreakRule> result = ApiOauthUtil.responseArray("/oauth/api/task/batch/rule", busiParams, "post", TaskBatchBreakRule.class);
    	return result;
	}
	
	@Override
	public List<TaskBatchBreakRule> findTaskBatchBreakRuleByStep(String batchId, String stepType) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	busiParams.put("stepType", stepType);
    	List<TaskBatchBreakRule> result = ApiOauthUtil.responseArray("/oauth/api/task/batch/ruleByStep", busiParams, "post", TaskBatchBreakRule.class);
    	return result;
	}

	@Override
	public List<TaskBatchBreakRuleDel> findTaskBatchBreakRuleDel(String batchId, String stepType) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	busiParams.put("stepType", stepType);
    	List<TaskBatchBreakRuleDel> result = ApiOauthUtil.responseArray("/oauth/api/task/batch/rule/progress", busiParams, "post", TaskBatchBreakRuleDel.class);
    	return result;
	}

	@Override
	public void updateTaskBatchBreakRuleDel(String batchId, String stepType, String caseId, TaskBatchBreakRuleDel up) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	busiParams.put("stepType", stepType);
    	busiParams.put("caseId", caseId);
    	busiParams.put("dataJson", JSON.toJSONString(up));
    	ApiOauthUtil.postSuccess("/oauth/api/task/batch/rule/updateProgress", busiParams);
	}
	
	@Override
	public void updateTaskBatchBreakRuleDel(String batchId, Set<String> stepTypes, String caseId, TaskBatchBreakRuleDel up) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	busiParams.put("stepType", StringUtils.join(stepTypes, ","));
    	busiParams.put("caseId", caseId);
    	busiParams.put("dataJson", JSON.toJSONString(up));
    	ApiOauthUtil.postSuccess("/oauth/api/task/batch/rule/updateProgress", busiParams);
	}

	@Override
	public List<TaskCommonConditionSet> queryTaskCommonConditionSet(String projectId) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("projectId", projectId);
    	List<TaskCommonConditionSet> result = ApiOauthUtil.responseArray("/oauth/api/task/project/conditionList", busiParams, "post", TaskCommonConditionSet.class);
    	return result;
	}
}
