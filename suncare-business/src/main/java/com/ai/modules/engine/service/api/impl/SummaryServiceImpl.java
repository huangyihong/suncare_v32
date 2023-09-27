/**
 * SummaryServiceImpl.java	  V1.0   2020年12月25日 上午9:39:32
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.api.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.modules.engine.service.api.IApiSummaryService;
import com.ai.modules.medical.entity.MedicalSpecialCaseClassify;
import com.ai.modules.medical.service.IMedicalSpecialCaseClassifyService;
import com.ai.modules.task.entity.TaskActionFieldConfig;
import com.ai.modules.task.entity.TaskBatchActionFieldConfig;
import com.ai.modules.task.service.ITaskActionFieldConfigService;
import com.ai.modules.task.service.ITaskBatchActionFieldConfigService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

//@Service
public class SummaryServiceImpl implements IApiSummaryService {
	@Autowired
	private ITaskActionFieldConfigService configSV;
	@Autowired
	private ITaskBatchActionFieldConfigService batchConfigSV;
	@Autowired
	private ITaskActionFieldConfigService dynamicConfigSV;
	@Autowired
	private IMedicalSpecialCaseClassifyService classifySV;
	
	@Override
	public Map<String, TaskActionFieldConfig> findTaskActionFieldConfig() {
		Map<String, TaskActionFieldConfig> result = new HashMap<String, TaskActionFieldConfig>();
		List<TaskActionFieldConfig> configList = configSV.list();
		for(TaskActionFieldConfig config : configList) {
			if(StringUtils.isNotBlank(config.getGroupFields())) {
				JSONArray jsonArray = JSON.parseArray(config.getGroupFields());
	        	if(jsonArray!=null && jsonArray.size()>0) {
	        		result.put(config.getActionName(), config);
	        	}
			}		
		}
		return result;
	}
	
	@Override
	public Map<String, TaskActionFieldConfig> findTaskActionFieldConfig(Collection<String> actionNames) {
		Map<String, TaskActionFieldConfig> result = new HashMap<String, TaskActionFieldConfig>();
		List<TaskActionFieldConfig> configList = configSV.list(new QueryWrapper<TaskActionFieldConfig>().in("ACTION_NAME", actionNames));
		for(TaskActionFieldConfig config : configList) {
			if(StringUtils.isNotBlank(config.getGroupFields())) {
				JSONArray jsonArray = JSON.parseArray(config.getGroupFields());
	        	if(jsonArray!=null && jsonArray.size()>0) {
	        		result.put(config.getActionName(), config);
	        	}
			}
		}
		return result;
	}
	
	@Override
	public TaskActionFieldConfig findTaskActionFieldConfig(String actionName) {
		TaskActionFieldConfig config = configSV.getOne(new QueryWrapper<TaskActionFieldConfig>().eq("ACTION_NAME", actionName));
		return config;
	}
	
	@Override
	public TaskActionFieldConfig findTaskActionFieldConfigByCache(String actionName) {
		TaskActionFieldConfig config = dynamicConfigSV.queryTaskActionFieldConfigByCache(actionName);
		return config;
	}

	@Override
	public void saveTaskBatchActionFieldConfig(TaskBatchActionFieldConfig record) {
		batchConfigSV.save(record);
	}
	
	@Override
	public void saveTaskBatchActionFieldConfig(List<TaskBatchActionFieldConfig> recordList) {
		batchConfigSV.saveBatch(recordList);
	}

	@Override
	public void removeTaskBatchActionFieldConfig(String batchId, String actionName) {
		batchConfigSV.remove(new QueryWrapper<TaskBatchActionFieldConfig>().eq("ACTION_NAME", actionName).eq("BATCH_ID", batchId));
	}
	
	@Override
	public void removeTaskBatchActionFieldConfig(String batchId) {
		batchConfigSV.remove(new QueryWrapper<TaskBatchActionFieldConfig>().eq("BATCH_ID", batchId));
	}

	@Override
	public void updateTaskBatchActionFieldConfig(String batchId, String actionName, TaskBatchActionFieldConfig up) {
		batchConfigSV.update(up, new QueryWrapper<TaskBatchActionFieldConfig>().eq("ACTION_NAME", actionName).eq("BATCH_ID", batchId));
	}

	@Override
	public List<MedicalSpecialCaseClassify> findMedicalSpecialCaseClassify() {
		List<MedicalSpecialCaseClassify> classifyList = classifySV.list();
		return classifyList;
	}
}
