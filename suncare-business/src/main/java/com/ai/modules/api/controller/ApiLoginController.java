/**
 * ApiLoginController.java	  V1.0   2018年5月3日 下午5:45:33
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ai.modules.api.rsp.ApiResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags="API登录")
@Controller
@RequestMapping("/oauth/api")
public class ApiLoginController {
	
	@ApiOperation(value = "登录接口")
	@RequestMapping(value="/login", method = {RequestMethod.POST, RequestMethod.GET })
	@ApiImplicitParams({
    	@ApiImplicitParam(name = "userCode", value = "账号", required = true, paramType = "query"),
    	@ApiImplicitParam(name = "userPwd", value = "密码", required = true, paramType = "query")
    })
	@ResponseBody
	public ApiResponse<?> doLogin(String userCode, String userPwd) throws Exception {
		if("admin".equals(userCode) && "123456".equals(userPwd)) {
			return ApiResponse.ok();
		}
		return ApiResponse.error("账号或密码不正确！");
	}
}
