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

public interface IEngineRuleService {
	void generateMedicalUnreasonableAction(TaskProject task, TaskProjectBatch batch, MedicalRuleConfig rule) throws Exception;
	
	/**
	 *
	 * 功能描述：按批次生成不合规数据
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年9月21日 上午11:07:04</p>
	 *
	 * @param batchId
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void generateUnreasonableAction(String batchId, String busiType);
	/**
	 * 
	 * 功能描述：跑批次的某个规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月10日 下午3:16:26</p>
	 *
	 * @param batchId
	 * @param ruleId
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void generateUnreasonableActionByRule(String batchId, String ruleId);
	
	/**
	 * 
	 * 功能描述：跑批次的某个规则线程
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
    void generateUnreasonableActionFailRerun(String batchId, String busiType);
    
    /**
     * 
     * 功能描述：不合规行为试算任务线程
     *
     * @author  zhangly
     * <p>创建日期 ：2021年1月15日 下午5:07:54</p>
     *
     * @param ruleId
     * @param etlSource
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    void trailActionThreadPool(String ruleId, String etlSource);
	/**
	 *
	 * 功能描述：不合规行为试算
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
	void trailAction(String ruleId, String etlSource, String datasource);
}
