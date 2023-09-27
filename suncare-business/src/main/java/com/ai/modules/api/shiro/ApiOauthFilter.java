/**
 * ApiOauthFilter.java	  V1.0   2018年5月4日 下午4:35:47
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.api.shiro;

import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.web.filter.AccessControlFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ai.modules.api.ApiErrorCode;
import com.ai.modules.api.ApiOauthException;
import com.ai.modules.api.ApiOauthInfo;
import com.ai.modules.api.rsp.ApiResponse;
import com.ai.modules.api.util.ApiOauthUtil;
import com.alibaba.fastjson.JSON;

/**
 * 
 * 功能描述：api验证过滤器
 *
 * @author  zhangly
 * Date: 2018年5月5日
 * Copyright (c) 2018 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class ApiOauthFilter extends AccessControlFilter {
	private static final Logger logger = LoggerFactory.getLogger(ApiOauthFilter.class);
	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
			throws Exception {
		return false;
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		String appKey = request.getParameter("appKey");
		String timestamp = request.getParameter("timestamp");
		String sign = request.getParameter("sign");
		String v = request.getParameter("v");
		//String bizContent = request.getParameter("bizContent");
		//业务参数
		Map<String, String> busiParams = new HashMap<String, String>();
		Enumeration<?> pNames = request.getParameterNames();
		while(pNames.hasMoreElements()){
		    String name = (String) pNames.nextElement();
		    if(!ApiOauthUtil.API_PUBLIC_PARAM.contains(name)) {
		    	busiParams.put(name, request.getParameter(name));
		    }
		}
		//busiParams.put("bizContent", bizContent);
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		//logger.info("request api url:{}", httpRequest.getRequestURL());
		try {
			//api协议验证
			String secret = ApiOauthUtil.API_SECRET;
			ApiOauthInfo oauth = new ApiOauthInfo();
			oauth.setAppKey(appKey);
			oauth.setSign(sign);
			oauth.setTimestamp(timestamp);
			oauth.setV(v);
			oauth.setSecret(secret);
			//验证参数是否为空
			oauth.checkParams();
			//计算签名
			oauth.buildMap(busiParams);
			String compare = ApiOauthUtil.API_APP_KEY;
			if(!compare.equals(appKey)) {
				throw new ApiOauthException(ApiErrorCode.APPKEY_VALUE_ERR);
			}
			if(!sign.equals(oauth.getSign())) {
				//签名不一致
				throw new ApiOauthException(ApiErrorCode.SIGN_VALUE_ERR);
			}
		} catch(Exception e) {
			logger.error("", e);			
			if(e instanceof ApiOauthException) {
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				httpResponse.setStatus(200);
				httpResponse.setCharacterEncoding("UTF-8");
				httpResponse.setContentType("application/json; charset=utf-8");
				OutputStream out = response.getOutputStream();
				ApiOauthException aoe = (ApiOauthException) e;				
				ApiResponse<?> apiRsp = ApiResponse.error(aoe.getErrorCode(), aoe.getMessage());				
				out.write(JSON.toJSONString(apiRsp).getBytes("utf-8"));
				out.flush();
				out.close();
			}
			return false;
		}
		return true;
	}
}
