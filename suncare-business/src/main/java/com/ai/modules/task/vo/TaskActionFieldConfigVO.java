package com.ai.modules.task.vo;

import com.ai.modules.task.entity.TaskActionFieldCol;
import com.ai.modules.task.entity.TaskActionFieldConfig;
import com.ai.modules.task.entity.TaskActionFieldRela;
import com.ai.modules.task.entity.TaskActionFieldRelaSer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Auther: zhangpeng
 * @Date: 2020/10/26 18
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TaskActionFieldConfigVO extends TaskActionFieldConfig {
    private Boolean multi;
    private List<TaskActionFieldRela> cols;

    private List<TaskActionFieldRelaSer> searchs;
}
