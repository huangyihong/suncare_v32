package com.ai.modules.task.dto;

import com.ai.modules.task.entity.TaskActionFieldRelaSer;
import lombok.Data;

import java.util.List;

/**
 * @Auther: zhangpeng
 * @Date: 2021/03/19 16
 * @Description:
 */
@Data
public class TaskActionFieldSearchDTO {
    private String configId;
    private List<TaskActionFieldRelaSer> cols;
}
