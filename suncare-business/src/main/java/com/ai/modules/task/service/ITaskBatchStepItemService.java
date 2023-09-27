package com.ai.modules.task.service;

import com.ai.modules.task.entity.TaskBatchStepItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 批次步骤子页子项关联
 * @Author: jeecg-boot
 * @Date:   2020-02-18
 * @Version: V1.0
 */
public interface ITaskBatchStepItemService extends IService<TaskBatchStepItem> {

    List<TaskBatchStepItem> queryByBatchStep(String batchId, Integer step);

}
