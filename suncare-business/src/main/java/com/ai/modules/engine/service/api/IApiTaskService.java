/**
 * ITaskService.java	  V1.0   2020年12月23日 下午3:06:33
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ai.modules.task.entity.TaskBatchBreakRule;
import com.ai.modules.task.entity.TaskBatchBreakRuleDel;
import com.ai.modules.task.entity.TaskBatchBreakRuleLog;
import com.ai.modules.task.entity.TaskBatchStepItem;
import com.ai.modules.task.entity.TaskCommonConditionSet;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

public interface IApiTaskService {
	/**
	 * 
	 * 功能描述：根据id查找项目
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月23日 下午3:12:16</p>
	 *
	 * @param projectId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	TaskProject findTaskProject(String projectId);
	
	/**
	 * 
	 * 功能描述：根据id查找项目批次
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月23日 下午3:13:47</p>
	 *
	 * @param batchId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	TaskProjectBatch findTaskProjectBatch(String batchId);
	
	void updateTaskProjectBatch(TaskProjectBatch batch);
	
	/**
	 * 
	 * 功能描述：项目批次是否可以运行
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年3月8日 下午3:53:41</p>
	 *
	 * @param batchId
	 * @param itemId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	boolean canRun(String batchId, String itemId);
	
	/**
	 * 
	 * 功能描述：更新项目批次进度
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月23日 下午3:26:20</p>
	 *
	 * @param batchId
	 * @param stepType
	 * @param up
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void updateTaskBatchStepItem(String batchId, String stepType, TaskBatchStepItem up);
	void updateTaskBatchStepItem(String batchId, Set<String> stepTypes, TaskBatchStepItem up);
	void updateTaskBatchStepItem(String batchId, String stepType, String datasource, TaskBatchStepItem up);
	
	/**
	 * 
	 * 功能描述：保存项目批次进度
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月23日 下午3:26:20</p>
	 *
	 * @param step
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void saveTaskBatchStepItem(TaskBatchStepItem step);
	
	/**
	 * 
	 * 功能描述：保存项目批次进度
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月23日 下午3:26:20</p>
	 *
	 * @param step
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void saveTaskBatchStepItem(List<TaskBatchStepItem> stepList);
	
	/**
	 * 
	 * 功能描述：删除项目批次进度
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月23日 下午3:26:20</p>
	 *
	 * @param step
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void removeTaskBatchStepItem(String batchId);
	
	void removeTaskBatchStepItem(String batchId, String datasource);
	
	/**
	 * 
	 * 功能描述：删除批次规则进度日志
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月23日 下午3:59:55</p>
	 *
	 * @param batchId
	 * @param busiType
	 * @param itemStype
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void removeTaskBatchBreakRuleLog(String batchId, String busiType);
	/**
	 * 
	 * 功能描述：删除批次规则进度日志
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月23日 下午3:59:55</p>
	 *
	 * @param batchId
	 * @param busiType
	 * @param itemStype
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void removeTaskBatchBreakRuleLog(String batchId, String busiType, String itemStype);
	
	/**
	 * 
	 * 功能描述：保存批次规则进度日志
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月23日 下午4:22:46</p>
	 *
	 * @param logList
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void saveTaskBatchBreakRuleLog(List<TaskBatchBreakRuleLog> logList);
	
	void saveTaskBatchBreakRuleLog(TaskBatchBreakRuleLog log);
	
	TaskBatchBreakRuleLog findTaskBatchBreakRuleLog(String logId);
	
	/**
	 * 
	 * 功能描述：修改批次规则进度日志
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月23日 下午4:22:33</p>
	 *
	 * @param batchId
	 * @param busiType
	 * @param itemId
	 * @param up
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void updateTaskBatchBreakRuleLog(String batchId, String busiType, String itemId, TaskBatchBreakRuleLog up);
	void updateTaskBatchBreakRuleLog(String batchId, String busiType, List<String> itemIds, TaskBatchBreakRuleLog up);
	
	/**
	 * 
	 * 功能描述：按状态统计批次规则进度日志
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月23日 下午4:49:08</p>
	 *
	 * @param batchId
	 * @param busiType
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	Map<String, Integer> groupByTaskBatchBreakRuleLog(String batchId, String busiType);
	
	/**
	 * 
	 * 功能描述：批量更新规则运行状态
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月24日 上午9:17:55</p>
	 *
	 * @param batchId
	 * @param busiType
	 * @param codes
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void waitTaskBatchBreakRuleLog(String batchId, String busiType, List<String> codes);
	
	/**
	 * 
	 * 功能描述：查找项目批次已选中的业务组、临床路径等
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月25日 下午9:19:56</p>
	 *
	 * @param batchId
	 * @param ruleId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<TaskBatchBreakRule> findTaskBatchBreakRule(String batchId, String ruleId);
	
	List<TaskBatchBreakRule> findTaskBatchBreakRuleByStep(String batchId, String stepType);
	
	/**
	 * 
	 * 功能描述：查找项目批次已选中的模型、临床路径进度
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月26日 下午5:38:24</p>
	 *
	 * @param batchId
	 * @param stepType
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<TaskBatchBreakRuleDel> findTaskBatchBreakRuleDel(String batchId, String stepType);
	
	/**
	 * 
	 * 功能描述：更新项目批次已选中的模型、临床路径进度
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月26日 下午6:23:11</p>
	 *
	 * @param batchId
	 * @param stepType
	 * @param caseId
	 * @param up
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void updateTaskBatchBreakRuleDel(String batchId, String stepType, String caseId, TaskBatchBreakRuleDel up);
	void updateTaskBatchBreakRuleDel(String batchId, Set<String> stepTypes, String caseId, TaskBatchBreakRuleDel up);
	
	/**
	 * 
	 * 功能描述：查找项目过滤条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年5月28日 下午5:02:23</p>
	 *
	 * @param projectId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<TaskCommonConditionSet> queryTaskCommonConditionSet(String projectId);
}
