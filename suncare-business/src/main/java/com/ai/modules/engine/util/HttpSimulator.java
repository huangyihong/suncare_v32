/**
 * ApiServerUtils.java	  V1.0   2018年3月3日 下午2:06:34
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpSimulator {
	private static final CloseableHttpClient httpClient;
	public static final String CHARSET = "UTF-8";

	// 采用静态代码块，初始化超时时间配置，再根据配置生成默认httpClient对象
	static {
		RequestConfig config = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build();
		httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	}
	
	public static String callApi(String rootUrl, String mappingUrl, Map<String, String> params, String type) throws Exception {
		type = type==null ? "post" : type;
		if ("post".equalsIgnoreCase(type)) {
			return doPost(rootUrl, mappingUrl, params);
		} else {
			return doGet(rootUrl, mappingUrl, params);
		}
	}

	/**
	 * 
	 * 功能描述：get方式请求
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2018年3月3日 下午4:04:37</p>
	 *
	 * @param rootUrl
	 * @param mappingUrl
	 * @param params
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public static String doGet(String rootUrl, String mappingUrl, Map<String, String> params)
			throws Exception {
		StringBuffer url = new StringBuffer(rootUrl); // 请求地址
		if(mappingUrl!=null && !mappingUrl.trim().equals("")) {
			if (rootUrl.endsWith("/")) {
				url.append(mappingUrl);
			} else {
				url.append("/").append(mappingUrl);
			}
		}		
		if (params != null && !params.isEmpty()) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(params.size());
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
	
	/**
	 * 
	 * 功能描述：post方式请求
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2018年3月3日 下午4:04:25</p>
	 *
	 * @param rootUrl
	 * @param mappingUrl
	 * @param params
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public static String doPost(String rootUrl, String mappingUrl, Map<String, String> params)
			throws Exception {
		StringBuffer url = new StringBuffer(rootUrl); // 请求地址
		if(mappingUrl!=null && !mappingUrl.trim().equals("")) {
			if (rootUrl.endsWith("/")) {
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
	

	public static void main(String[] args) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("expr", "list(\r\n" + 
				"stats(MEDICAL_MZ_ZY_MASTER_INFO,q=\"*:*\",fq=\"{!frange l=0 u=10}YEARAGE\",count(*)),\r\n" + 
				"stats(MEDICAL_MZ_ZY_MASTER_INFO,q=\"*:*\",fq=\"{!frange l=10 u=20}YEARAGE\",count(*)),\r\n" + 
				"stats(MEDICAL_MZ_ZY_MASTER_INFO,q=\"*:*\",fq=\"{!frange l=20 u=30}YEARAGE\",count(*)),\r\n" + 
				"stats(MEDICAL_MZ_ZY_MASTER_INFO,q=\"*:*\",fq=\"{!frange l=30 u=50}YEARAGE\",count(*)),\r\n" + 
				"stats(MEDICAL_MZ_ZY_MASTER_INFO,q=\"*:*\",fq=\"{!frange l=50 u=70}YEARAGE\",count(*)),\r\n" + 
				"stats(MEDICAL_MZ_ZY_MASTER_INFO,q=\"*:*\",fq=\"{!frange l=70 u=100}YEARAGE\",count(*)),\r\n" + 
				"stats(MEDICAL_MZ_ZY_MASTER_INFO,q=\"*:*\",fq=\"{!frange l=100 u=200}YEARAGE\",count(*))\r\n" + 
				")");
		String result = HttpSimulator.callApi("http://10.63.80.131:8983/solr/", "MEDICAL_MZ_ZY_MASTER_INFO/stream", params, "post");
		System.out.println(result);
	}
}
