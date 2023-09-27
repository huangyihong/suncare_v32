package com.ai.modules.task.service.impl;

import com.ai.modules.task.entity.TaskBatchStepItem;
import com.ai.modules.task.mapper.TaskBatchStepItemMapper;
import com.ai.modules.task.service.ITaskBatchStepItemService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 批次步骤子页子项关联
 * @Author: jeecg-boot
 * @Date:   2020-02-18
 * @Version: V1.0
 */
@Service
public class TaskBatchStepItemServiceImpl extends ServiceImpl<TaskBatchStepItemMapper, TaskBatchStepItem> implements ITaskBatchStepItemService {

    @Override
    public List<TaskBatchStepItem> queryByBatchStep(String batchId, Integer step) {
        QueryWrapper<TaskBatchStepItem> queryWrapper = new QueryWrapper<TaskBatchStepItem>()
                .eq("BATCH_ID",batchId)
                .eq("STEP",step);
        return this.baseMapper.selectList(queryWrapper);
    }
}
