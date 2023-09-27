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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.json.BucketBasedJsonFacet;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;
import org.apache.solr.client.solrj.response.json.NestableJsonFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ai.modules.engine.model.report.ReportEchartsEntity;
import com.ai.modules.engine.model.report.ReportFacetBucketField;
import com.ai.modules.engine.model.report.ReportFacetBucketFieldList;
import com.ai.modules.engine.model.report.ReportParamModel;
import com.ai.modules.engine.model.report.StatisticsReportModel;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/***
 *
 * 功能描述：数据加工抽象类
 *
 * @author  zhangly
 * Date: 2019年4月11日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public abstract class AbstractReportHandler {
	protected Logger logger = LoggerFactory.getLogger(AbstractReportHandler.class);

	protected ReportParamModel paramModel;

	public AbstractReportHandler(ReportParamModel paramModel) {
		this.paramModel = paramModel;
	}

	/**
	 *
	 * 功能描述：根据读取solr的数据，封装成渲染前台页面需要的对象
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年4月11日 下午3:19:02</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public abstract StatisticsReportModel handle();

	public StatisticsReportModel render(ReportEchartsEntity echartsData) {
		StatisticsReportModel reportModel = new StatisticsReportModel(echartsData, paramModel.getCurrentEchart().toLowerCase(), paramModel.getDimDict());
		return reportModel.render();
	}		

	/**
	 *
	 * 功能描述：单维度
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年4月11日 下午3:22:10</p>
	 *
	 * @param fq
	 * @param facetPivot
	 * @param limit
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
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
			subFacteMap.put("total", paramModel.getStaFunction());
			jsonObject.put("facet", subFacteMap);
		}
		facetJsonMap.put("categories", jsonObject);

		//String facetJson = "{categories:{type:terms,limit:20,field:JGMC00,facet:{total:\"unique(TBRID0)\"}}}}";
		String facetJson = JSON.toJSONString(facetJsonMap);
		logger.info("======json.facet:"+facetJson);

		query.set("json.facet", facetJson);
		query.setFacet(true);
		query.setFacetLimit(paramModel.getxLimit());
		QueryResponse response;
		try {
			String solrCollection = paramModel.getSolrCollection()==null ? EngineUtil.YW_JZXX00 : paramModel.getSolrCollection();

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
			return result;
		} catch (Exception e) {
			logger.error("=solr exception=", e);
			throw new RuntimeException("查询失败：" + e.getMessage());
		}
	}

	/**
	 *
	 * 功能描述：二维度
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年4月11日 下午3:23:47</p>
	 *
	 * @param fq
	 * @param facetPivot
	 * @param limit
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
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
			subFacteMap.put("total", paramModel.getStaFunction());
			jsonObject2.put("facet", subFacteMap);
		}
		JSONObject facetMap = new JSONObject();
		facetMap.put("xAxis", jsonObject2);
		jsonObject.put("facet", facetMap);
		facetJsonMap.put("categories", jsonObject);

		//String facetJson = "{categories:{type:terms,limit:20,field:JGMC00,facet:{total:\"unique(TBRID0)\"}}}}";
		String facetJson = JSON.toJSONString(facetJsonMap);
		logger.info("======json.facet:"+facetJson);

		query.set("json.facet", facetJson);
		query.setFacet(true);
		query.setFacetLimit(paramModel.getxLimit());

		QueryResponse response;
		try {
			String solrCollection = paramModel.getSolrCollection()==null ? EngineUtil.YW_JZXX00 : paramModel.getSolrCollection();

			List<ReportFacetBucketFieldList> result = new ArrayList<ReportFacetBucketFieldList>();
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
	    	        			BigDecimal value = new BigDecimal(subBucket.getCount());
	    	        			if(StringUtils.isNotBlank(paramModel.getStaFunction())) {
	    	        				BigDecimal decimal = new BigDecimal(subBucket.getStatFacetValue("total").toString());
	    	        				value = decimal.setScale(2, BigDecimal.ROUND_HALF_UP);
	    	        			}
	        					ReportFacetBucketField facetBucketField = new ReportFacetBucketField(field, value);
        						items.add(facetBucketField);
	        				}
	        				bucketFieldList.setBuckets(items);
	        			}
	        			result.add(bucketFieldList);
	        		}
				}
			}

			//logger.info("======result:"+result);
			return result;
		} catch (Exception e) {
			logger.error("=solr exception=", e);
			throw new RuntimeException("查询失败：" + e.getMessage());
		}
	}
	
	/**
	 * 
	 * 功能描述：遇到数据不齐补0
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年4月12日 上午11:22:35</p>
	 *
	 * @param src
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected List<ReportFacetBucketFieldList> repair(List<ReportFacetBucketFieldList> recordList, Set<String> axis) {
		boolean validate = true;
		// 数据不一致行坐标
		Set<Integer> differ = new HashSet<Integer>();
		int i=0;
		for(ReportFacetBucketFieldList bucketList : recordList) {
			//logger.info("pivotList:size:"+pivotList.size()+"--"+pivotList);
			if(bucketList.size()<axis.size()) {
				validate = validate && false;
				differ.add(i);
			}
			i++;
		}
		if(validate) {
			return recordList;
		}
		
		// 遍历不一致行，进行补零处理
		for(Integer index : differ) {
			ReportFacetBucketFieldList bucketList = recordList.get(index);
			for(String key : axis) {
				boolean find = false;
				for(ReportFacetBucketField bucket : bucketList.getBuckets()) {
					if(key.equals(bucket.getField())) {
						find = true;
						break;
					}					
				}
				if(find==false) {
					//进行补零
					ReportFacetBucketField zero = new ReportFacetBucketField(key, new BigDecimal(0));
					bucketList.getBuckets().add(zero);
				}					
			}
			//重新按field排序
			Collections.sort(bucketList.getBuckets(), new Comparator<ReportFacetBucketField>() {
				@Override
				public int compare(ReportFacetBucketField o1, ReportFacetBucketField o2) {
					return o1.getField().compareTo(o2.getField());
				}
				
			});
		}
		return recordList;
	}
	
	/**
	 * 
	 * 功能描述：遇到数据不齐补0
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年9月10日 下午3:27:39</p>
	 *
	 * @param recordList
	 * @param axis
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public List<ReportFacetBucketField> repairZero(List<ReportFacetBucketField> recordList, Set<String> axis) {
		boolean validate = true;
		if(recordList.size()<axis.size()) {
			validate = validate && false;
		}
		if(validate) {
			return recordList;
		}
		
		List<ReportFacetBucketField> zeroList = new ArrayList<ReportFacetBucketField>();
		// 遍历进行补零处理
		for(String key : axis) {
			boolean find = false;
			for(ReportFacetBucketField bucket : recordList) {
				if(key.equals(bucket.getField())) {
					find = true;
					break;
				}					
			}
			if(find==false) {
				//进行补零
				ReportFacetBucketField zero = new ReportFacetBucketField(key, BigDecimal.ZERO);
				zeroList.add(zero);
			}					
		}
		recordList.addAll(zeroList);
		//重新按field排序
		Collections.sort(recordList, new Comparator<ReportFacetBucketField>() {
			@Override
			public int compare(ReportFacetBucketField o1, ReportFacetBucketField o2) {
				return o1.getField().compareTo(o2.getField());
			}
			
		});
		return recordList;
	}

	public ReportParamModel getParamModel() {
		return paramModel;
	}

	public void setParamModel(ReportParamModel paramModel) {
		this.paramModel = paramModel;
	}
}
