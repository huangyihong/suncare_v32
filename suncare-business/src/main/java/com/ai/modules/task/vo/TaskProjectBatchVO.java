package com.ai.modules.task.vo;

import com.ai.modules.task.entity.TaskProjectBatch;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Auther: zhangpeng
 * @Date: 2020/9/10 10
 * @Description:
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class TaskProjectBatchVO extends TaskProjectBatch {

    private String step1Status;

    private Integer dataCount;

    private Integer pushDataCount;
}
