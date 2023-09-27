package com.ai.modules.task.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.ai.modules.task.dto.TaskBatchBreakRuleLogDTO;
import com.ai.modules.task.entity.TaskBatchBreakRuleLog;
import com.ai.modules.task.vo.TaskBatchBreakRuleLogVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;

/**
 * @Description: 批次任务运行日志
 * @Author: jeecg-boot
 * @Date:   2020-10-12
 * @Version: V1.0
 */
public interface TaskBatchBreakRuleLogMapper extends BaseMapper<TaskBatchBreakRuleLog> {
	IPage<TaskBatchBreakRuleLogVO> queryTaskBatchBreakRuleLog(IPage<TaskBatchBreakRuleLogVO> page, @Param(Constants.WRAPPER) Wrapper<TaskBatchBreakRuleLogDTO> wrapper);
	IPage<TaskBatchBreakRuleLogVO> queryDruguseRuleLog(IPage<TaskBatchBreakRuleLogVO> page, @Param(Constants.WRAPPER) Wrapper<TaskBatchBreakRuleLogDTO> wrapper);
	
	List<Map<String, Object>> queryTaskBatchBreakRuleLimit(@Param(Constants.WRAPPER) Wrapper<TaskBatchBreakRuleLogDTO> wrapper);
	
	List<Map<String, Object>> queryTaskBatchBreakRuleAction(@Param(Constants.WRAPPER) Wrapper<TaskBatchBreakRuleLogDTO> wrapper);
	
	List<Map<String, Object>> queryTaskBatchBreakRuleEngine(@Param("batchId") String batchId);
}
