package com.ai.modules.task.service;

import com.ai.modules.task.entity.TaskCommonConditionSet;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 通用条件设置
 * @Author: jeecg-boot
 * @Date:   2021-05-27
 * @Version: V1.0
 */
public interface ITaskCommonConditionSetService extends IService<TaskCommonConditionSet> {

    void removeByRuleId(String projectId);
}
