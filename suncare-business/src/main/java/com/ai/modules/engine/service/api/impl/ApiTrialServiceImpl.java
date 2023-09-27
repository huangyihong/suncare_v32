/**
 * ApiTrialServiceImpl.java	  V1.0   2021年9月3日 下午3:05:47
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.api.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ai.modules.api.util.ApiOauthUtil;
import com.ai.modules.engine.service.api.IApiTrialService;
import com.ai.modules.medical.entity.MedicalFlowTrial;
import com.alibaba.fastjson.JSON;

@Service
public class ApiTrialServiceImpl implements IApiTrialService {

	@Override
	public void saveMedicalFlowTrial(MedicalFlowTrial trial) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("dataJson", JSON.toJSONString(trial));
    	ApiOauthUtil.postSuccess("/oauth/api/trial/case/save", busiParams);
	}
	
	@Override
	public void saveMedicalFlowTrial(List<MedicalFlowTrial> trialList) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("dataJson", JSON.toJSONString(trialList));
    	ApiOauthUtil.postSuccess("/oauth/api/trial/case/batchSave", busiParams);
	}

	@Override
	public void updateMedicalFlowTrial(String caseId, String nodeCode, String datasource, MedicalFlowTrial trial) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("caseId", caseId);
		busiParams.put("nodeCode", nodeCode);
		busiParams.put("datasource", datasource);
    	busiParams.put("dataJson", JSON.toJSONString(trial));
    	ApiOauthUtil.postSuccess("/oauth/api/trial/case/update", busiParams);
	}

	@Override
	public void removeMedicalFlowTrial(String caseId, String datasource) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("caseId", caseId);
		busiParams.put("datasource", datasource);
    	ApiOauthUtil.postSuccess("/oauth/api/trial/case/remove", busiParams);
	}

}
