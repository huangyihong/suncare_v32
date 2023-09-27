/**
 * EngineService.java	  V1.0   2019年11月29日 上午11:05:59
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.service;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ai.modules.engine.model.EchartsEntity;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineResult;
import com.ai.modules.engine.model.dto.CaseFlowDTO;
import com.ai.modules.engine.model.dto.CompareCaseFlowDTO;
import com.ai.modules.engine.model.dto.EchartCaseFlowDTO;
import com.ai.modules.engine.model.dto.EchartCompareCaseFlowDTO;
import com.ai.modules.engine.model.dto.EngineCaseDTO;
import com.ai.modules.engine.model.dto.EngineCaseFlowDTO;
import com.ai.modules.engine.model.vo.MedicalCaseVO;
import com.ai.modules.formal.entity.MedicalFormalCase;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;

public interface IEngineService {
	/**
	 *
	 * 功能描述：根据前端传递的参数解析成引擎流程节点对象
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年12月2日 下午3:31:45</p>
	 *
	 * @param dto
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<EngineNode> parseEngineCaseDTO(EngineCaseDTO dto) throws Exception;

	/**
	 *
	 * 功能描述：根据前端传递的参数解析成引擎流程节点对象
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年12月2日 下午3:31:45</p>
	 *
	 * @param dto
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<EngineNode> parseEngineCaseDTO(CaseFlowDTO dto) throws Exception;

	/**
	 *
	 * 功能描述：构造查询条件表达式
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年11月29日 下午3:19:17</p>
	 *
	 * @param nodeList
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	Set<String> constructConditionExpression(List<EngineNode> nodeList) throws Exception;

    Set<String> constructTrialFq(EngineCaseFlowDTO dto) throws Exception;

    /**
	 *
	 * 功能描述：试算病例
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年12月2日 下午4:43:17</p>
	 *
	 * @param page
	 * @param dto
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	IPage<SolrDocument> trial(IPage<SolrDocument> page, EngineCaseFlowDTO dto) throws Exception;

	/**
	 *
	 * 功能描述：试算病例统计图表
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年12月3日 下午4:30:36</p>
	 *
	 * @param dto
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	EchartsEntity echart(EchartCaseFlowDTO dto) throws Exception;

	/**
	 *
	 * 功能描述：试算两个节点病例的交集或差集
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年12月4日 下午4:56:40</p>
	 *
	 * @param page
	 * @param dto
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	IPage<MedicalCaseVO> compare(IPage<MedicalCaseVO> page, CompareCaseFlowDTO dto) throws Exception;

	/**
	 *
	 * 功能描述：试算两个节点病例的交集或差集分组统计
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年12月5日 上午11:20:56</p>
	 *
	 * @param dto
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	EchartsEntity echart(EchartCompareCaseFlowDTO dto) throws Exception;

	/**
	 *
	 * 功能描述：根据caseId解析出模型所有流程
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年12月6日 下午3:08:13</p>
	 *
	 * @param caseId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<List<EngineNode>> queryFormalEngineNode(String caseId);

	List<List<EngineNode>> queryHisFormalEngineNode(String caseId,String batchId);
	
	List<EngineNodeRule> queryHisFormalEngineNodeRule(String caseId, String batchId);

	/**
	 *
	 * 功能描述：构造模型流程查询条件，多个之间是或关系
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年12月9日 下午2:39:07</p>
	 *
	 * @param flowList
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	String[] constructFormalCaseCondition(List<List<EngineNode>> flowList) throws Exception;

    Integer trialExport(EngineCaseFlowDTO dto, OutputStream os) throws Exception;

	void trialExportMasterInfo(EngineCaseFlowDTO dto, SolrQuery solrQuery, OutputStream os) throws Exception;
}
