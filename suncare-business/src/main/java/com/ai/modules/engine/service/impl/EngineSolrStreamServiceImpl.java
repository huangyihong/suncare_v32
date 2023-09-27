/**
 * EngineSolrStreamServiceImpl.java	  V1.0   2020年1月2日 下午2:48:01
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.service.impl;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.InputStreamResponseParser;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ai.modules.engine.service.IEngineSolrStreamService;
import com.ai.modules.engine.util.EngineUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EngineSolrStreamServiceImpl implements IEngineSolrStreamService {
	@Value("${solr.datasource.dynamic.druid.maxRow:2000000}")
    private int maxRow;
	@Override
	public List<QueryResponse> solrQuery(SolrClient solrClient, String[] fq) throws Exception {
		//是否需要分页查询
		boolean pager = maxRow<0 ? true : false;
		int pageSize = pager ? EngineUtil.MAX_ROW : maxRow;
		SolrQuery solrQuery = new SolrQuery();
		// 设定查询字段
		String q = "*:*";
		solrQuery.add("q", q);
		solrQuery.addFilterQuery(fq);
		solrQuery.setStart(0);
		solrQuery.setRows(pageSize);
//        solrQuery.setRequestHandler("/export");
        solrQuery.setRequestHandler("/query");
        log.info("======solr query: " + URLDecoder.decode(solrQuery.toQueryString(), "UTF-8"));

		List<QueryResponse> result = new ArrayList<QueryResponse>();

		QueryRequest request = new QueryRequest(solrQuery);
		request.setResponseParser(new InputStreamResponseParser("json"));
		request.setMethod(SolrRequest.METHOD.POST);

		QueryResponse response = request.process(solrClient);
		result.add(response);
		if(!pager) {
			return result;
		}

		long total = getNumFound(solrClient, fq);
		if(total==0) {
			return null;
		}
		if(total<=pageSize) {
			return result;
		}
		// 查询结果记录条数超过每页记录数
		long pageTotal = (total+(pageSize-1)) / pageSize;
		for(int pageNum=2; pageNum<=pageTotal; pageNum++) {
			QueryResponse queryResponse = this.solrQuery(solrClient, fq, pageNum);
			result.add(queryResponse);
		}
		return result;
	}

	private long getNumFound(SolrClient solrClient, String[] fq) throws Exception {
		SolrQuery solrQuery = new SolrQuery();
		String q = "*:*";
		solrQuery.add("q", q);
		solrQuery.addFilterQuery(fq);
		solrQuery.setStart(0);
		solrQuery.setRows(10);
		QueryResponse response = solrClient.query(solrQuery, SolrRequest.METHOD.POST);
		SolrDocumentList documents = response.getResults();
		return documents.getNumFound();
	}

	private QueryResponse solrQuery(SolrClient solrClient, String[] fq, int pageNum) throws Exception {
		int pageSize = EngineUtil.MAX_ROW;
		int offset = pageNum > 0 ? (pageNum - 1) * pageSize : 0;
		SolrQuery solrQuery = new SolrQuery();
		// 设定查询字段
		String q = "*:*";
		solrQuery.add("q", q);
		solrQuery.addFilterQuery(fq);
		solrQuery.setStart(offset);
		solrQuery.setRows(pageSize);
        solrQuery.setRequestHandler("/export");
        log.info("======solr query: " + solrQuery.toString());
        QueryResponse response = solrClient.query(solrQuery, SolrRequest.METHOD.POST);
		return response;
	}
}
