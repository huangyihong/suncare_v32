/**
 * IEngineResultHandle.java	  V1.0   2022年5月17日 下午2:45:39
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service;

public interface IEngineResultHandle {
	
	/**
	 * 
	 * 功能描述：计算结果数据从solr同步到数仓
	 *
	 * @author  zhangly
	 *
	 * @param batchId
	 * @throws Exception
	 */
	void syncSolr2Warehouse(String batchId) throws Exception;
	
	/**
	 * 
	 * 功能描述：计算结果数据从solr同步到数仓（开启线程同步）
	 *
	 * @author  zhangly
	 *
	 * @param batchId
	 * @throws Exception
	 */
	void syncSolr2WarehouseByThread(String batchId) throws Exception;
	
	void write(String batchId) throws Exception;
}
