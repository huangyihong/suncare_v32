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
 * 功能描述：临床路径合规检查
 *
 * @author  zhangly
 * Date: 2020年1月19日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public interface IEngineClinicalOldService {
	
	/**
	 * 
	 * 功能描述：按每个疾病的所有规则计算违规数据
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年1月17日 下午2:35:33</p>
	 *
	 * @param task
	 * @param batch
	 * @param itemCode
	 * @param ruleList
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void generateMedicalUnreasonableClinicalAction(TaskProject task, TaskProjectBatch batch, String itemCode, List<MedicalDrugRule> ruleList) throws Exception;
	
	void generateMedicalUnreasonableClinicalAction(String batchId) throws Exception;
	
	/**
	 * 
	 * 功能描述：临床路径规则对象解析成查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年1月16日 下午3:30:14</p>
	 *
	 * @param rule
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<String> parseClinicalRuleCondition(MedicalDrugRule rule);
	
	
}
