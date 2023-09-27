/**
 * IEngineDrugService.java	  V1.0   2020年1月2日 上午11:06:38
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.service;

import com.ai.modules.engine.model.EngineResult;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

import java.util.List;

/**
 *
 * 功能描述：不合理行为结果
 *
 * @author  zhangpeng
 * Date: 2020年2月14日
 */
public interface IEngineBehaviorService {

	EngineResult generateUnreasonableBehaviorAll(String batchId);
}
