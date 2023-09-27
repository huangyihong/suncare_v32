/**
 * IEngineSolrStreamService.java	  V1.0   2020年1月2日 下午2:48:25
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.service;

import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;

public interface IEngineSolrStreamService {
	List<QueryResponse> solrQuery(SolrClient solrClient, String[] fq) throws Exception;

}
