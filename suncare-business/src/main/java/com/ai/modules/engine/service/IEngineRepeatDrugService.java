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

import java.util.List;

import com.ai.modules.config.entity.MedicalYbDrug;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 
 * 功能描述：重复用药不合规检查
 *
 * @author  zhangly
 * Date: 2020年2月5日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public interface IEngineRepeatDrugService {
	void generateMedicalUnreasonableAction(TaskProject task, TaskProjectBatch batch, List<MedicalYbDrug> ruleList) throws Exception;
	void generateMedicalUnreasonableAction(String batchId);
	
	/**
	 * 
	 * 功能描述：重跑批次中失败部分的重复用药规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年6月15日 下午2:27:17</p>
	 *
	 * @param batchId
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void generateUnreasonableActionFailRerun(String batchId);
	
	/**
	 * 
	 * 功能描述：跑批次的某个重复用药合规规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年6月15日 下午2:27:48</p>
	 *
	 * @param batchId
	 * @param ruleId
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void generateUnreasonableAction(String batchId, String ruleId);
	
	void generateUnreasonableActionByThreadPool(String batchId, String ruleId);
}
