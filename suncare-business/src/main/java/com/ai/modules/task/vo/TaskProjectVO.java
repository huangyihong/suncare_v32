package com.ai.modules.task.vo;

import com.ai.modules.task.entity.TaskCommonConditionSet;
import com.ai.modules.task.entity.TaskProject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Auther: zhangpeng
 * @Date: 2020/2/18 14
 * @Description:
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class TaskProjectVO extends TaskProject {
    private String clientIds;
    private Integer batchCount;
    private List<TaskCommonConditionSet> conditionSets;
}
