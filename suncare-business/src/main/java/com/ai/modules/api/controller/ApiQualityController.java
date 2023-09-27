/**
 * ApiQualityController.java	  V1.0   2021年3月26日 下午4:42:11
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ai.modules.api.rsp.ApiResponse;
import com.ai.modules.medical.service.IMedicalColumnQualityService;

@Controller
@RequestMapping("/admin/rule")
public class ApiQualityController {
	@Autowired
	private IMedicalColumnQualityService service;
	
	@RequestMapping(value="/columnQuality", method = {RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ApiResponse<?> ruleList(String batchId, String stepType) throws Exception {
		service.computeMedicalColumnQualityVO();
		return ApiResponse.ok();
	}
}
