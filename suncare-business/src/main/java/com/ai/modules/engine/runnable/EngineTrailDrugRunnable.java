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

import com.ai.modules.engine.service.IEngineDrugService;

/**
 * 
 * 功能描述：药品合规试算任务批次线程
 *
 * @author  zhangly
 * Date: 2020年9月21日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineTrailDrugRunnable extends AbsEngineRunnable {
	
	private String batchId;
	private String etlSource;

	public EngineTrailDrugRunnable(String datasource, String batchId, String etlSource) {
		super(datasource);
		this.batchId = batchId;
		this.etlSource = etlSource;
	}

	@Override
	public void execute() throws Exception {
		IEngineDrugService service = SpringContextUtils.getApplicationContext().getBean(IEngineDrugService.class);
		service.trailDrugAction(batchId, etlSource, datasource);
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getEtlSource() {
		return etlSource;
	}

	public void setEtlSource(String etlSource) {
		this.etlSource = etlSource;
	}
}
