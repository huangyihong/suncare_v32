/**
 * IApiTrialService.java	  V1.0   2021年9月3日 下午3:04:29
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.api;

import java.util.List;

import com.ai.modules.medical.entity.MedicalFlowTrial;

public interface IApiTrialService {
	void saveMedicalFlowTrial(MedicalFlowTrial trial);
	
	void saveMedicalFlowTrial(List<MedicalFlowTrial> trialList);
	
	void updateMedicalFlowTrial(String caseId, String nodeCode, String datasource, MedicalFlowTrial trial);
	
	void removeMedicalFlowTrial(String caseId, String datasource);
}
