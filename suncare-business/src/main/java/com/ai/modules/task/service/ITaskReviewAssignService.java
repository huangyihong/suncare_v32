package com.ai.modules.task.service;

import com.ai.modules.task.vo.TaskReviewRuleTotalVO;
import com.ai.modules.task.entity.TaskReviewAssign;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 系统审核任务分配
 * @Author: jeecg-boot
 * @Date:   2020-05-19
 * @Version: V1.0
 */
public interface ITaskReviewAssignService extends IService<TaskReviewAssign> {

    List<TaskReviewAssign> listJoinUserInfo(String batchId, String leader, String ruleType, Integer step);

    List<TaskReviewRuleTotalVO> ruleResultInfo(String batchId, String ruleType);
}
