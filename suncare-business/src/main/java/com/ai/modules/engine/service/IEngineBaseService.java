/**
 * IEngineBaseService.java	  V1.0   2020年9月21日 上午11:00:52
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.service;

public interface IEngineBaseService {

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
	void generateUnreasonableAction(String batchId);

	/**
	 * 
	 * 功能描述：跑批次的某个规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月1日 上午9:14:31</p>
	 *
	 * @param batchId
	 * @param itemCode
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void generateUnreasonableAction(String batchId, String itemCode);
}
