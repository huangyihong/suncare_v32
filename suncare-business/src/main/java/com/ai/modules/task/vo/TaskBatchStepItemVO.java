package com.ai.modules.task.vo;

import com.ai.modules.task.entity.TaskBatchStepItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Auther: zhangpeng
 * @Date: 2020/9/10 16
 * @Description:
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class TaskBatchStepItemVO  extends TaskBatchStepItem {

    private String batchName;
    private String projectName;
}
