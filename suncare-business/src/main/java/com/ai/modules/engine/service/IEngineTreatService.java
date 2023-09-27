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

import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 *
 * 功能描述：诊疗合规检查
 *
 * @author  zhangly
 * Date: 2020年1月19日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public interface IEngineTreatService extends IEngineBaseService {

	/**
	 *
	 * 功能描述：按每个诊疗项目的所有规则计算违规数据
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年1月17日 下午2:35:33</p>
	 *
	 * @param task
	 * @param batch
	 * @param itemCode
	 * @param rule
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void generateMedicalUnreasonableTreatAction(TaskProject task, TaskProjectBatch batch, String itemCode, List<MedicalDrugRule> ruleList) throws Exception;

    void generateMedicalUnreasonableTreatActionByThreadPool(String batchId);
    
    /**
     * 
     * 功能描述：批次的某个收费规则任务放入线程池
     *
     * @author  zhangly
     * <p>创建日期 ：2020年12月1日 上午9:08:02</p>
     *
     * @param batchId
     * @param itemCode
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    void generateUnreasonableActionByThreadPool(String batchId, String itemCode);
    
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

	void trailTreatActionThreadPool(String ruleId, String etlSource);
	/**
	 *
	 * 功能描述：诊疗不合规行为试算
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年6月19日 下午2:57:20</p>
	 *
	 * @param ruleId
	 * @param etlSource etl数据源
	 * @param datasource 项目地数据源
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void trailTreatAction(String ruleId, String etlSource, String datasource);
}
