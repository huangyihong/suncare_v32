package com.ai.modules.task.service;

import com.ai.modules.task.entity.TaskBatchBreakRule;
import com.ai.modules.task.vo.TaskBatchBreakRuleVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 批次规则关联
 * @Author: jeecg-boot
 * @Date:   2020-01-02
 * @Version: V1.0
 */
public interface ITaskBatchBreakRuleService extends IService<TaskBatchBreakRule> {

    List<TaskBatchBreakRuleVO> listByType(QueryWrapper<TaskBatchBreakRule> queryWrapper, String type, Boolean inFormal);

    List<TaskBatchBreakRuleVO> listInFormalByType(String[] ruleIds, String type);
}
