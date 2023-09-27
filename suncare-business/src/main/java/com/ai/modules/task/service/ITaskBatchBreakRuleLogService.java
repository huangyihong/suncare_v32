package com.ai.modules.task.service;

import java.util.List;
import java.util.Map;

import com.ai.modules.task.dto.TaskBatchBreakRuleLogDTO;
import com.ai.modules.task.entity.TaskBatchBreakRuleLog;
import com.ai.modules.task.vo.TaskBatchBreakRuleLogVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 批次任务运行日志
 * @Author: jeecg-boot
 * @Date:   2020-10-12
 * @Version: V1.0
 */
public interface ITaskBatchBreakRuleLogService extends IService<TaskBatchBreakRuleLog> {
	Map<String, Integer> groupByStatus(String batchId, String busiType);
	
	/**
     * 
     * 功能描述：批量更新规则运行状态
     *
     * @author  zhangly
     * <p>创建日期 ：2020年12月22日 下午12:36:07</p>
     *
     * @param batchId
     * @param busiType
     * @param codes
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    void waitTaskBatchBreakRuleLog(String batchId, String busiType, List<String> codes);
    
    IPage<TaskBatchBreakRuleLogVO> queryTaskBatchBreakRuleLog(IPage<TaskBatchBreakRuleLogVO> page, Wrapper<TaskBatchBreakRuleLogDTO> wrapper);
    IPage<TaskBatchBreakRuleLogVO> queryTaskBatchBreakRuleLog(IPage<TaskBatchBreakRuleLogVO> page, TaskBatchBreakRuleLogDTO dto);
    
    List<Map<String, Object>> queryTaskBatchBreakRuleLimit(TaskBatchBreakRuleLogDTO dto);
    
    List<Map<String, Object>> queryTaskBatchBreakRuleAction(TaskBatchBreakRuleLogDTO dto);
    
    IPage<TaskBatchBreakRuleLogVO> queryDruguseLog(IPage<TaskBatchBreakRuleLogVO> page, TaskBatchBreakRuleLogDTO dto);
    
    IPage<TaskBatchBreakRuleLog> queryDrugLog(IPage<TaskBatchBreakRuleLog> page, TaskBatchBreakRuleLogDTO dto);
    
    List<Map<String, Object>> queryTaskBatchBreakRuleEngine(String batchId);
}
