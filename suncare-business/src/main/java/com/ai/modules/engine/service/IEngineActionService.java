/**
 * IEngineActionService.java	  V1.0   2020年9月9日 下午4:20:01
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service;

import java.io.BufferedWriter;
import java.util.List;
import java.util.Map;

import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.engine.model.EngineCntResult;
import com.ai.modules.engine.model.EngineLimitScopeEnum;
import com.ai.modules.engine.model.dto.ActionTypeDTO;
import com.ai.modules.engine.model.vo.ProjectFilterWhereVO;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;

public interface IEngineActionService {
	/**
	 * 
	 * 功能描述：删除批次历史数据
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年9月9日 下午4:28:26</p>
	 *
	 * @param batchId
	 * @param ruleType
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void deleteSolr(String batchId, String ruleType, boolean slave) throws Exception;
	
	/**
	 * 
	 * 功能描述：删除批次历史数据（按某个项目删除）
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月26日 下午5:57:34</p>
	 *
	 * @param batchId
	 * @param itemcode
	 * @param ruleType
	 * @param slave
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void deleteSolr(String batchId, String itemcode, String ruleType, boolean slave) throws Exception;
	
	/**
	 * 
	 * 功能描述：删除批次历史数据（按某个规则删除）
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月26日 下午5:57:34</p>
	 *
	 * @param batchId
	 * @param itemcode
	 * @param ruleType
	 * @param slave
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void deleteSolrByRule(String batchId, String ruleId, String ruleType, boolean slave) throws Exception;
	
	/**
	 * 
	 * 功能描述：数据写入文件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年9月9日 下午4:28:38</p>
	 *
	 * @param fileWriter
	 * @param map
	 * @param task
	 * @param batch
	 * @param drugCode
	 * @param ruleList
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	JSONObject writerJson(BufferedWriter fileWriter, Map<String, Object> map, 
			TaskProject task, TaskProjectBatch batch, String drugCode, 
			List<MedicalDrugRule> ruleList, EngineLimitScopeEnum limitScopeEnum,
			ActionTypeDTO dto, Map<String, MedicalActionDict> actionDictMap);
	
	JSONObject writerJson(BufferedWriter fileWriter, Map<String, Object> map, 
			MedicalDrugRule rule, EngineLimitScopeEnum limitScopeEnum,
			ActionTypeDTO dto);
	
	/**
	 * 
	 * 功能描述：更新字段
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年7月14日 下午5:45:47</p>
	 *
	 * @param dataList
	 * @param collection
	 * @param batchId
	 * @param itemCode
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void updateAction(List<JSONObject> dataList, String collection, String batchId, String itemCode) throws Exception;
	
	/**
     * 
     * 功能描述：数据过滤掉指标为空值的查询条件
     *
     * @author  zhangly
     * <p>创建日期 ：2020年7月15日 下午4:26:15</p>
     *
     * @param rule
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
	List<String> ignoreNullWhere(MedicalDrugRule rule);
	
	/**
     * 
     * 功能描述：药品不合规、收费项目不合规等结果数据到solr主服务器的MEDICAL_UNREASONABLE_ACTION
     *
     * @author  zhangly
     * <p>创建日期 ：2020年9月8日 下午5:16:07</p>
     *
     * @param task
     * @param batch
     * @param type
     * @param slave: 临时数据是否存储在备用solr服务器
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    void syncUnreasonableAction(TaskProject task, TaskProjectBatch batch, String type, boolean slave) throws Exception;
    
    /**
     * 
     * 功能描述：药品不合规、收费项目不合规等结果数据按每个细项同步到solr主服务器的MEDICAL_UNREASONABLE_ACTION
     *
     * @author  zhangly
     * <p>创建日期 ：2020年10月13日 上午11:07:30</p>
     *
     * @param task
     * @param batch
     * @param itemcode
     * @param type
     * @param slave
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    void syncUnreasonableAction(TaskProject task, TaskProjectBatch batch, String itemcode, String type, boolean slave) throws Exception;
    
    /**
     * 
     * 功能描述：药品不合规、收费项目不合规等试算结果数据到同步MEDICAL_TRAIL_ACTION
     *
     * @author  zhangly
     * <p>创建日期 ：2020年9月9日 下午2:52:34</p>
     *
     * @param ruleId
     * @param type
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    void syncTrailAction(String ruleId, String type) throws Exception;
    
    /**
     * 
     * 功能描述：导出已经审核的不合规行为结果
     *
     * @author  zhangly
     * <p>创建日期 ：2020年12月30日 上午10:20:23</p>
     *
     * @param batchId
     * @param busiType
     * @param fqs
     * @return
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    EngineCntResult importApprovalAction(String batchId, String busiType, List<String> fqs) throws Exception;
    
    EngineCntResult importApprovalAction(String batchId, String busiType) throws Exception;
    
    /**
     * 
     * 功能描述：导出某个模型、药品等规则已经审核的不合规行为结果
     *
     * @author  zhangly
     * <p>创建日期 ：2021年3月30日 上午11:20:25</p>
     *
     * @param batchId
     * @param busiType
     * @param caseId
     * @return
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    EngineCntResult importApprovalAction(String batchId, String busiType, String caseId) throws Exception;
    
    /**
     * 
     * 功能描述：导出某个规则已经审核的不合规行为结果
     *
     * @author  zhangly
     * <p>创建日期 ：2021年3月30日 上午11:28:03</p>
     *
     * @param batchId
     * @param busiType
     * @param ruleId
     * @return
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    EngineCntResult importApprovalActionFromRule(String batchId, String busiType, String ruleId) throws Exception;
    
    /**
	 * 
	 * 功能描述：项目业务数据的过滤条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年5月28日 上午11:27:54</p>
	 *
	 * @param project
	 * @param isMaster 是否主表是dwb_master_info
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
    ProjectFilterWhereVO filterCondition(TaskProject project, boolean isMaster) throws Exception;
    
    /**
     * 
     * 功能描述：不合规行为汇总
     *
     * @author  zhangly
     * <p>创建日期 ：2021年6月8日 上午10:23:24</p>
     *
     * @param batchId
     * @param actionId
     * @param fqs
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    void executeGroupBy(String batchId, String actionId, String[] fqs);
    
    void executeGroupBy(String batchId, String caseId, String busiType);
}
