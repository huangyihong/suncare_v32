package com.ai.modules.task.service;

import com.ai.modules.task.entity.TaskBatchBreakRule;
import com.ai.modules.task.entity.TaskBatchBreakRuleDel;
import com.ai.modules.task.vo.TaskBatchBreakRuleDelVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @Description: 违规模型详情
 * @Author: jeecg-boot
 * @Date:   2020-01-17
 * @Version: V1.0
 */
public interface ITaskBatchBreakRuleDelService extends IService<TaskBatchBreakRuleDel> {

    void save(String batchId, List<TaskBatchBreakRule> breakRuleList) throws Exception;

    void update(String batchId, String busiId, String ruleId);

    void update(String actionId);

    void save(String batchId, String busiId, String ruleId);

    void exportExcel(String ruleType,QueryWrapper<TaskBatchBreakRuleDel> queryWrapper, OutputStream os) throws Exception;

    IPage<TaskBatchBreakRuleDelVO> pageVo(Page<TaskBatchBreakRuleDel> page, QueryWrapper<TaskBatchBreakRuleDel> queryWrapper, String batchId);
}
