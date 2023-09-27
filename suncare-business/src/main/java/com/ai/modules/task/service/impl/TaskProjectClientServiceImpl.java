package com.ai.modules.task.service.impl;

import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectClient;
import com.ai.modules.task.mapper.TaskProjectClientMapper;
import com.ai.modules.task.service.ITaskProjectClientService;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 项目客户关联
 * @Author: jeecg-boot
 * @Date:   2020-02-18
 * @Version: V1.0
 */
@Service
public class TaskProjectClientServiceImpl extends ServiceImpl<TaskProjectClientMapper, TaskProjectClient> implements ITaskProjectClientService {

    @Override
    public List<LoginUser> selectUsersByProject(String projectId) {
        return this.baseMapper.selectUsersByProject(projectId);
    }

    @Override
    public List<TaskProject> selectProjectByUser(String userId) {
        return this.baseMapper.selectProjectByUser(userId);
    }
}
