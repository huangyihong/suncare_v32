package com.ai.modules.task.service.impl;

import com.ai.modules.task.entity.TaskAsyncActionLog;
import com.ai.modules.task.mapper.TaskAsyncActionLogMapper;
import com.ai.modules.task.service.ITaskAsyncActionLogService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 异步操作日志
 * @Author: jeecg-boot
 * @Date:   2020-12-07
 * @Version: V1.0
 */
@Service
public class TaskAsyncActionLogServiceImpl extends ServiceImpl<TaskAsyncActionLogMapper, TaskAsyncActionLog> implements ITaskAsyncActionLogService {

}
