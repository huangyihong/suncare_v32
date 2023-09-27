/**
 * AbsSecondaryHandle.java	  V1.0   2020年12月4日 上午10:00:08
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.secondary;

import java.util.List;

import com.ai.modules.medical.entity.MedicalDrugRule;

public abstract class AbsSecondaryHandle {
	protected String batchId;
	protected String itemCode;
	protected List<MedicalDrugRule> ruleList;
	protected boolean trail;
	
	public AbsSecondaryHandle(String batchId, String itemCode, List<MedicalDrugRule> ruleList, boolean trail) {
		this.batchId = batchId;
		this.itemCode = itemCode;
		this.ruleList = ruleList;
		this.trail = trail;
	}
	
	public abstract void execute() throws Exception;
}
