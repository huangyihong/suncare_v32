/**
 * ApiSummaryServiceImpl.java	  V1.0   2020年12月25日 上午10:04:11
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
import org.springframework.stereotype.Service;

import com.ai.modules.api.util.ApiOauthUtil;
import com.ai.modules.engine.service.api.IApiSummaryService;
import com.ai.modules.medical.entity.MedicalSpecialCaseClassify;
import com.ai.modules.task.entity.TaskActionFieldConfig;
import com.ai.modules.task.entity.TaskBatchActionFieldConfig;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

@Service
public class ApiSummaryServiceImpl implements IApiSummaryService {

	@Override
	public Map<String, TaskActionFieldConfig> findTaskActionFieldConfig() {
		Map<String, TaskActionFieldConfig> result = new HashMap<String, TaskActionFieldConfig>();
		List<TaskActionFieldConfig> configList = ApiOauthUtil.responseArray("/oauth/api/summary/allConfigList", null, "post", TaskActionFieldConfig.class);
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
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("actionNames", StringUtils.join(actionNames, ","));
    	List<TaskActionFieldConfig> configList = ApiOauthUtil.responseArray("/oauth/api/summary/configList", busiParams, "post", TaskActionFieldConfig.class);
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
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("actionName", actionName);
    	TaskActionFieldConfig config = ApiOauthUtil.response("/oauth/api/summary/config", busiParams, "post", TaskActionFieldConfig.class);
    	return config;
	}

	@Override
	public TaskActionFieldConfig findTaskActionFieldConfigByCache(String actionName) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("actionName", actionName);
    	TaskActionFieldConfig config = ApiOauthUtil.response("/oauth/api/summary/configByCache", busiParams, "post", TaskActionFieldConfig.class);
    	return config;
	}

	@Override
	public void saveTaskBatchActionFieldConfig(TaskBatchActionFieldConfig record) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("dataJson", JSON.toJSONString(record));
    	ApiOauthUtil.postSuccess("/oauth/api/summary/saveBatchConfig", busiParams);
	}

	@Override
	public void saveTaskBatchActionFieldConfig(List<TaskBatchActionFieldConfig> recordList) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("dataJson", JSON.toJSONString(recordList));
    	ApiOauthUtil.postSuccess("/oauth/api/summary/batchSaveBatchConfig", busiParams);
	}

	@Override
	public void removeTaskBatchActionFieldConfig(String batchId, String actionName) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	busiParams.put("actionName", actionName);
    	ApiOauthUtil.postSuccess("/oauth/api/summary/removeBatchConfigByAction", busiParams);
	}

	@Override
	public void removeTaskBatchActionFieldConfig(String batchId) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	ApiOauthUtil.postSuccess("/oauth/api/summary/removeBatchConfig", busiParams);
	}

	@Override
	public void updateTaskBatchActionFieldConfig(String batchId, String actionName, TaskBatchActionFieldConfig up) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	busiParams.put("actionName", actionName);
    	busiParams.put("dataJson", JSON.toJSONString(up));
    	ApiOauthUtil.postSuccess("/oauth/api/summary/updateBatchConfig", busiParams);
	}

	@Override
	public List<MedicalSpecialCaseClassify> findMedicalSpecialCaseClassify() {
		List<MedicalSpecialCaseClassify> classifyList = ApiOauthUtil.responseArray("/oauth/api/case/classifyList", null, "post", MedicalSpecialCaseClassify.class);
		return classifyList;
	}
}
