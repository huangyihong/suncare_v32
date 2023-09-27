package com.ai.modules.task.dto;

import cn.hutool.json.JSONObject;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
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
public class TaskReviewAssignManualDTO extends TaskReviewAssign {
    String memberName;
    String[] selectKeys;
    Integer rangeStart;
    Integer rangeEnd;
    JSONObject params;
}
