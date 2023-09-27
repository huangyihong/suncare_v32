package com.ai.modules.task.mapper;

import java.util.List;

import com.ai.modules.task.vo.TaskReviewRuleTotalVO;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.task.entity.TaskReviewAssign;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 系统审核任务分配
 * @Author: jeecg-boot
 * @Date:   2020-05-19
 * @Version: V1.0
 */
public interface TaskReviewAssignMapper extends BaseMapper<TaskReviewAssign> {

    List<TaskReviewAssign> listJoinUserInfo(
            @Param("batchId") String batchId,
            @Param("leader") String leader,
            @Param("ruleType") String ruleType,
            @Param("step") Integer step
    );
    List<TaskReviewRuleTotalVO> ruleResultInfo(@Param("batchId") String batchId, @Param("ruleType") String ruleType);
}
