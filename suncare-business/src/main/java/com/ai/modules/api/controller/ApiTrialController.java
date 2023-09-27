/**
 * ApiTrialController.java	  V1.0   2021年9月3日 下午12:40:49
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
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
import com.ai.modules.medical.entity.MedicalFlowTrial;
import com.ai.modules.medical.service.IMedicalFlowTrialService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags="试算模型")
@Controller
@RequestMapping("/oauth/api/trial")
public class ApiTrialController {
	@Autowired
	private IMedicalFlowTrialService trialSV;
	
	@ApiOperation(value = "保存模型试算进度")
	@RequestMapping(value="/case/save", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> save(String dataJson) throws Exception {
		MedicalFlowTrial entity = JSON.parseObject(dataJson, MedicalFlowTrial.class);
        trialSV.save(entity);
		return ApiResponse.ok();
	}
	
	@ApiOperation(value = "批量保存模型试算进度")
	@RequestMapping(value="/case/batchSave", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> batchSave(String dataJson) throws Exception {
		List<MedicalFlowTrial> entityList = JSON.parseArray(dataJson, MedicalFlowTrial.class);
        trialSV.saveBatch(entityList);
		return ApiResponse.ok();
	}
	
	@ApiOperation(value = "删除模型试算进度")
	@RequestMapping(value="/case/remove", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> remove(String caseId, String datasource) throws Exception {
		trialSV.remove(new QueryWrapper<MedicalFlowTrial>().eq("CASE_ID", caseId).eq("PROJECT", datasource));
		return ApiResponse.ok();
	}
	
	@ApiOperation(value = "更新模型试算进度")
	@RequestMapping(value="/case/update", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> update(String caseId, String nodeCode, String datasource, String dataJson) throws Exception {
		QueryWrapper<MedicalFlowTrial> wrapper = new QueryWrapper<MedicalFlowTrial>().eq("CASE_ID", caseId);
		wrapper.eq("PROJECT", datasource);
		wrapper.eq("NODE_CODE", nodeCode);
		MedicalFlowTrial entity = JSON.parseObject(dataJson, MedicalFlowTrial.class);
		trialSV.update(entity, wrapper);
		return ApiResponse.ok();
	}
}
