/**
 * TaskBatchBreakRuleLogVO.java	  V1.0   2021年4月28日 上午10:24:51
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.task.vo;

import org.jeecg.common.aspect.annotation.AutoResultMap;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecg.common.util.dbencrypt.EncryptTypeHandler;

import com.ai.modules.task.entity.TaskBatchBreakRuleLog;
import com.baomidou.mybatisplus.annotation.TableField;

import lombok.Data;

@Data
@AutoResultMap
public class TaskBatchBreakRuleLogVO extends TaskBatchBreakRuleLog {
	private String ruleType;
	private String ruleSource;
	@TableField(typeHandler = EncryptTypeHandler.class)
	private String ruleBasis;
	private String ruleBasisType;
	@TableField(typeHandler = EncryptTypeHandler.class)
	private String ruleRemark;
	private String actionId;
	private String actionType;
	private String actionName;
	private String ruleSourceCode;
	private String ruleLimit;
}
