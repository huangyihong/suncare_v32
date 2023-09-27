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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.ai.modules.review.dto.DynamicFieldConfig;
import com.ai.modules.review.vo.*;
import com.ai.modules.task.entity.TaskActionFieldCol;
import com.alibaba.fastjson.JSONArray;
import jxl.write.WritableSheet;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jeecg.common.system.vo.LoginUser;

import com.ai.modules.engine.model.vo.MedicalCaseVO;
import com.ai.modules.task.entity.TaskBatchBreakRule;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface IReviewService {

	IPage<SolrDocument> pageDynamicResult(Map<String, Set<String>> tabFieldMap, SolrQuery solrQuery, String collection, Page<SolrDocument> page) throws Exception;

	void dynamicResultExport(String[] fields, String[] fieldTitles,Map<String, Set<String>> tabFieldMap, SolrQuery solrQuery, String collection, boolean isStep2, OutputStream os) throws Exception;

	/**
	 * 保存客户审查信息
	 * @param obj
	 * @param user
	 * @throws Exception
	 */
	void saveCustomReview(JSONObject obj, LoginUser user)throws Exception;

	/**
	 * 通过visitid查询就诊信息DWB_MASTER_INFO
	 * @param visitid
	 * @return
	 */
	DwbMasterInfoVo getDwbMasterInfoByVisitidBySolr(String visitid) throws Exception;

	/**
	 * 通过clientid查询病人信息DWB_CLIENT
	 * @param clientid
	 * @return
	 */
	DwbClientVo getDwbClientByClientidBySolr(String clientid) throws Exception;

    List<DwbClientVo> getDwbClientByClientidsBySolr(List<String> clientids) throws Exception;

    /**
	 * 通过orgid查询医院信息DWB_ORGANIZATION
	 * @param orgid
	 * @return
	 * @throws Exception
	 */
	DwbOrganizationVo getDwbOrganizationByOrgidBySolr(String orgid) throws Exception;

	/**
	 * 通过doctorid查询医院信息DWB_DOCTOR
	 * @param doctorid
	 * @return
	 */
	DwbDoctorVo getDwbDoctorByDoctoridBySolr(String doctorid) throws Exception;

	List<DwbDoctorVo> getDwbDoctorByDoctoridsBySolr(List<String> doctorids) throws Exception;

	void dynamicResultExport(DynamicFieldConfig fieldConfig, SolrQuery solrQuery, String collection, Boolean isStep2, OutputStream os) throws Exception;

	/**
	 *  新增不合规行为（手工录入）
	 * @param bean
	 */
//	void saveMedicalUnreasonableAction(MedicalUnreasonableActionVo bean) throws Exception;

    void dynamicResultExport(List<TaskActionFieldCol> colList, SolrQuery solrQuery, String collection, Boolean isStep2, OutputStream os) throws Exception;

    /**
	 * 保存推送信息
	 * @param obj
	 * @param user
	 * @throws Exception
	 */
    void saveReviews(JSONObject obj, LoginUser user) throws Exception;

	/**
	 * 导出不合规病例
	 * @param solrQuerys
	 * @param os
	 */
	void exportExcel(SolrQuery[] solrQuerys, OutputStream os) throws Exception;

    void exportClientMasterInfo(String visitidParam, WritableSheet sheet) throws Exception;


    void dynamicGroupExport(String collection, SolrQuery solrQuery, String[] colOrders,  Map<String, Set<String>> tabFieldMap
            , List<String> groupByList, boolean isGroupActionName, List<String> facetFields, Set<String> linkFields
            , String[] fieldTitles, String[] fields, List<OutputStream> osList) throws Exception;

    void dynamicGroupMultiTableExport(String collection, SolrQuery solrQuery, String[] colOrders, Map<String, Set<String>> tabFieldMap
            , List<String> groupByList, List<String> facetFields, Set<String> linkFields
            , String[] fieldTitles, String[] fields, List<OutputStream> osList) throws Exception;

    void dynamicResultExport(String[] fields, String[] fieldTitles, Map<String, Set<String>> tabFieldMap, List<String> groupByList, boolean isGroupActionName, Map<String, String> linkChild, String facetStr, OutputStream os) throws Exception;


    DwbAdmmisionVo getDwbAdmmisionByVisitidBySolr(String visitid) throws Exception;;

	DwbDischargeVo getDwbDischargeByVisitidBySolr(String visitid) throws Exception;
}
