/**
 * ApiClinicalServiceImpl.java	  V1.0   2020年12月28日 上午9:25:39
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
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
import com.ai.modules.engine.service.api.IApiClinicalService;
import com.ai.modules.medical.entity.MedicalClinical;
import com.ai.modules.medical.entity.MedicalClinicalAccessGroup;
import com.ai.modules.medical.entity.MedicalClinicalInfo;
import com.ai.modules.medical.entity.MedicalClinicalRangeGroup;

@Service
public class ApiClinicalServiceImpl implements IApiClinicalService {

	@Override
	public MedicalClinical findMedicalClinical(String clinicalId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("clinicalId", clinicalId);
		MedicalClinical clinical = ApiOauthUtil.response("/oauth/api/clinical", busiParams, "post", MedicalClinical.class);
		return clinical;
	}

	@Override
	public MedicalClinicalInfo findMedicalClinicalInfo(String clinicalId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("clinicalId", clinicalId);
		MedicalClinicalInfo clinicalInfo = ApiOauthUtil.response("/oauth/api/clinical", busiParams, "post", MedicalClinicalInfo.class);
		return clinicalInfo;
	}

	@Override
	public List<MedicalClinicalAccessGroup> findMedicalClinicalAccessGroup(String clinicalId, String groupType) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("clinicalId", clinicalId);
		busiParams.put("groupType", groupType);
		List<MedicalClinicalAccessGroup> result = ApiOauthUtil.responseArray("/oauth/api/clinical/access", busiParams, "post", MedicalClinicalAccessGroup.class);
		return result;
	}

	@Override
	public List<MedicalClinicalRangeGroup> findMedicalClinicalRangeGroup(String clinicalId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("clinicalId", clinicalId);
		List<MedicalClinicalRangeGroup> result = ApiOauthUtil.responseArray("/oauth/api/clinical/range", busiParams, "post", MedicalClinicalRangeGroup.class);
		return result;
	}

	@Override
	public List<MedicalClinicalRangeGroup> findClinicalRequireDrugGroup(String clinicalId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("clinicalId", clinicalId);
		List<MedicalClinicalRangeGroup> result = ApiOauthUtil.responseArray("/oauth/api/clinical/range/requireDrug", busiParams, "post", MedicalClinicalRangeGroup.class);
		return result;
	}

	@Override
	public List<MedicalClinicalRangeGroup> findClinicalRequireTreatGroup(String clinicalId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("clinicalId", clinicalId);
		List<MedicalClinicalRangeGroup> result = ApiOauthUtil.responseArray("/oauth/api/clinical/range/requireTreat", busiParams, "post", MedicalClinicalRangeGroup.class);
		return result;
	}

}
