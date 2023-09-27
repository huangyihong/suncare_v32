/**
 * ClinicalServiceImpl.java	  V1.0   2020年12月28日 上午9:26:16
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.api.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.modules.engine.mapper.QueryTaskBatchBreakRuleMapper;
import com.ai.modules.engine.service.api.IApiClinicalService;
import com.ai.modules.medical.entity.MedicalClinical;
import com.ai.modules.medical.entity.MedicalClinicalAccessGroup;
import com.ai.modules.medical.entity.MedicalClinicalInfo;
import com.ai.modules.medical.entity.MedicalClinicalRangeGroup;
import com.ai.modules.medical.service.IMedicalClinicalAccessGroupService;
import com.ai.modules.medical.service.IMedicalClinicalInfoService;
import com.ai.modules.medical.service.IMedicalClinicalRangeGroupService;
import com.ai.modules.medical.service.IMedicalClinicalService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

//@Service
public class ClinicalServiceImpl implements IApiClinicalService {
	@Autowired
	private IMedicalClinicalService clinicalService;
	@Autowired
	private IMedicalClinicalInfoService clinicalInfoService;
	@Autowired
	private IMedicalClinicalAccessGroupService clinicalAccessGroupService;
	@Autowired
	private IMedicalClinicalRangeGroupService clinicalRangeGroupService;
	@Autowired
	private QueryTaskBatchBreakRuleMapper queryBreakRuleMapper;
	
	@Override
	public MedicalClinical findMedicalClinical(String clinicalId) {
		MedicalClinical clinical = clinicalService.getById(clinicalId);
		return clinical;
	}

	@Override
	public MedicalClinicalInfo findMedicalClinicalInfo(String clinicalId) {
		MedicalClinicalInfo clinicalInfo = clinicalInfoService.getById(clinicalId);
		return clinicalInfo;
	}

	@Override
	public List<MedicalClinicalAccessGroup> findMedicalClinicalAccessGroup(String clinicalId, String groupType) {
		QueryWrapper<MedicalClinicalAccessGroup> wrapper = new QueryWrapper<MedicalClinicalAccessGroup>()
				.eq("CLINICAL_ID", clinicalId).eq("GROUP_TYPE", groupType);
		List<MedicalClinicalAccessGroup> list = clinicalAccessGroupService.list(wrapper);
		return list;
	}

	@Override
	public List<MedicalClinicalRangeGroup> findMedicalClinicalRangeGroup(String clinicalId) {
		QueryWrapper<MedicalClinicalRangeGroup> wrapper = new QueryWrapper<MedicalClinicalRangeGroup>()
				.eq("CLINICAL_ID", clinicalId);
		List<MedicalClinicalRangeGroup> rangeList = clinicalRangeGroupService.list(wrapper);
		return rangeList;
	}

	@Override
	public List<MedicalClinicalRangeGroup> findClinicalRequireDrugGroup(String clinicalId) {
		List<MedicalClinicalRangeGroup> result = queryBreakRuleMapper.queryClinicalRequireDrugGroup(clinicalId);
		return result;
	}

	@Override
	public List<MedicalClinicalRangeGroup> findClinicalRequireTreatGroup(String clinicalId) {
		List<MedicalClinicalRangeGroup> result = queryBreakRuleMapper.queryClinicalRequireTreatGroup(clinicalId);
		return result;
	}
}
