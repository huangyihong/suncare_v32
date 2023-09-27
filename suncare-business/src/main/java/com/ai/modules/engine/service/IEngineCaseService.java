/**
 * IEngineCaseService.java	  V1.0   2020年1月16日 下午2:54:47
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.service;

import java.util.List;
import java.util.Set;

import com.ai.modules.engine.model.EngineResult;
import com.ai.modules.engine.model.dto.BatchItemDTO;

public interface IEngineCaseService extends IEngineBaseService {

	/**
	 *
	 * 功能描述：按任务批次每个模型生成不合理行为数据
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年1月17日 下午5:35:27</p>
	 *
	 * @param batchId:项目批次号
	 * @param busiId:业务组编码
	 * @param caseId:模型编码
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void generateMedicalUnreasonableAction(String batchId, String caseId) throws Exception;

    EngineResult generateUnreasonableDrugAction(String batchId, String ruleType, String itemCode);
    
    EngineResult generateUnreasonableRuleAction(List<BatchItemDTO> itemList);

    /**
	 *
	 * 功能描述：按任务批次生成不合理行为、药品不合规、收费不合规、临床路径不合规等数据
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年1月7日 上午10:25:57</p>
	 *
	 * @param batchId
	 * @param ruleTypeSet
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	EngineResult generateUnreasonableActionAll(String batchId, Set<String> ruleTypeSet);

	void generateUnreasonableActionByBatch(String batchId, Set<String> ruleTypeSet);

	void generateMedicalUnreasonableActionByThreadPool(String batchId, String busiId, String caseId);

	void initStep(String batchId, Set<String> ruleTypeSet);
}
