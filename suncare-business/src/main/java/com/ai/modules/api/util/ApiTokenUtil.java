/**
 * ApiOauthUtil.java	  V1.0   2020年12月15日 下午8:43:07
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.api.util;

import com.ai.common.utils.ThreadUtils;
import com.ai.modules.api.rsp.ApiResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.shiro.vo.DefContants;
import org.jeecg.common.system.vo.LoginUser;

import java.lang.reflect.Type;
import java.util.*;

public class ApiTokenUtil {
	private static Properties prop = new Properties();
	/**接口调用根路径*/
	public static String API_URL;
	/**鉴权key*/
	public static String API_APP_KEY;
	/**鉴权私钥*/
	public static String API_SECRET;
	// 是否中心节点
	public static Boolean IS_CENTER;
	// 是否地方节点的主节点 执行启动或定时同步程序
	public static Boolean IS_NODE_MAIN;

	private static List<String> DATA_SOURCES = new ArrayList<>();

	public static String DEFAULT_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2MDgwODM5NDAsImRhdGFTb3VyY2UiOiJoZXplIiwidXNlcm5hbWUiOiJ6aGFuZ2x5In0.tyX_KQ9cjPn5c2etd6xjalUTtBY9qIMObC2K-2wLcD4";

	private static final CloseableHttpClient httpClient;
	private static final String CHARSET = "UTF-8";

	static {
		try {
			prop.load(ApiTokenUtil.class.getClassLoader().getResourceAsStream("config.properties"));
			API_URL = prop.getProperty("api.url");
			API_APP_KEY = prop.getProperty("api.appKey");
			API_SECRET = prop.getProperty("api.appSecret");
			IS_CENTER = Boolean.parseBoolean(prop.getProperty("IS_CENTER"));
			IS_NODE_MAIN = Boolean.parseBoolean(prop.getProperty("IS_NODE_MAIN"));
			String dataSources = prop.getProperty("DATA_SOURCES");
			if(StringUtils.isNotBlank(dataSources)){
                DATA_SOURCES.addAll(Arrays.asList(dataSources.replaceAll(" ", "").split(",")));
			}
			DEFAULT_TOKEN = prop.getProperty("DEFAULT_TOKEN");
		} catch(Exception ignored) {}

		RequestConfig config = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build();
		httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	}

	public static List<String> getDataSources(){
	    return DATA_SOURCES;
    }

	public static List<String> getNodeDataSources(){
		return IS_NODE_MAIN?DATA_SOURCES: new ArrayList<>();
	}


	public static <T> List<T> getArray(String mappingUrl, Map<String, String> busiParams, Class<T> clazz) {
		ApiResponse<?> rsp = getApi(mappingUrl, busiParams);
		return parseArray(rsp, clazz);
	}

	public static <T> T getObj(String mappingUrl, Map<String, String> busiParams, Class<T> clazz) {
		ApiResponse<?> rsp = getApi(mappingUrl, busiParams);
		return parseObject(rsp, clazz);
	}

	public static JSONObject getJson(String mappingUrl, Map<String, String> busiParams) {
		ApiResponse<?> rsp = getApi(mappingUrl, busiParams);
		if(null == rsp.getResult()) {
			return null;
		}
		String text = rsp.getResult().toString();
		return (JSONObject)JSON.parse(text);
	}

	public static <T> List<T> postArray(String mappingUrl, Map<String, String> busiParams, Class<T> clazz) {
		ApiResponse<?> rsp = postApi(mappingUrl, busiParams);
		return parseArray(rsp, clazz);
	}

	public static <T> T postObj(String mappingUrl, Map<String, String> busiParams, Class<T> clazz) {
		ApiResponse<?> rsp = postApi(mappingUrl, busiParams);
		return parseObject(rsp, clazz);
	}

	public static <T> IPage<T> Page(String mappingUrl, Map<String, String> busiParams, Class<T> clazz) {
		ApiResponse<?> rsp = getApi(mappingUrl, busiParams);
		return parsePage(rsp, clazz);
	}

	public static  ApiResponse<?> postApi(String mappingUrl, Map<String, String> busiParams){
		return response(mappingUrl, busiParams, "post");
	}

	public static ApiResponse<?> getApi(String mappingUrl, Map<String, String> busiParams){
		return response(mappingUrl, busiParams, "get");
	}

	public static  ApiResponse<?> deleteApi(String mappingUrl, Map<String, String> busiParams){
		return response(mappingUrl, busiParams, "delete");
	}

	public static  ApiResponse<?> postBodyApi(String mappingUrl, Object obj){
		return response(mappingUrl, obj, "post");
	}

	public static  ApiResponse<?> putBodyApi(String mappingUrl, Object obj){
		return response(mappingUrl, obj, "put");
	}

	/**
	 *
	 * 功能描述：get方式请求
	 *
	 */
	public static String doGet(String rootUrl, String mappingUrl, Map<String, String> params)
			throws Exception {
		// 请求地址
		StringBuilder url = new StringBuilder(rootUrl);
		if(mappingUrl!=null && !"".equals(mappingUrl.trim())) {
			if (mappingUrl.startsWith("/")) {
				url.append(mappingUrl);
			} else {
				url.append("/").append(mappingUrl);
			}
		}
		if (params != null && !params.isEmpty()) {
			List<NameValuePair> pairs = new ArrayList<>(params.size());
			for (Map.Entry<String, String> entry : params.entrySet()) {
				String value = entry.getValue();
				if (value != null) {
					pairs.add(new BasicNameValuePair(entry.getKey(), value));
				}
			}
			// 将请求参数和url进行拼接
			url.append("?").append(EntityUtils.toString(new UrlEncodedFormEntity(pairs, CHARSET)));
		}
		//System.out.println("url:"+url.toString());
		HttpGet httpGet = new HttpGet(url.toString());
		/*if(token != null && token.length() > 0) {
			httpGet.setHeader("X-Access-Token", token);
        } else {*/
			setToken(httpGet);
//		}
		CloseableHttpResponse response = httpClient.execute(httpGet);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			httpGet.abort();
			throw new RuntimeException("HttpClient,error status code :" + statusCode);
		}
		HttpEntity entity = response.getEntity();
		String result = null;
		if (entity != null) {
			result = EntityUtils.toString(entity, "utf-8");
		}
		EntityUtils.consume(entity);
		response.close();
		return result;
	}

	public static String doDelete(String rootUrl, String mappingUrl, Map<String, String> params)
			throws Exception {
		// 请求地址
		StringBuilder url = new StringBuilder(rootUrl);
		if(mappingUrl!=null && !"".equals(mappingUrl.trim())) {
			if (mappingUrl.startsWith("/")) {
				url.append(mappingUrl);
			} else {
				url.append("/").append(mappingUrl);
			}
		}
		if (params != null && !params.isEmpty()) {
			List<NameValuePair> pairs = new ArrayList<>(params.size());
			for (Map.Entry<String, String> entry : params.entrySet()) {
				String value = entry.getValue();
				if (value != null) {
					pairs.add(new BasicNameValuePair(entry.getKey(), value));
				}
			}
			// 将请求参数和url进行拼接
			url.append("?").append(EntityUtils.toString(new UrlEncodedFormEntity(pairs, CHARSET)));
		}
		//System.out.println("url:"+url.toString());
		HttpDelete httpHandle = new HttpDelete(url.toString());
		/*if(token != null && token.length() > 0) {
			httpHandle.setHeader("X-Access-Token", token);
		} else {*/
			setToken(httpHandle);
//		}
		CloseableHttpResponse response = httpClient.execute(httpHandle);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			httpHandle.abort();
			throw new RuntimeException("HttpClient,error status code :" + statusCode);
		}
		HttpEntity entity = response.getEntity();
		String result = null;
		if (entity != null) {
			result = EntityUtils.toString(entity, "utf-8");
		}
		EntityUtils.consume(entity);
		response.close();
		return result;
	}

	/**
	 *
	 * 功能描述：post方式请求
	 *
	 */
	public static String doPost(String rootUrl, String mappingUrl, Map<String, String> params)
			throws Exception {
		// 请求地址
		StringBuilder url = new StringBuilder(rootUrl);
		if(mappingUrl!=null && !"".equals(mappingUrl.trim())) {
			if (mappingUrl.startsWith("/")) {
				url.append(mappingUrl);
			} else {
				url.append("/").append(mappingUrl);
			}
		}
		List<NameValuePair> pairs = null;
		if (params != null && !params.isEmpty()) {
			pairs = new ArrayList<NameValuePair>(params.size());
			for (Map.Entry<String, String> entry : params.entrySet()) {
				String value = entry.getValue();
				if (value != null) {
					pairs.add(new BasicNameValuePair(entry.getKey(), value));
				}
			}
		}
		HttpPost httpPost = new HttpPost(url.toString());
        if (pairs != null && pairs.size() > 0) {
            httpPost.setEntity(new UrlEncodedFormEntity(pairs, CHARSET));
        }
		/*if(token != null && token.length() > 0) {
        	httpPost.setHeader("X-Access-Token", token);
        } else {*/
			setToken(httpPost);
//		}
		CloseableHttpResponse response = httpClient.execute(httpPost);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			httpPost.abort();
			throw new RuntimeException("HttpClient,error status code :" + statusCode);
		}
		HttpEntity entity = response.getEntity();
		String result = null;
		if (entity != null) {
			result = EntityUtils.toString(entity, "utf-8");
		}
		EntityUtils.consume(entity);
		response.close();
		return result;
	}

	/**
	 *
	 * 功能描述：post方式请求
	 *
	 */
	public static String doPostBody(String rootUrl, String mappingUrl, Object obj)
			throws Exception {
		// 请求地址
		StringBuilder url = new StringBuilder(rootUrl);
		if(mappingUrl!=null && !"".equals(mappingUrl.trim())) {
			if (mappingUrl.startsWith("/")) {
				url.append(mappingUrl);
			} else {
				url.append("/").append(mappingUrl);
			}
		}

		HttpPost httpHandle = new HttpPost(url.toString());
		if (obj != null) {
			StringEntity httpEntity = new StringEntity(obj instanceof String?obj.toString(): JSONObject.toJSON(obj).toString(), "utf-8");
			httpEntity.setContentType("application/json");
			httpHandle.setEntity(httpEntity);
		}
		/*if(token != null && token.length() > 0) {
			httpHandle.setHeader("X-Access-Token", token);
		} else {*/
			setToken(httpHandle);
//		}
		CloseableHttpResponse response = httpClient.execute(httpHandle);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			httpHandle.abort();
			throw new RuntimeException("HttpClient,error status code :" + statusCode);
		}
		HttpEntity entity = response.getEntity();
		String result = null;
		if (entity != null) {
			result = EntityUtils.toString(entity, "utf-8");
		}
		EntityUtils.consume(entity);
		response.close();
		return result;
	}

	/**
	 * 请求内容为对象的PUT请求方式
	 * @param rootUrl
	 * @param mappingUrl
	 * @param SerialObj
	 * @param token
	 * @return
	 * @throws Exception
	 */

	public static String doPutBody(String rootUrl, String mappingUrl, Object obj)
			throws Exception {
		// 请求地址
		StringBuilder url = new StringBuilder(rootUrl);
		if(mappingUrl!=null && !"".equals(mappingUrl.trim())) {
			if (mappingUrl.startsWith("/")) {
				url.append(mappingUrl);
			} else {
				url.append("/").append(mappingUrl);
			}
		}

		HttpPut httpHandle = new HttpPut(url.toString());
		if (obj != null) {
			StringEntity httpEntity = new StringEntity(obj instanceof String?obj.toString(): JSONObject.toJSON(obj).toString(), "utf-8");
			httpEntity.setContentType("application/json");
			httpHandle.setEntity(httpEntity);
		}
		/*if(token != null && token.length() > 0) {
			httpHandle.setHeader("X-Access-Token", token);
		} else {*/
			setToken(httpHandle);
//		}
		CloseableHttpResponse response = httpClient.execute(httpHandle);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			httpHandle.abort();
			throw new RuntimeException("HttpClient,error status code :" + statusCode);
		}
		HttpEntity entity = response.getEntity();
		String result = null;
		if (entity != null) {
			result = EntityUtils.toString(entity, "utf-8");
		}
		EntityUtils.consume(entity);
		response.close();
		return result;
	}


	private static  ApiResponse<?> response(String mappingUrl, Object obj, String type){
		try {
			String text = null;
			if ("post".equalsIgnoreCase(type)) {
				text = doPostBody(ApiTokenUtil.API_URL,mappingUrl, obj);
			} else if ("put".equalsIgnoreCase(type)){
				text = doPutBody(ApiTokenUtil.API_URL,mappingUrl, obj);
			}
			ApiResponse<?> rsp = JSON.parseObject(text, ApiResponse.class);
			if(!rsp.isSuccess()) {
				throw new Exception(rsp.getMessage());
			}
			return rsp;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static  ApiResponse<?> response(String mappingUrl, Map<String, String> busiParams, String type){
		try {
			String text = null;
			if ("post".equalsIgnoreCase(type)) {
				text = doPost(ApiTokenUtil.API_URL,mappingUrl, busiParams);
			} else if ("get".equalsIgnoreCase(type)){
				text = doGet(ApiTokenUtil.API_URL,mappingUrl, busiParams);
			} else if ("delete".equalsIgnoreCase(type)){
				text = doDelete(ApiTokenUtil.API_URL,mappingUrl, busiParams);
			}
			ApiResponse<?> rsp = JSON.parseObject(text, ApiResponse.class);
			if(!rsp.isSuccess()) {
				throw new Exception(rsp.getMessage());
			}
			return rsp;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> List<T> parseArray(ApiResponse<?> rsp, Class<T> clazz) {
		if(null==rsp.getResult()) {
			return null;
		}
		String text = rsp.getResult().toString();
		return JSON.parseArray(text, clazz);
	}

	public static <T> T parseObject(ApiResponse<?> rsp, Class<T> clazz) {
		if(null==rsp.getResult()) {
			return null;
		}
		String text = rsp.getResult().toString();
		return JSON.parseObject(text, clazz);
	}
	public static <T> IPage<T> parsePage(ApiResponse<?> rsp, Class<T> clazz) {
		if(null==rsp.getResult()) {
			return null;
		}

		String text = rsp.getResult().toString();
		IPage<T> page = JSON.parseObject(text, new ParameterizedTypeImpl(new Type[]{clazz}, null, Page.class));
		return page;
	}

	private static void setToken(HttpRequestBase requestBase){
		String token = ThreadUtils.getToken(); //从线程副本获取数据源
		if(token == null) {
			try {
				// 启动时因为线程安全会报错
				LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
				if(loginUser != null){
					token = loginUser.getToken();
				}
			} catch (Exception ignored){ }

		}
		requestBase.addHeader(DefContants.X_ACCESS_TOKEN, DEFAULT_TOKEN);
		if(token != null && !token.equals(DEFAULT_TOKEN)){
			requestBase.addHeader(DefContants.X_TOKEN_USER, token);
		}

	}

}
