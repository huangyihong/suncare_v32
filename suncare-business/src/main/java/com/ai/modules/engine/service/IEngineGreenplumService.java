/**
 * IEngineGreenplumService.java	  V1.0   2022年12月15日 上午9:11:05
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service;

public interface IEngineGreenplumService {
	
	/**
	 * 
	 * 功能描述：备份批次审核结果并删除批次计算结果
	 *
	 * @author  zhangly
	 *
	 * @param batchId
	 * @param busiType
	 */
	void remove(String batchId, String busiType);
	
	/**
	 * 
	 * 功能描述：备份批次审核结果并删除批次计算结果
	 *
	 * @author  zhangly
	 *
	 * @param batchId
	 * @param caseId
	 */
	void removeCase(String batchId, String caseId);
	
	/**
	 * 
	 * 功能描述：备份批次审核结果并删除批次计算结果
	 *
	 * @author  zhangly
	 *
	 * @param batchId
	 * @param ruleId
	 */
	void removeRule(String batchId, String ruleId);
	
	/**
	 * 
	 * 功能描述：回填批次计算结果审核状态
	 *
	 * @author  zhangly
	 *
	 * @param batchId
	 * @param busiType
	 */
	void backFill(String batchId, String busiType);
	
	/**
	 * 
	 * 功能描述：回填批次计算结果审核状态
	 *
	 * @author  zhangly
	 *
	 * @param batchId
	 * @param caseId
	 */
	void backFillCase(String batchId, String caseId);
	
	/**
	 * 
	 * 功能描述：回填批次计算结果审核状态
	 *
	 * @author  zhangly
	 *
	 * @param batchId
	 * @param ruleId
	 */
	void backFillRule(String batchId, String ruleId);
}
