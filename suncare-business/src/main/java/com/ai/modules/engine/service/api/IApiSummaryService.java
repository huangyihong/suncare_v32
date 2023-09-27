/**
 * ISummaryService.java	  V1.0   2020年12月25日 上午9:39:13
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ai.modules.medical.entity.MedicalSpecialCaseClassify;
import com.ai.modules.task.entity.TaskActionFieldConfig;
import com.ai.modules.task.entity.TaskBatchActionFieldConfig;

public interface IApiSummaryService {
	Map<String, TaskActionFieldConfig> findTaskActionFieldConfig();
	
	Map<String, TaskActionFieldConfig> findTaskActionFieldConfig(Collection<String> actionNames);
	
	TaskActionFieldConfig findTaskActionFieldConfig(String actionName);
	
	TaskActionFieldConfig findTaskActionFieldConfigByCache(String actionName);
	
	void saveTaskBatchActionFieldConfig(TaskBatchActionFieldConfig record);
	
	void saveTaskBatchActionFieldConfig(List<TaskBatchActionFieldConfig> recordList);
	
	void removeTaskBatchActionFieldConfig(String batchId, String actionName);
	
	void removeTaskBatchActionFieldConfig(String batchId);
	
	void updateTaskBatchActionFieldConfig(String batchId, String actionName, TaskBatchActionFieldConfig up);
	
	List<MedicalSpecialCaseClassify> findMedicalSpecialCaseClassify();
}
