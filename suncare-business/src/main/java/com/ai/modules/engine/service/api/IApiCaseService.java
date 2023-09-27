/**
 * IApiCaseService.java	  V1.0   2020年12月26日 下午6:48:17
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.api;

import java.util.List;

import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineRuleGrade;
import com.ai.modules.formal.entity.MedicalFormalCase;
import com.ai.modules.formal.entity.MedicalFormalCaseItemRela;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.medical.entity.MedicalSpecialCaseClassify;
import com.ai.modules.probe.entity.MedicalProbeCase;

public interface IApiCaseService {
	/**
	 * 
	 * 功能描述：根据批次号查找模型
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月26日 下午6:51:08</p>
	 *
	 * @param batchId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<HisMedicalFormalCase> findHisMedicalFormalCase(String batchId);
	/**
	 * 
	 * 功能描述：根据批次号、业务组号查找模型
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月26日 下午6:51:36</p>
	 *
	 * @param batchId
	 * @param busiId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<HisMedicalFormalCase> findHisMedicalFormalCase(String batchId, String busiId);
	
	/**
	 * 
	 * 功能描述：根据批次号、模型编号查找模型
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月26日 下午7:02:38</p>
	 *
	 * @param batchId
	 * @param caseId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	HisMedicalFormalCase findHisMedicalFormalCaseByCaseid(String batchId, String caseId);
	
	/**
	 * 
	 * 功能描述：根据模型编号查找模型关联的项目
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月26日 下午7:07:29</p>
	 *
	 * @param caseId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	MedicalFormalCaseItemRela findMedicalFormalCaseItemRela(String caseId);
	
	/**
	 * 
	 * 功能描述：根据批次号、模型编号查找模型的评分列表
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月26日 下午7:11:15</p>
	 *
	 * @param batchId
	 * @param caseId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<EngineRuleGrade> findEngineRuleGrade(String batchId, String caseId);
	
	/**
	 * 
	 * 功能描述：查找特殊模型列表
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月26日 下午7:24:44</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalSpecialCaseClassify> findMedicalSpecialCaseClassify();
	
	/**
	 * 
	 * 功能描述：查找特殊模型
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月26日 下午7:25:07</p>
	 *
	 * @param classifyId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	MedicalSpecialCaseClassify findMedicalSpecialCaseClassify(String classifyId);
	
	/**
	 * 
	 * 功能描述：按模型ID递归查询流程节点
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月28日 上午10:59:30</p>
	 *
	 * @param caseId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<EngineNode> recursionMedicalFormalFlowByCaseid(String caseId);
	
	/**
	 * 
	 * 功能描述：按模型ID查询流程节点的所有查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月28日 上午11:04:54</p>
	 *
	 * @param caseId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<EngineNodeRule> queryMedicalFormalFlowRuleByCaseid(String caseId);
	
	/**
	 * 
	 * 功能描述：按批次号、模型ID递归查询流程节点
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月28日 上午11:08:57</p>
	 *
	 * @param caseId
	 * @param batchId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<EngineNode> recursionMedicalFormalFlowByCaseid(String caseId, String batchId);
	
	List<EngineNode> queryHisMedicalFormalFlow(String caseId, String batchId);
	
	/**
	 * 
	 * 功能描述：按批次号、模型ID查询流程节点的所有查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月28日 上午11:13:27</p>
	 *
	 * @param caseId
	 * @param batchId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<EngineNodeRule> queryMedicalFormalFlowRuleByCaseid(String caseId, String batchId);
	
	/**
	 * 
	 * 功能描述：查找模板流程节点的所有查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月28日 上午11:18:04</p>
	 *
	 * @param nodeId
	 * @param nodeCode
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<EngineNodeRule> queryMedicalFormalFlowRuleByTmpl(String nodeId, String nodeCode);
	
	MedicalFormalCase findMedicalFormalCase(String caseId);
	
	List<EngineNode> findMedicalFormalFlowByCaseid(String caseId);
	
	List<MedicalFormalCase> findMedicalFormalCaseAll();
	
	/**
	 * 
	 * 功能描述：按探查模型ID查询流程节点的所有查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年9月1日 下午1:21:42</p>
	 *
	 * @param caseId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<EngineNodeRule> queryMedicalProbeFlowRuleByCaseid(String caseId);
	
	/**
	 * 
	 * 功能描述：根据ID查找探查模型
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年9月1日 下午1:33:38</p>
	 *
	 * @param caseId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	MedicalProbeCase findMedicalProbeCase(String caseId);
	
	List<MedicalProbeCase> findMedicalProbeCaseAll();
}
