/**
 * EngineCaseRunnable.java	  V1.0   2020年9月21日 下午12:24:47
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.runnable;

import org.jeecg.common.util.SpringContextUtils;

import com.ai.modules.engine.service.IEngineRuleService;

/**
 * 
 * 功能描述：不合规计算任务线程
 *
 * @author  zhangly
 * Date: 2020年9月21日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineRuleConfigByBatchRunnable extends AbsEngineRunnable {
	
	private String batchId;
	private String busiType;

	public EngineRuleConfigByBatchRunnable(String datasource, String batchId, String busiType) {
		super(datasource);
		this.batchId = batchId;
		this.busiType = busiType;
	}

	@Override
	public void execute() throws Exception {
		IEngineRuleService service = SpringContextUtils.getApplicationContext().getBean(IEngineRuleService.class);
		service.generateUnreasonableAction(batchId, busiType);
	}
}
