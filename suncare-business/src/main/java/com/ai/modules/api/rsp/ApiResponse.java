/**
 * ApiResponse.java	  V1.0   2018年4月21日 下午8:36:04
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.api.rsp;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;

import com.ai.modules.api.ApiResultType;
import com.alibaba.fastjson.JSON;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "响应数据")
public class ApiResponse<T> extends Result<T> {
	
	@Override
	public String toString() {
		return "ApiResponse [success=" + success + ", message=" + message + ", code=" + code + ", result=" + result
				+ ", timestamp=" + timestamp + "]";
	}

	public String toJSONString() {
		return JSON.toJSONString(this);
	}

	public ApiResponse() {
		
	}
	
	public ApiResponse<T> success(String message) {
		this.message = message;
		this.code = CommonConstant.SC_OK_200;
		this.success = true;
		return this;
	}
	
	public ApiResponse<T> error500(String message) {
		this.message = message;
		this.code = CommonConstant.SC_INTERNAL_SERVER_ERROR_500;
		this.success = false;
		return this;
	}
	
	public ApiResponse(ApiResultType result) {
		this.code = ApiResultType.OK.getCode();
		this.message = ApiResultType.OK.getMessage();
	}
	
	public ApiResponse(ApiResultType result, T data) {
		this.code = result.getCode();
		this.message = result.getMessage();
		this.result = data;
	}
	
	public static ApiResponse<Object> ok() {
		ApiResponse<Object> r = new ApiResponse<Object>();
		r.setSuccess(true);
		r.setMessage("success");
		r.setCode(CommonConstant.SC_OK_200);
		r.setMessage("success");
		return r;
	}
	
	public static ApiResponse<Object> ok(Object data) {
		ApiResponse<Object> r = new ApiResponse<Object>();
		r.setSuccess(true);
		r.setMessage("success");
		r.setCode(CommonConstant.SC_OK_200);
		r.setResult(data);
		return r;
	}
	
	public static ApiResponse<Object> error(String msg) {
		return error(CommonConstant.SC_INTERNAL_SERVER_ERROR_500, msg);
	}
	
	public static ApiResponse<Object> error(int code, String msg) {
		ApiResponse<Object> r = new ApiResponse<Object>();
		r.setCode(code);
		r.setMessage(msg);
		r.setSuccess(false);
		return r;
	}
}
