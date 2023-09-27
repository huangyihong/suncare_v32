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

import com.ai.modules.engine.service.IEngineCaseService;

/**
 * 
 * 功能描述：业务组某个模型计算任务线程
 *
 * @author  zhangly
 * Date: 2020年9月21日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineCaseRunnable extends AbsEngineRunnable {
	
	private String batchId;
	private String caseId;

	public EngineCaseRunnable(String datasource) {
		super(datasource);
	}

	@Override
	public void execute() throws Exception {
		IEngineCaseService service = SpringContextUtils.getApplicationContext().getBean(IEngineCaseService.class);
		service.generateMedicalUnreasonableAction(batchId, caseId);
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}
}
