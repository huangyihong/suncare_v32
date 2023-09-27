package com.ai.modules.task.vo;

import com.ai.modules.task.entity.TaskBatchBreakRuleDel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Auther: zhangpeng
 * @Date: 2020/12/1 11
 * @Description:
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class TaskBatchBreakRuleDelVO extends TaskBatchBreakRuleDel {

    private String caseClassify;
}
