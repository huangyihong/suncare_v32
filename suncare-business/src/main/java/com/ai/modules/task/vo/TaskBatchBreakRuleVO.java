package com.ai.modules.task.vo;


import com.ai.modules.task.entity.TaskBatchBreakRule;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TaskBatchBreakRuleVO extends TaskBatchBreakRule {

	private String code;
	private String name;
}
