/**
 * ProjectFilterWhereVO.java	  V1.0   2021年6月2日 下午3:55:37
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.vo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ai.modules.task.entity.TaskCommonConditionSet;

import lombok.Data;

@Data
public class ProjectFilterWhereVO {
	private List<TaskCommonConditionSet> whereList;
	private Set<String> typeSet;
	/**
	 * 是否疾病映射不全过滤
	 */
	private boolean diseaseFilter = true;
	private String condition;
	
	public void addType(String type) {
		if(typeSet==null) {
			typeSet = new HashSet<String>();
		}
		typeSet.add(type);
	}
}
