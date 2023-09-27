/**
 * EngineService.java	  V1.0   2019年11月29日 上午11:05:59
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.review.service;

import com.ai.modules.review.vo.ReviewSystemDrugViewVo;
import org.apache.solr.client.solrj.SolrQuery;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IReviewSystemService {


	void operateReviewCaseId(String[] ids, String caseId, String action) throws Exception;

    void exportDrugList(SolrQuery solrQuery, String collection, Map<String, String> fieldMap, String title,String ruleType, OutputStream os) throws Exception;
}
