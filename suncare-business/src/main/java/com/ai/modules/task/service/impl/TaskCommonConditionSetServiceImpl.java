package com.ai.modules.task.service.impl;

import com.ai.modules.task.entity.TaskCommonConditionSet;
import com.ai.modules.task.mapper.TaskCommonConditionSetMapper;
import com.ai.modules.task.service.ITaskCommonConditionSetService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 通用条件设置
 * @Author: jeecg-boot
 * @Date:   2021-05-27
 * @Version: V1.0
 */
@Service
public class TaskCommonConditionSetServiceImpl extends ServiceImpl<TaskCommonConditionSetMapper, TaskCommonConditionSet> implements ITaskCommonConditionSetService {

    @Override
    public void removeByRuleId(String ruleId) {
        this.remove(new QueryWrapper<TaskCommonConditionSet>().eq("RULE_ID", ruleId));
    }
}
