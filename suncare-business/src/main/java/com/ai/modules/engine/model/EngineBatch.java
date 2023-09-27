/**
 * EngineProjectBatch.java	  V1.0   2022年1月12日 上午10:08:44
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

import com.ai.modules.engine.model.vo.ProjectFilterWhereVO;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

public class EngineBatch {
	//项目信息
	private TaskProject task;
	//批次信息
	private TaskProjectBatch batch;
	//项目过滤条件
    private ProjectFilterWhereVO filterVO;
    
	public TaskProject getTask() {
		return task;
	}
	public void setTask(TaskProject task) {
		this.task = task;
	}
	public TaskProjectBatch getBatch() {
		return batch;
	}
	public void setBatch(TaskProjectBatch batch) {
		this.batch = batch;
	}
	public ProjectFilterWhereVO getFilterVO() {
		return filterVO;
	}
	public void setFilterVO(ProjectFilterWhereVO filterVO) {
		this.filterVO = filterVO;
	}
}
