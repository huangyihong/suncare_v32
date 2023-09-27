/**
 * IEngineTargetService.java	  V1.0   2020年7月23日 下午3:46:26
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service;

import java.util.Set;

import com.ai.modules.medical.entity.MedicalDrugRule;

public interface IEngineTargetService {
	/**
	 * 
	 * 功能描述：计算违规指标
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年7月23日 下午3:48:49</p>
	 *
	 * @param collection
	 * @param slave 是否使用备用服务器
	 * @param batchId
	 * @param rule
	 * @param ignoreSet 忽略规则
	 * @param trail 试算标识{true:是, false:否}
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void calculateBreakActionTarget(String collection, boolean slave, String batchId, MedicalDrugRule rule, Set<String> ignoreSet, boolean trail) throws Exception;
}