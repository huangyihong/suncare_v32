/**
 * java	  V1.0   2020年12月15日 下午8:43:07
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.api.util;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
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
import org.jeecg.common.shiro.vo.DefContants;
import org.jeecg.common.util.DateUtils;

import com.ai.modules.api.ApiOauthInfo;
import com.ai.modules.api.rsp.ApiResponse;
import com.alibaba.fastjson.JSON;

public class ApiOauthUtil {
	private static Properties prop = new Properties();
	/**接口调用根路径*/
	public static String API_URL;
	/**鉴权key*/
	public static String API_APP_KEY;
	/**鉴权私钥*/
	public static String API_SECRET;
	public static final Set<String> API_PUBLIC_PARAM = new HashSet<String>();
	private static final CloseableHttpClient httpClient;
	private static final String CHARSET = "UTF-8";
	private static final String TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2MDgwODM5NDAsImRhdGFTb3VyY2UiOiJoZXplIiwidXNlcm5hbWUiOiJ6aGFuZ2x5In0.tyX_KQ9cjPn5c2etd6xjalUTtBY9qIMObC2K-2wLcD4";

	static {
		try {
			prop.load(ApiOauthUtil.class.getClassLoader().getResourceAsStream("config.properties"));
			API_URL = prop.getProperty("api.url");
			API_APP_KEY = prop.getProperty("api.appKey");
			API_SECRET = prop.getProperty("api.appSecret");
		} catch(Throwable e) {
			e.printStackTrace();
		}
		API_PUBLIC_PARAM.add("method");
		API_PUBLIC_PARAM.add("timestamp");
		API_PUBLIC_PARAM.add("appKey");
		API_PUBLIC_PARAM.add("v");
		API_PUBLIC_PARAM.add("sign");
		API_PUBLIC_PARAM.add("session");

		RequestConfig config = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build();
		httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	}

	/*@Value("${engine.api.url}")
	public void url(String value) {
		API_URL = value;
	}

	@Value("${engine.api.appKey}")
	public void appKey(String value) {
		API_APP_KEY = value;
	}

	@Value("${engine.api.appSecret}")
	public void appSecret(String value) {
		API_SECRET = value;
	}*/

	public static String callApi(String rootUrl, String mappingUrl, Map<String, String> params, String type, boolean mustToken) throws Exception {
		type = type==null ? "post" : type;
		if ("post".equalsIgnoreCase(type)) {
			return doPost(rootUrl, mappingUrl, params, mustToken);
		} else {
			return doGet(rootUrl, mappingUrl, params, mustToken);
		}
	}

	public static String callApi(String rootUrl, String mappingUrl, Map<String, String> params, String type) throws Exception {
		return callApi(rootUrl, mappingUrl, params, type, false);
	}


	/**
	 * 调用指定网页，将结果直接写向指定流
	 * @param mappingUrl
	 * @param busiParams
	 */
	public static void writeResultToStream(String mappingUrl, Map<String,
			String> busiParams,OutputStream outStream) {
		ApiOauthInfo oauth = new ApiOauthInfo();
		oauth.setAppKey(API_APP_KEY);
		oauth.setSecret(API_SECRET);
		oauth.setTimestamp(DateUtils.getDate("yyyy-MM-dd HH:mm:ss"));
		oauth.setV("2.0");
		Map<String, String> params = oauth.buildMap(busiParams);
		if(busiParams!=null) {
			params.putAll(busiParams);
		}

		try {
			doPostToStream(ApiTokenUtil.API_URL, mappingUrl, params,
					null , outStream);
		} catch(Exception e) {
			throw new RuntimeException(e);
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
	public static String doGet(String rootUrl, String mappingUrl, Map<String, String> params, boolean mustToken)
			throws Exception {
		StringBuffer url = new StringBuffer(rootUrl); // 请求地址
		if(mappingUrl!=null && !mappingUrl.trim().equals("")) {
			if (mappingUrl.startsWith("/")) {
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
		if(mustToken) {
			httpGet.setHeader(DefContants.X_ACCESS_TOKEN, TOKEN);
        }
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
	public static String doPost(String rootUrl, String mappingUrl, Map<String, String> params, boolean mustToken)
			throws Exception {
		StringBuffer url = new StringBuffer(rootUrl); // 请求地址
		if(mappingUrl!=null && !mappingUrl.trim().equals("")) {
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
        if(mustToken) {
        	httpPost.setHeader(DefContants.X_ACCESS_TOKEN, TOKEN);
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

	/**
	 *
	 * 功能描述：post方式请求，将结果写到指定流
	 *
	 */
	public static void doPostToStream(String rootUrl, String mappingUrl, Map<String, String> params,
			String token ,OutputStream outStream)
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
		if(token != null && token.length() > 0) {
			httpPost.setHeader(DefContants.X_ACCESS_TOKEN, token);
		}
		CloseableHttpResponse response = httpClient.execute(httpPost);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			httpPost.abort();
			throw new RuntimeException("HttpClient,error status code :" + statusCode);
		}
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			entity.writeTo(outStream);
		}
		EntityUtils.consume(entity);
		response.close();
	}

	/**
	 *
	 * 功能描述：调用鉴权的接口
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月10日 下午3:10:59</p>
	 *
	 * @param rootUrl
	 * @param mappingUrl
	 * @param busiParams
	 * @param type
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public static String callOauthApi(String rootUrl, String mappingUrl, Map<String, String> busiParams, String type, boolean mustToken) throws Exception {
		type = type==null ? "post" : type;
		ApiOauthInfo oauth = new ApiOauthInfo();
		oauth.setAppKey(API_APP_KEY);
		oauth.setSecret(API_SECRET);
		oauth.setTimestamp(DateUtils.getDate("yyyy-MM-dd HH:mm:ss"));
		oauth.setV("2.0");
		Map<String, String> params = oauth.buildMap(busiParams);
		if(busiParams!=null) {
			params.putAll(busiParams);
		}
		if ("post".equalsIgnoreCase(type)) {
			return doPost(rootUrl, mappingUrl, params, mustToken);
		} else {
			return doGet(rootUrl, mappingUrl, params, mustToken);
		}
	}

	public static String postApi(String mappingUrl, Map<String, String> busiParams) throws Exception {
		return callOauthApi(API_URL, mappingUrl, busiParams, "post", false);
	}

	public static String getApi(String mappingUrl, Map<String, String> busiParams) throws Exception {
		return callOauthApi(API_URL, mappingUrl, busiParams, "get", false);
	}

	public static boolean postSuccess(String mappingUrl, Map<String, String> busiParams) {
		try {
			String text = postApi(mappingUrl, busiParams);
			ApiResponse<?> result = JSON.parseObject(text, ApiResponse.class);
			if(!result.isSuccess()) {
				throw new RuntimeException(result.getMessage());
			}
			return true;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static ApiResponse<?> response(String mappingUrl, Map<String, String> busiParams, String type) {
		try {
			String text = null;
			if ("post".equalsIgnoreCase(type)) {
				text = postApi(mappingUrl, busiParams);
			} else {
				text = getApi(mappingUrl, busiParams);
			}
			ApiResponse<?> result = JSON.parseObject(text, ApiResponse.class);
			return result;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static ApiResponse<?> response(String mappingUrl, Map<String, String> busiParams) {
		try {
			String text = postApi(mappingUrl, busiParams);
			ApiResponse<?> result = JSON.parseObject(text, ApiResponse.class);
			return result;
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
		try {
			if(clazz.equals(String.class)
					|| clazz.equals(Integer.TYPE)
					|| clazz.equals(Long.TYPE)
					|| clazz.equals(Float.TYPE)
					|| clazz.equals(Double.TYPE)) {
				Constructor<T> ct = clazz.getDeclaredConstructor(new Class[]{String.class});
				T result = ct.newInstance(new Object[] {text});
				return result;
			}
		} catch (Exception e) {}

		return JSON.parseObject(text, clazz);
	}

	public static <T> List<T> responseArray(String mappingUrl, Map<String, String> busiParams, String type, Class<T> clazz) {
		ApiResponse<?> rsp = response(mappingUrl, busiParams, type);
		if(!rsp.isSuccess()) {
			throw new RuntimeException(rsp.getMessage());
		}
		return parseArray(rsp, clazz);
	}

	public static <T> T response(String mappingUrl, Map<String, String> busiParams, String type, Class<T> clazz) {
		ApiResponse<?> rsp = response(mappingUrl, busiParams, type);
		if(!rsp.isSuccess()) {
			throw new RuntimeException(rsp.getMessage());
		}
		return parseObject(rsp, clazz);
	}

	public static String getProperty(String key) {
		return prop.getProperty(key);
	}
}
