/**
 * IEngineDrugUseService.java	  V1.0   2020年11月10日 上午10:52:35
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service;

import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

public interface IEngineDrugUseService extends IEngineBaseService {
	void generateMedicalUnreasonableDrugAction(TaskProject task, TaskProjectBatch batch, MedicalRuleConfig rule) throws Exception;
	
	/**
	 * 
	 * 功能描述：跑批次的某个用药合规规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月10日 下午3:16:26</p>
	 *
	 * @param batchId
	 * @param ruleId
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void generateUnreasonableAction(String batchId, String ruleId);
	
	/**
	 * 
	 * 功能描述：跑批次的某个用药合规规则线程
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月19日 上午11:37:55</p>
	 *
	 * @param batchId
	 * @param ruleId
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void generateUnreasonableActionByThreadPool(String batchId, String ruleId);
	
	/**
     * 
     * 功能描述：失败项目重跑
     *
     * @author  zhangly
     * <p>创建日期 ：2020年12月1日 上午9:37:30</p>
     *
     * @param batchId
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    void generateUnreasonableActionFailRerun(String batchId);
}
