/**
 * ApiOauthAop.java	  V1.0   2018年6月13日 上午11:49:37
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.api.aop;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ai.modules.api.rsp.ApiResponse;
import com.ai.modules.api.util.ApiOauthUtil;
import com.ai.modules.api.util.DigestUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 
 * 功能描述：接口协议返回报文增加签名(存放在header中)
 *
 * @author  zhangly
 * Date: 2018年6月21日
 * Copyright (c) 2018 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Component
@Aspect
public class ApiOauthAop {
	private static final Logger logger = LoggerFactory.getLogger(ApiOauthAop.class);
	
	@AfterReturning(pointcut = "execution(* com.ai.modules.api.controller..*Controller.*(..))", returning = "_result")
	public void afterReturning(Object _result) {
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		if(_result instanceof ApiResponse) {
			SerializerFeature[] features        = new SerializerFeature[0];
			SerializeFilter[] serialzeFilters = new SerializeFilter[0];

			String text = JSON.toJSONString(_result, // 
	                SerializeConfig.globalInstance, // 
	                serialzeFilters, // 
	                null, // 
	                JSON.DEFAULT_GENERATE_FEATURE, // 
	                features);			
			HttpServletRequest request = requestAttributes.getRequest();
			//logger.info("接口调用协议的appKey:"+request.getParameter("appKey"));
			HttpServletResponse response = requestAttributes.getResponse();
			try {
				response.setHeader("sign", DigestUtil.sign(ApiOauthUtil.API_SECRET+text+ApiOauthUtil.API_SECRET));
			} catch (Exception e) {
			}
		} else if(_result instanceof String) {
			HttpServletResponse response = requestAttributes.getResponse();
			try {
				response.setHeader("sign", DigestUtil.sign(ApiOauthUtil.API_SECRET+_result.toString()+ApiOauthUtil.API_SECRET));
			} catch (Exception e) {
			}
		}
	}
}
