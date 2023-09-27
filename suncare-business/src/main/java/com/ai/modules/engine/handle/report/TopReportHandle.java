/**
 * TopReportHandle.java	  V1.0   2022年5月30日 下午4:58:06
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.handle.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.ai.common.utils.MD5Util;
import com.ai.modules.config.entity.MedicalOrgan;
import com.ai.modules.engine.util.ObjectCacheWithFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.json.BucketBasedJsonFacet;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;
import org.apache.solr.client.solrj.response.json.NestableJsonFacet;

import com.ai.modules.engine.model.report.ReportFacetBucketField;
import com.ai.modules.engine.model.report.TopReportParam;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;

@Slf4j
public class TopReportHandle {
	private TopReportParam paramModel;

	public TopReportHandle(TopReportParam paramModel) {
		this.paramModel = paramModel;
	}

	public List<ReportFacetBucketField> singleDimCallSolr() {
		SolrQuery query = new SolrQuery();
		// 设定查询字段
		String q = "*:*";
		query.add("q", q);
		if(paramModel.whereSolrFq()!=null){
			query.addFilterQuery(paramModel.whereSolrFq());
		}
		query.setRows(0);
		JSONObject facetJsonMap = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", "terms");
		jsonObject.put("limit", paramModel.getLimit());
		jsonObject.put("field", paramModel.getGroupBy());
		String sort = "desc";
		if(StringUtils.isNotBlank(paramModel.getSort())) {
			sort = paramModel.getSort();
		}
		String sortField = "count";
		if(StringUtils.isNotBlank(paramModel.getStaFunction())) {
			sortField = "total";
			// 使用聚合函数统计值
			JSONObject subFacteMap = new JSONObject();
			subFacteMap.put("total", paramModel.getStaFunction());
			jsonObject.put("facet", subFacteMap);
		}
		jsonObject.put("sort", sortField.concat(" ").concat(sort));
		facetJsonMap.put("categories", jsonObject);

		//String facetJson = "{categories:{type:terms,limit:20,field:JGMC00,facet:{total:\"unique(TBRID0)\"}}}}";
		String facetJson = JSON.toJSONString(facetJsonMap);
		log.info("======json.facet:"+facetJson);

		//先尝试从缓存获取
		String cacheType= "singleDimCallSolr";
		String cacheName = MD5Util.getMD5(facetJson+paramModel.getLimit()+
				paramModel.getCollection() +SolrUtil.getCurrentDsName());
		int expireSecond = 10 *60; //10分钟
		List<ReportFacetBucketField>  cacheObjectList =(List<ReportFacetBucketField> ) ObjectCacheWithFile.getObjectFromFile(cacheType, cacheName, expireSecond);

		if(cacheObjectList != null) {
			return cacheObjectList;
		}

		query.set("json.facet", facetJson);
		query.setFacet(true);
		query.setFacetLimit(paramModel.getLimit());
		QueryResponse response;
		try {
			String solrCollection = paramModel.getCollection()==null ? EngineUtil.DWB_MASTER_INFO : paramModel.getCollection();

			List<ReportFacetBucketField> result = new ArrayList<ReportFacetBucketField>();
			//logger.info("======solr query: " + URLDecoder.decode(query.toQueryString(), "UTF-8"));
			response = SolrUtil.call(query, solrCollection);
			//logger.info("======solr result:" + response.toString());
			NestableJsonFacet nestableJsonFacet = response.getJsonFacetingResponse();
			BucketBasedJsonFacet bucketBasedJsonFacet = nestableJsonFacet.getBucketBasedFacets("categories");
			if(bucketBasedJsonFacet!=null) {
				List<BucketJsonFacet> bucketJsonFacetList = bucketBasedJsonFacet.getBuckets();
				for(int i=0; i<bucketJsonFacetList.size(); i++) {
					BucketJsonFacet bucket = bucketJsonFacetList.get(i);
	        		if(bucket != null) {
	        			String field = String.valueOf(bucket.getVal());
	        			BigDecimal value = new BigDecimal(bucket.getCount());
	        			if(StringUtils.isNotBlank(paramModel.getStaFunction())) {
	        				BigDecimal decimal = new BigDecimal(bucket.getStatFacetValue("total").toString());
	        				value = decimal.setScale(2, BigDecimal.ROUND_HALF_UP);
	        			}
	        			ReportFacetBucketField facetBucketField = new ReportFacetBucketField(field, value);
	        			result.add(facetBucketField);
	        		}
				}
			}
			//logger.info("======result:"+result);

			//保存缓存
			if(result != null) {
				ObjectCacheWithFile.saveObjectToFile(cacheType,cacheName,result);
			}
			return result;
		} catch (Exception e) {
			log.error("=solr exception=", e);
			throw new RuntimeException("查询失败：" + e.getMessage());
		}
	}
}
