package com.ai.modules.review.service;

import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Auther: zhangpeng
 * @Date: 2021/5/20 17
 * @Description:
 */
public interface IReviewNewSecService {
    String importReviewStatus(MultipartFile file, MedicalUnreasonableActionVo searchObj) throws Exception;

    String importGroupReviewStatus(MultipartFile file, MedicalUnreasonableActionVo searchObj, SolrQuery solrQuery) throws Exception;
}
