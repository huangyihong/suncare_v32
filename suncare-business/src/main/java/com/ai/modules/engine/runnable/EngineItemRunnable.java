/**
 * EngineDrugRunnable.java	  V1.0   2020年9月21日 上午10:48:51
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.runnable;

import com.ai.modules.engine.service.IEngineBaseService;

/**
 *
 * 功能描述：药品合规、收费合规、临床路径等任务批次线程
 *
 * @author  zhangly
 * Date: 2020年9月21日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineItemRunnable extends AbsEngineRunnable {

	private String batchId;
	private String itemCode;
	private IEngineBaseService service;

	public EngineItemRunnable(String datasource, String batchId, String itemCode, IEngineBaseService service) {
		super(datasource);
		this.batchId = batchId;
		this.itemCode = itemCode;
		this.service = service;
	}

	@Override
	public void execute() throws Exception {
		service.generateUnreasonableAction(batchId, itemCode);
	}
}
