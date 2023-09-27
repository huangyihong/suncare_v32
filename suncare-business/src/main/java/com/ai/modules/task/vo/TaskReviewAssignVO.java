package com.ai.modules.task.vo;

import com.ai.modules.task.entity.TaskReviewAssign;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Auther: zhangpeng
 * @Date: 2020/5/20 14
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TaskReviewAssignVO extends TaskReviewAssign {
    private String memberName;
    private String avatar;
    private Integer ruleCount;
    private Integer ruleCountAudited;
}
