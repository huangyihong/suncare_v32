/**
 * CaseJobMeta.java	  V1.0   2020年2月11日 下午2:46:49
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.job.meta;

public class EngineJobMeta extends JobMeta {	
	//任务批次号
	private String batchNo;
	
	public EngineJobMeta(BaseMeta baseMeta) {
		super(baseMeta);
	}

	@Override
	protected void parse() throws Exception {
		String batchId = this.getBaseMeta().getParams().get("pc");
		this.batchNo = batchId;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}
}
