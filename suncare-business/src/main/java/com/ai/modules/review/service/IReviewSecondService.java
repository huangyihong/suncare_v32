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

import com.ai.modules.engine.model.vo.MedicalCaseVO;
import com.ai.modules.formal.vo.MedicalFormalBehaviorVO;
import com.ai.modules.review.vo.*;
import com.ai.modules.task.entity.TaskBatchBreakRule;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.jeecg.common.system.vo.LoginUser;

import javax.servlet.http.HttpServletRequest;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IReviewSecondService {


	/**
	 * 导出不合规病例
	 *
	 * @param solrQuerys
	 * @param os
	 */
	void exportExcel(SolrQuery[] solrQuerys, String batchId, OutputStream os) throws Exception;

	void exportExcelCase(String batchId, Map<String, List<MedicalFormalBehaviorVO>> caseBehaviorMap, SXSSFWorkbook workbook) throws Exception;

	void exportExcelHosp(String batchId, Map<String, MedicalFormalBehaviorVO> behaviorIdMap, SXSSFWorkbook workbook) throws Exception;

	void exportExcelDoc(String batchId, Map<String, MedicalFormalBehaviorVO> behaviorIdMap, SXSSFWorkbook workbook) throws Exception;

	void exportStatItemDetail(SolrQuery masterQuery, List<String> unreasonableFqList, String batchId, OutputStream os) throws Exception;

	void exportStatItemTotal(SolrQuery masterQuery, List<String> unreasonableFqList, String batchId, OutputStream os) throws Exception;

	void exportStatCaseTotal(SolrQuery masterQuery, List<String> unreasonableFqList, OutputStream os) throws Exception;
}
