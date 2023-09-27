/**
 * ActionDTO.java	  V1.0   2020年12月28日 上午9:00:01
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.dto;

import com.ai.modules.task.entity.TaskActionFieldConfig;

public class ActionDTO {
	private String actionTypeId;
	private String actionTypeName;
	private String actionId;
	private String actionName;
	private TaskActionFieldConfig dynamicActionConfig;
	
	public String getActionTypeId() {
		return actionTypeId;
	}
	public void setActionTypeId(String actionTypeId) {
		this.actionTypeId = actionTypeId;
	}
	public String getActionTypeName() {
		return actionTypeName;
	}
	public void setActionTypeName(String actionTypeName) {
		this.actionTypeName = actionTypeName;
	}
	public String getActionId() {
		return actionId;
	}
	public void setActionId(String actionId) {
		this.actionId = actionId;
	}
	public String getActionName() {
		return actionName;
	}
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
	public TaskActionFieldConfig getDynamicActionConfig() {
		return dynamicActionConfig;
	}
	public void setDynamicActionConfig(TaskActionFieldConfig dynamicActionConfig) {
		this.dynamicActionConfig = dynamicActionConfig;
	}	
}
