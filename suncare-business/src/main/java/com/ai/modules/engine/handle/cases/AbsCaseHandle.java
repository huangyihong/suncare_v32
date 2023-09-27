/**
 * AbsCaseHandle.java	  V1.0   2022年12月6日 上午10:59:04
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases;

import java.math.BigDecimal;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.json.BucketBasedJsonFacet;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;
import org.apache.solr.client.solrj.response.json.NestableJsonFacet;
import org.jeecg.common.util.DateUtils;

import com.ai.common.MedicalConstant;
import com.ai.modules.engine.model.EngineResult;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.date.DateUtil;

public abstract class AbsCaseHandle {

	//数据来源
	protected String datasource;
	//任务
	protected TaskProject task;
	//任务批次
	protected TaskProjectBatch batch;
	//模型
	protected HisMedicalFormalCase formalCase;
	
	public AbsCaseHandle(String datasource, TaskProject task, TaskProjectBatch batch, HisMedicalFormalCase formalCase) {
		this.datasource = datasource;
		this.task = task;
		this.batch = batch;
		this.formalCase = formalCase;
	}
	
	/**
	 * 
	 * 功能描述：计算引擎入口
	 *
	 * @author  zhangly
	 *
	 * @return
	 * @throws Exception
	 */
	public abstract EngineResult generateUnreasonableAction() throws Exception;
	
	/**
	 * 
	 * 功能描述：模型是否忽略执行
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	protected boolean ignoreRun() {
		String project_startTime = MedicalConstant.DEFAULT_START_TIME;
        String project_endTime = MedicalConstant.DEFAULT_END_TIME;
        String batch_startTime = MedicalConstant.DEFAULT_START_TIME;
        String batch_endTime = MedicalConstant.DEFAULT_END_TIME;
        String case_startTime = MedicalConstant.DEFAULT_START_TIME;
        String case_endTime = MedicalConstant.DEFAULT_END_TIME;
        project_startTime = task.getDataStartTime()!=null ? DateUtil.format(task.getDataStartTime(), "yyyy-MM-dd") : project_startTime;
        project_endTime = task.getDataEndTime()!=null ? DateUtil.format(task.getDataEndTime(), "yyyy-MM-dd") : project_endTime;
        batch_startTime = batch.getStartTime()!=null ? DateUtil.format(batch.getStartTime(), "yyyy-MM-dd") : batch_startTime;
        batch_endTime = batch.getEndTime()!=null ? DateUtil.format(batch.getEndTime(), "yyyy-MM-dd") : batch_endTime;
        case_startTime = formalCase.getStartTime()!=null ? DateUtils.formatDate(formalCase.getStartTime(), "yyyy-MM-dd") : case_startTime;
        case_endTime = formalCase.getEndTime()!=null ? DateUtils.formatDate(formalCase.getEndTime(), "yyyy-MM-dd") : case_endTime;
        if(project_endTime.compareTo(case_startTime)<0 || project_startTime.compareTo(case_endTime)>0) {
        	//模型有效期不在批次时间范围内
            return true;
        }
        if(batch_endTime.compareTo(case_startTime)<0 || batch_startTime.compareTo(case_endTime)>0) {
        	//模型有效期不在批次时间范围内
        	return true;
        }
		return false;
	}
	
	/**
	 * 
	 * 功能描述：统计模型病例数、违规金额等
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月17日 下午12:22:05</p>
	 *
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected EngineResult computeMoney() throws Exception {
		EngineResult result = EngineResult.ok();
        
		SolrQuery query = new SolrQuery();
		// 设定查询字段
		String q = "*:*";
		query.add("q", q);
		query.addFilterQuery("BATCH_ID:"+batch.getBatchId());
		query.addFilterQuery("CASE_ID:"+formalCase.getCaseId());
		query.setRows(0);
		JSONObject facetJsonMap = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", "terms");
		jsonObject.put("limit", 1);
		jsonObject.put("field", "CASE_ID");
		// 使用聚合函数统计值
		JSONObject subFacteMap = new JSONObject();
		//就诊金额
		subFacteMap.put("totalfee", "sum(TOTALFEE)");
		//违规金额
		subFacteMap.put("minmoney", "sum(MIN_MONEY)");
		//违规基金支出金额
		subFacteMap.put("actionmoney", "sum(ACTION_MONEY)");
		jsonObject.put("facet", subFacteMap);
		facetJsonMap.put("categories", jsonObject);
		String facetJson = JSON.toJSONString(facetJsonMap);
		query.set("json.facet", facetJson);
		query.setFacet(true);
		query.setFacetLimit(1);
		QueryResponse response = SolrUtil.call(query, EngineUtil.MEDICAL_UNREASONABLE_ACTION);
		NestableJsonFacet nestableJsonFacet = response.getJsonFacetingResponse();
		BucketBasedJsonFacet bucketBasedJsonFacet = nestableJsonFacet.getBucketBasedFacets("categories");
		if(bucketBasedJsonFacet!=null) {
			List<BucketJsonFacet> bucketJsonFacetList = bucketBasedJsonFacet.getBuckets();
			for(int i=0; i<bucketJsonFacetList.size(); i++) {
				BucketJsonFacet bucket = bucketJsonFacetList.get(i);
        		if(bucket != null) {
        			BigDecimal count = new BigDecimal(bucket.getCount());
        			BigDecimal totalfee = new BigDecimal(bucket.getStatFacetValue("totalfee").toString());
        			totalfee = totalfee.setScale(2, BigDecimal.ROUND_HALF_UP);
        			BigDecimal fundcover = new BigDecimal(bucket.getStatFacetValue("minmoney").toString());
        			fundcover = fundcover.setScale(2, BigDecimal.ROUND_HALF_UP);
        			
        			result.setCount(count.intValue());
        			result.setMoney(totalfee);
        			result.setActionMoney(fundcover);
        		}
			}
		}
		return result;
	}
}
