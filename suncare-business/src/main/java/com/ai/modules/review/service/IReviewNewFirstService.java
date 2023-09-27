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

import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface IReviewNewFirstService {


    void pushBatchByCaseId(String collection, SolrQuery solrQuery, JSONObject commonDoc, boolean isPush) throws Exception;

    void pushRecordByIds(List<String> ids, SolrInputDocument commonDoc) throws Exception;

    void pushRecord(SolrQuery solrQuery, JSONObject commonDoc) throws Exception;

    int pushRecord(SolrQuery solrQuery, JSONObject commonDoc, Consumer<Integer> updateProcess) throws Exception;


    void copyRecordPropByIds(List<String> ids, JSONObject commonDoc, Map<String, String> flMap) throws Exception;

    void copyRecordProp(SolrQuery solrQuery, JSONObject commonDoc, Map<String, String> flMap) throws Exception;

    void copyRecordProp(SolrQuery solrQuery, JSONObject commonDoc, Map<String, String> flMap, Consumer<Integer> updateProcess) throws Exception;

    String importReviewStatus(MultipartFile file, MedicalUnreasonableActionVo searchObj) throws Exception;

    String importGroupReviewStatus(MultipartFile file, MedicalUnreasonableActionVo searchObj, SolrQuery solrQuery) throws Exception;

    void exportDrugList(SolrQuery solrQuery, String collection, Map<String, String> fieldMap, String title, String ruleType, OutputStream os) throws Exception;

    void exportClinicalList(SolrQuery solrQuery, String collection, Map<String, String> fieldMap, String title, String ruleType, OutputStream os) throws Exception;
}
