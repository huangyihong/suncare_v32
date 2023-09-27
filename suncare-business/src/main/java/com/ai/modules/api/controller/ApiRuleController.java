/**
 * ApiTaskController.java	  V1.0   2020年12月23日 下午2:53:23
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
import com.ai.modules.medical.service.IMedicalRuleConfigService;
import com.ai.modules.medical.vo.QueryMedicalRuleConfigVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags="规则相关")
@Controller
@RequestMapping("/oauth/api/rule")
public class ApiRuleController {
	@Autowired
    private IMedicalRuleConfigService service;
		
	@ApiOperation(value = "查找规则")
	@RequestMapping(value="/config", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> batchRule(String ruleType, String ruleLimit) throws Exception {
		List<QueryMedicalRuleConfigVO> items = service.queryMedicalRuleConfig(ruleType, ruleLimit);
		return ApiResponse.ok(items);
	}	
}
