package com.ai.modules.review.service;

import com.ai.modules.task.entity.TaskProject;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.solr.client.solrj.SolrQuery;

import java.io.OutputStream;

/**
 * @Auther: zhangpeng
 * @Date: 2021/7/21 09
 * @Description:
 */
public interface IReviewStatisticNewService {
    JSONObject module0Data(SolrQuery solrQuery, String collection) throws Exception;

    JSONObject module0MasterInfoData(TaskProject project) throws Exception;

    JSONObject module0ChargeDetailData(TaskProject project) throws Exception;

    JSONArray module1Data(SolrQuery solrQuery, String collection) throws Exception;

//    JSONObject module2Data(SolrQuery solrQuery, String collection, String secReviewStatus) throws Exception;
    JSONArray module2Data(SolrQuery solrQuery, String collection, String groupBy) throws Exception;
    JSONArray module2ExportData(SolrQuery solrQuery, String collection, String groupBy, String statusField) throws Exception;

    JSONArray module3Data(SolrQuery solrQuery, String collection, String secReviewStatus) throws Exception;

    JSONArray module4Data(SolrQuery solrQuery, String collection) throws Exception;;

    JSONArray module5Data(SolrQuery solrQuery, String collection, String secReviewStatus) throws Exception;;
    JSONArray module5ExportData(SolrQuery solrQuery, String collection) throws Exception;;

    void export(JSONObject jsonObject, String[] titles, String[] fields, String title, OutputStream os) throws Exception;

    void export(JSONArray jsonArray, String[] titles, String[] fields, String title, OutputStream os) throws Exception;

    JSONArray module6Data(SolrQuery solrQuery, String collection, String groupBy) throws Exception;

    JSONArray module6ExportData(SolrQuery solrQuery, String collection, String groupBy) throws Exception;
}
