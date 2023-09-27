package com.ai.modules.task.service.impl;

import com.ai.modules.task.entity.TaskReviewAssign;
import com.ai.modules.task.mapper.TaskReviewAssignMapper;
import com.ai.modules.task.service.ITaskReviewAssignService;
import com.ai.modules.task.vo.TaskReviewRuleTotalVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;
import java.util.Map;

/**
 * @Description: 系统审核任务分配
 * @Author: jeecg-boot
 * @Date:   2020-05-19
 * @Version: V1.0
 */
@Service
public class TaskReviewAssignServiceImpl extends ServiceImpl<TaskReviewAssignMapper, TaskReviewAssign> implements ITaskReviewAssignService {

    @Override
    public List<TaskReviewAssign> listJoinUserInfo(String batchId, String leader, String ruleType, Integer step) {
        return this.baseMapper.listJoinUserInfo(batchId, leader, ruleType, step);
    }

    @Override
    public List<TaskReviewRuleTotalVO> ruleResultInfo(String batchId, String ruleType) {
        return this.baseMapper.ruleResultInfo(batchId, ruleType);
    }
}
