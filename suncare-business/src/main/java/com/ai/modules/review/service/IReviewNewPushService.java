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

import com.ai.modules.review.entity.NewV3Tmp;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.web.multipart.MultipartFile;

public interface IReviewNewPushService {

    void pushBatchBySolr(SolrQuery solrQuery, NewV3Tmp tmpBean) throws Exception;

    void exportActionSolrMain(String batchId, boolean isProject) throws Exception;

    String importMedicalUnreasonableAction(MultipartFile file, TaskProjectBatch taskProjectBatch) throws Exception;

}
