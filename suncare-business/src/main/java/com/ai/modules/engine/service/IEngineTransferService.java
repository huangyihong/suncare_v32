/**
 * IEngineTransferService.java	  V1.0   2020年9月16日 上午9:24:34
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service;

public interface IEngineTransferService {
	/**
	 * 
	 * 功能描述：批次数据从solr迁移到oracle数据库
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年9月16日 上午9:40:33</p>
	 *
	 * @param batchId
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void solrTransferOracle(String batchId) throws Exception;
}
