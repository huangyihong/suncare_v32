package com.ai.modules.review.dto;

import cn.hutool.json.JSONObject;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.ai.modules.task.entity.TaskReviewAssign;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Auther: zhangpeng
 * @Date: 2020/5/20 15
 * @Description: 暂时没用
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class reviewManualDTO extends MedicalUnreasonableActionVo {

    private String[] selectKeys;
    private Integer rangeStart;
    private Integer rangeEnd;
    private JSONObject params;
}
