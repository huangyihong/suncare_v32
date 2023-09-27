package com.ai.modules.task.dto;

import com.ai.modules.task.entity.TaskReviewAssign;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Auther: zhangpeng
 * @Date: 2020/5/20 15
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TaskReviewAssignDTO extends TaskReviewAssign {
    String memberName;
    List<String> caseIds;
}
