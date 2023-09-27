package com.ai.modules.task.service;

import com.ai.modules.his.entity.HisTaskBatchBreakRule;
import com.ai.modules.task.dto.TaskBatchExecInfo;
import com.ai.modules.task.entity.TaskBatchBreakRule;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.modules.task.vo.TaskBatchStepItemVO;
import com.ai.modules.task.vo.TaskProjectBatchVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 任务项目批次
 * @Author: jeecg-boot
 * @Date:   2020-01-03
 * @Version: V1.0
 */
public interface ITaskProjectBatchService extends IService<TaskProjectBatch> {

    void saveBatch(TaskProjectBatch taskProjectBatch, List<TaskBatchBreakRule> ruleList);

    void updateBatch(TaskProjectBatch taskProjectBatch, List<TaskBatchBreakRule> ruleList, List<String> editRuleTypes);

    TaskProjectBatch removeBatch(String id) throws Exception;


    IPage<TaskProjectBatchVO> pageVO(Page<TaskProjectBatchVO> page, QueryWrapper<TaskProjectBatch> queryWrapper);

    List<TaskBatchStepItemVO> selectTopBatchItems(int topNum, String dataSource);

    TaskBatchExecInfo queryExecInfoById(String batchId);

    void reBackHis(String batchId);

    void reBackHis(String batchId, List<String> caseIdS) throws Exception;

    List<TaskProjectBatch> queryBatchByProjectOrDs(String[] dsArray, String[] pjArray);
}
