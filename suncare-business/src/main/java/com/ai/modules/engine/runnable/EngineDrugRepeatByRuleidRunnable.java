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

import com.ai.modules.engine.service.IEngineRepeatDrugService;

/**
 * 
 * 功能描述：重复用药不合规计算任务线程
 *
 * @author  zhangly
 * Date: 2020年9月21日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineDrugRepeatByRuleidRunnable extends AbsEngineRunnable {
	
	private String batchId;
	private String ruleId;

	public EngineDrugRepeatByRuleidRunnable(String datasource, String batchId, String ruleId) {
		super(datasource);
		this.batchId = batchId;
		this.ruleId = ruleId;
	}

	@Override
	public void execute() throws Exception {
		IEngineRepeatDrugService service = SpringContextUtils.getApplicationContext().getBean(IEngineRepeatDrugService.class);
		service.generateUnreasonableAction(batchId, ruleId);
	}
}
