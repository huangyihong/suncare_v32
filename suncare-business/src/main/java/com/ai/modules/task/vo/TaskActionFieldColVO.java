package com.ai.modules.task.vo;

import com.ai.modules.task.entity.TaskActionFieldCol;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Auther: zhangpeng
 * @Date: 2021/2/23 10
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TaskActionFieldColVO extends TaskActionFieldCol {
    private String whereInputType;
    private String dataType;
    private String selectType;
    private String configId;

    private String editName;
}
