/**
 * IEngineTrialService.java	  V1.0   2021年8月26日 下午2:12:58
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.service;

import java.util.List;

import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.formal.entity.MedicalFormalCase;

public interface IEngineTrialService {

	/**
	 *
	 * 功能描述：试算某个模型的某个节点的记录数
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年9月1日 下午1:12:32</p>
	 *
	 * @param formalCase
	 * @param flow
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void trialCaseFlowCnt(MedicalFormalCase formalCase, List<EngineNode> flow);

	/**
	 *
	 * 功能描述：试算某个模型的每个节点的记录数
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年9月1日 下午1:11:55</p>
	 *
	 * @param caseId
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void trialFormalFlowCnt(String caseId);

	/**
	 *
	 * 功能描述：试算所有模型的每个节点记录数
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年8月31日 上午11:22:19</p>
	 *
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void trialCaseFlowCnt();

	/**
	 *
	 * 功能描述：试算所有模型的每个节点记录数
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年9月3日 上午9:21:44</p>
	 *
	 * @param probe 执行探查库或模型库{true:探查,false:模型}
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void trialCaseFlowCnt(boolean probe);

	/**
	 *
	 * 功能描述：试算某个探查模型的每个节点的记录数
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年9月1日 下午1:11:55</p>
	 *
	 * @param caseId
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void trialProbeFlowCnt(String caseId);
}
