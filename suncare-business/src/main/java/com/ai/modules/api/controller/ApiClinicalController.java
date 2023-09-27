/**
 * ApiClinicalController.java	  V1.0   2020年12月28日 上午9:21:09
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ai.modules.api.rsp.ApiResponse;
import com.ai.modules.engine.mapper.QueryTaskBatchBreakRuleMapper;
import com.ai.modules.medical.entity.MedicalClinical;
import com.ai.modules.medical.entity.MedicalClinicalAccessGroup;
import com.ai.modules.medical.entity.MedicalClinicalInfo;
import com.ai.modules.medical.entity.MedicalClinicalRangeGroup;
import com.ai.modules.medical.service.IMedicalClinicalAccessGroupService;
import com.ai.modules.medical.service.IMedicalClinicalInfoService;
import com.ai.modules.medical.service.IMedicalClinicalRangeGroupService;
import com.ai.modules.medical.service.IMedicalClinicalService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags="临床路径相关")
@Controller
@RequestMapping("/oauth/api/clinical")
public class ApiClinicalController {
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
	
	@ApiOperation(value = "根据编号查找临床路径")
	@RequestMapping(value="", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> clinical(String clinicalId) throws Exception {
		MedicalClinical clinical = clinicalService.getById(clinicalId);
		return ApiResponse.ok(clinical);
	}
	
	@ApiOperation(value = "根据编号查找临床路径")
	@RequestMapping(value="/clinicalInfo", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> clinicalInfo(String clinicalId) throws Exception {
		MedicalClinicalInfo clinicalInfo = clinicalInfoService.getById(clinicalId);
		return ApiResponse.ok(clinicalInfo);
	}
	
	@ApiOperation(value = "查找临床路径准入条件")
	@RequestMapping(value="/access", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> access(String clinicalId, String groupType) throws Exception {
		QueryWrapper<MedicalClinicalAccessGroup> wrapper = new QueryWrapper<MedicalClinicalAccessGroup>()
				.eq("CLINICAL_ID", clinicalId).eq("GROUP_TYPE", groupType);
		List<MedicalClinicalAccessGroup> list = clinicalAccessGroupService.list(wrapper);
		return ApiResponse.ok(list);
	}
	
	@ApiOperation(value = "查找临床路径药品、项目范围")
	@RequestMapping(value="/range", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> range(String clinicalId) throws Exception {
		QueryWrapper<MedicalClinicalRangeGroup> wrapper = new QueryWrapper<MedicalClinicalRangeGroup>()
				.eq("CLINICAL_ID", clinicalId);
		List<MedicalClinicalRangeGroup> rangeList = clinicalRangeGroupService.list(wrapper);
		return ApiResponse.ok(rangeList);
	}
	
	@ApiOperation(value = "查找临床路径必需包含的药品范围")
	@RequestMapping(value="/range/requireDrug", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> rangeRequireDrug(String clinicalId) throws Exception {
		List<MedicalClinicalRangeGroup> treatList  = queryBreakRuleMapper.queryClinicalRequireDrugGroup(clinicalId);
		return ApiResponse.ok(treatList);
	}
	
	@ApiOperation(value = "查找临床路径必需包含的项目范围")
	@RequestMapping(value="/range/requireTreat", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> rangeRequireTreat(String clinicalId) throws Exception {
		List<MedicalClinicalRangeGroup> treatList  = queryBreakRuleMapper.queryClinicalRequireTreatGroup(clinicalId);
		return ApiResponse.ok(treatList);
	}
}
