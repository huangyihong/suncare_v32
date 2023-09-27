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

import com.ai.modules.engine.service.IEngineClinicalService;

/**
 * 
 * 功能描述：临床路径不合规计算任务线程
 *
 * @author  zhangly
 * Date: 2020年9月21日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineClinicalRunnable extends AbsEngineRunnable {
	
	private String batchId;
	private String clinicalId;

	public EngineClinicalRunnable(String datasource, String batchId, String clinicalId) {
		super(datasource);
		this.batchId = batchId;
		this.clinicalId = clinicalId;
	}

	@Override
	public void execute() throws Exception {
		IEngineClinicalService service = SpringContextUtils.getApplicationContext().getBean(IEngineClinicalService.class);
		service.generateMedicalUnreasonableClinicalAction(batchId, clinicalId);
	}
}
