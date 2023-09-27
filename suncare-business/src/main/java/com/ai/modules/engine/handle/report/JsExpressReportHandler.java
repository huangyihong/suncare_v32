/**
 * BaseReportHandler.java	  V1.0   2019年4月11日 下午3:09:45
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.handle.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.json.BucketBasedJsonFacet;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;
import org.apache.solr.client.solrj.response.json.NestableJsonFacet;

import com.ai.modules.engine.model.report.ReportFacetBucketField;
import com.ai.modules.engine.model.report.ReportFacetBucketFieldList;
import com.ai.modules.engine.model.report.ReportParamModel;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 *
 * 功能描述：js算术表达式报表处理器
 *
 * @author  zhangly
 * Date: 2019年4月11日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class JsExpressReportHandler extends MultiReportHandler {

	public JsExpressReportHandler(ReportParamModel paramModel) {
		super(paramModel);
	}

	@Override
	public List<ReportFacetBucketField> singleDimCallSolr() {
		SolrQuery query = new SolrQuery();
		// 设定查询字段
		String q = "*:*";
		query.add("q", q);
		query.addFilterQuery(paramModel.whereSolrFq());
		query.setRows(0);
		JSONObject facetJsonMap = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", "terms");
		jsonObject.put("limit", paramModel.getxLimit());
		jsonObject.put("field", paramModel.getGroupBy()[0]);
		if(StringUtils.isNotBlank(paramModel.getSort())) {
			jsonObject.put("sort", paramModel.getSort());
		}
		if(StringUtils.isNotBlank(paramModel.getStaFunction())) {
			if(StringUtils.isBlank(paramModel.getSort())) {
				jsonObject.put("sort", "total");
			}
			// 使用聚合函数统计值
			JSONObject subFacteMap = new JSONObject();
			String staFunctionStr = paramModel.getStaFunction();
			if('{' == staFunctionStr.charAt(0)){
				JSONObject facetObj = JSON.parseObject(staFunctionStr);
				// 无type属性，则为其余计算参数
				if(facetObj.get("type") == null){
					for(Map.Entry<String, Object> entry: facetObj.entrySet()){
						subFacteMap.put(entry.getKey(),entry.getValue());
					}
				} else {
					subFacteMap.put("obj", facetObj);
				}

			} else {
				subFacteMap.put("total", staFunctionStr);
			}
			jsonObject.put("facet", subFacteMap);
		}
		facetJsonMap.put("categories", jsonObject);

		//String facetJson = "{categories:{type:terms,limit:20,field:JGMC00,facet:{total:\"unique(TBRID0)\"}}}}";
		String facetJson = JSON.toJSONString(facetJsonMap);
		logger.info("=======json.facet:"+facetJson);

		query.set("json.facet", facetJson);
		query.setFacet(true);
		query.setFacetLimit(paramModel.getxLimit());
		QueryResponse response;
		try {
			String solrCollection = paramModel.getSolrCollection()==null ? EngineUtil.YW_JZXX00 : paramModel.getSolrCollection();

			List<ReportFacetBucketField> result = new ArrayList<ReportFacetBucketField>();
			response = SolrUtil.call(query, solrCollection);
			logger.info("======solr result:" + response.toString());
			NestableJsonFacet nestableJsonFacet = response.getJsonFacetingResponse();
			BucketBasedJsonFacet bucketBasedJsonFacet = nestableJsonFacet.getBucketBasedFacets("categories");
			if(bucketBasedJsonFacet!=null) {
				// 加载js引擎
				ScriptEngine jsEngine = null;
				if(StringUtils.isNotBlank(paramModel.getJsCrithExpress())) {
					jsEngine = new ScriptEngineManager().getEngineByName("javascript");
					jsEngine.eval("resolveVal = function(val){ if(val && isFinite(val)){ return val} else {return 0} }");
				}
				List<BucketJsonFacet> bucketJsonFacetList = bucketBasedJsonFacet.getBuckets();
				for(int i=0; i<bucketJsonFacetList.size(); i++) {
					BucketJsonFacet bucket = bucketJsonFacetList.get(i);
	        		if(bucket != null) {
	        			String field = String.valueOf(bucket.getVal());
	        			if(jsEngine==null) {
	        				BigDecimal value = new BigDecimal(bucket.getCount());
		        			if(StringUtils.isNotBlank(paramModel.getStaFunction())) {
		        				BigDecimal decimal = new BigDecimal(bucket.getStatFacetValue("total").toString());
		        				value = decimal.setScale(2, BigDecimal.ROUND_HALF_UP);
		        			}
		        			ReportFacetBucketField facetBucketField = new ReportFacetBucketField(field, value);
		        			result.add(facetBucketField);
	        			} else {
	        				BigDecimal count = new BigDecimal(bucket.getCount());
    	        			jsEngine.put("count", count);
    	        			
    	        			Set<String> keys = bucket.getStatFacetNames();
    	        			for(String key : keys) {
    	        				BigDecimal decimal = new BigDecimal(bucket.getStatFacetValue(key).toString());
    	        				jsEngine.put(key, decimal);
    	        			}    	        			
    	        			Set<String> listKeys = bucket.getQueryFacetNames();
    	        			if(!listKeys.isEmpty()) {    	        				
    	        				for(String key : listKeys) {
    	        					JSONObject jsObject = new JSONObject();
        	        				NestableJsonFacet jsonFacet = bucket.getQueryFacet(key);
        	        				jsObject.put("count", jsonFacet.getCount());
        	        				jsEngine.put(key, jsObject);
        	        			}    	        				
    	        			}    	        			
    	        			
    	        			String express = paramModel.getJsCrithExpress();
    	        			//执行js表达式计算出值
							BigDecimal value = new BigDecimal(String.valueOf(jsEngine.eval("resolveVal(" + express +")")));

        					ReportFacetBucketField facetBucketField = new ReportFacetBucketField(field, value);
        					result.add(facetBucketField);
	        			}
	        		}
				}
			}
			logger.info("======result:"+result);
			return result;
		} catch (Exception e) {
			logger.error("=solr exception=", e);
			throw new RuntimeException("查询失败：" + e.getMessage());
		}
	}

	@Override
	public List<ReportFacetBucketFieldList> multiDimCallSolr() {
		SolrQuery query = new SolrQuery();
		// 设定查询字段
		String q = "*:*";
		query.add("q", q);
		query.addFilterQuery(paramModel.whereSolrFq());
		query.setRows(0);
		JSONObject facetJsonMap = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", "terms");
		jsonObject.put("limit", paramModel.getyLimit());
		jsonObject.put("field", paramModel.getGroupBy()[1]);

		JSONObject jsonObject2 = new JSONObject();
		jsonObject2.put("type", "terms");
		jsonObject2.put("limit", paramModel.getxLimit());
		jsonObject2.put("field", paramModel.getGroupBy()[0]);
		jsonObject2.put("sort", "index");
		if(StringUtils.isNotBlank(paramModel.getStaFunction())) {
			// 使用聚合函数统计值
			JSONObject subFacteMap = new JSONObject();

			String staFunctionStr = paramModel.getStaFunction();
			if('{' == staFunctionStr.charAt(0)){
				JSONObject facetObj = JSON.parseObject(staFunctionStr);
				// 无type属性，则为其余计算参数
				if(facetObj.get("type") == null){
					for(Map.Entry<String, Object> entry: facetObj.entrySet()){
						subFacteMap.put(entry.getKey(),entry.getValue());
					}
				} else {
					subFacteMap.put("obj", facetObj);
				}

			} else {
				subFacteMap.put("total", staFunctionStr);
			}
			jsonObject2.put("facet", subFacteMap);
		}
		JSONObject facetMap = new JSONObject();
		facetMap.put("xAxis", jsonObject2);
		jsonObject.put("facet", facetMap);
		facetJsonMap.put("categories", jsonObject);

		//String facetJson = "{categories:{type:terms,limit:20,field:JGMC00,facet:{total:\"unique(TBRID0)\"}}}}";
		String facetJson = JSON.toJSONString(facetJsonMap);
		logger.info("=======json.facet:"+facetJson);

		query.set("json.facet", facetJson);
		query.setFacet(true);
		query.setFacetLimit(paramModel.getxLimit());

		QueryResponse response;
		try {
			String solrCollection = paramModel.getSolrCollection()==null ? EngineUtil.YW_JZXX00 : paramModel.getSolrCollection();

			List<ReportFacetBucketFieldList> result = new ArrayList<ReportFacetBucketFieldList>();
			response = SolrUtil.call(query, solrCollection);
			logger.info("======solr result:" + response.toString());
			NestableJsonFacet nestableJsonFacet = response.getJsonFacetingResponse();
			BucketBasedJsonFacet bucketBasedJsonFacet = nestableJsonFacet.getBucketBasedFacets("categories");
			if(bucketBasedJsonFacet!=null) {
				// 加载js引擎
				ScriptEngine jsEngine = null;
				if(StringUtils.isNotBlank(paramModel.getJsCrithExpress())) {
					jsEngine = new ScriptEngineManager().getEngineByName("javascript");
					jsEngine.eval("resolveVal = function(val){ if(val && isFinite(val)){ return val} else {return 0} }");
				}
				List<BucketJsonFacet> bucketJsonFacetList = bucketBasedJsonFacet.getBuckets();
				for(int i=0; i<bucketJsonFacetList.size(); i++) {
					BucketJsonFacet bucket = bucketJsonFacetList.get(i);
	        		if(bucket != null) {
	        			ReportFacetBucketFieldList bucketFieldList = new ReportFacetBucketFieldList();
	        			bucketFieldList.setTitle(String.valueOf(bucket.getVal()));
	        			bucketFieldList.setTotal(new BigDecimal(bucket.getCount()));
	        			// 子集buckets
	        			BucketBasedJsonFacet subBucketBasedJsonFacet = bucket.getBucketBasedFacets("xAxis");
	        			if(subBucketBasedJsonFacet!=null) {
	        				List<BucketJsonFacet> subBucketJsonFacetList = subBucketBasedJsonFacet.getBuckets();
	        				List<ReportFacetBucketField> items = new ArrayList<ReportFacetBucketField>();
	        				for(BucketJsonFacet subBucket : subBucketJsonFacetList) {
	        					String field = String.valueOf(subBucket.getVal());
	        					if(jsEngine==null) {
	        						// 未配置js计算表达式直接取值
	    	        				BigDecimal value = new BigDecimal(subBucket.getCount());
		    	        			if(StringUtils.isNotBlank(paramModel.getStaFunction())) {
		    	        				BigDecimal decimal = new BigDecimal(subBucket.getStatFacetValue("total").toString());
		    	        				value = decimal.setScale(2, BigDecimal.ROUND_HALF_UP);
		    	        			}
		        					ReportFacetBucketField facetBucketField = new ReportFacetBucketField(field, value);
	        						items.add(facetBucketField);
	    	        			} else {
	    	        				// js表达式计算
	    	        				BigDecimal count = new BigDecimal(subBucket.getCount());

		    	        			jsEngine.put("count", count);
		    	        			Set<String> keys = subBucket.getStatFacetNames();
		    	        			for(String key : keys) {
		    	        				BigDecimal decimal = new BigDecimal(subBucket.getStatFacetValue(key).toString());
		    	        				jsEngine.put(key, decimal);
		    	        			}
									Set<String> listKeys = subBucket.getBucketBasedFacetNames();
									for(String key : listKeys) {
										BucketBasedJsonFacet basedJsonFacet = subBucket.getBucketBasedFacets(key);
										JSONObject bucketJsonObj = bucketJsonFacetTOJson(basedJsonFacet, jsEngine);
										jsEngine.put(key, bucketJsonObj);
									}
		    	        			String express = paramModel.getJsCrithExpress();
		    	        			//执行js表达式计算出值
									BigDecimal value = new BigDecimal(String.valueOf(jsEngine.eval("resolveVal(" + express +")")));

		        					ReportFacetBucketField facetBucketField = new ReportFacetBucketField(field, value);
	        						items.add(facetBucketField);
	    	        			}
	        				}
	        				bucketFieldList.setBuckets(items);
	        			}
	        			result.add(bucketFieldList);
	        		}
				}
			}

			logger.info("======result:"+result);
			return result;
		} catch (Exception e) {
			logger.error("=solr exception=", e);
			throw new RuntimeException("查询失败：" + e.getMessage());
		}
	}

	private JSONObject bucketJsonFacetTOJson(BucketBasedJsonFacet basedJsonFacet, ScriptEngine jsEngine){
		JSONObject jsonObject = new JSONObject();
		for(BucketJsonFacet bucketJsonFacet: basedJsonFacet.getBuckets()){
			Object bucketVal = bucketJsonFacet.getVal();
			jsonObject.put(bucketVal == null?"count":String.valueOf(bucketVal),bucketJsonFacet.getCount());
			// 其他信息
			Set<String> keys = bucketJsonFacet.getStatFacetNames();
			for(String key : keys) {
				BigDecimal decimal = new BigDecimal(bucketJsonFacet.getStatFacetValue(key).toString());
				jsonObject.put(key, decimal);
			}
			// 继续钻取
			Set<String> basedFacetNames = bucketJsonFacet.getBucketBasedFacetNames();
			List<JSONObject> list = new ArrayList<>();
			for(String name: basedFacetNames){
				BucketBasedJsonFacet jsonFacet = bucketJsonFacet.getBucketBasedFacets(name);
				list.add(bucketJsonFacetTOJson(jsonFacet,jsEngine));
			}
			if(list.size() > 0){
				jsonObject.put("list", list);
			}

		}

		return jsonObject;
	}
}
