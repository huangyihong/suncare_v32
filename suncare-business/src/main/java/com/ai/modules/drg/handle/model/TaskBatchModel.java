package com.ai.modules.drg.handle.model;

import lombok.Data;

import java.util.List;

/**
 * @author : zhangly
 * @date : 2023/3/31 14:24
 */
@Data
public class TaskBatchModel {
    /**批次号*/
    private String batchId;
    /**任务名称*/
    private String batchName;
    /**sequence*/
    private String currentSqlSeq;
    /**项目地*/
    private String project;
    /**医院编码*/
    private String orgIds;
    private String startVisitdate;
    private String endVisitdate;
    /**adrg限定规则*/
    private List<DrgRuleModel> adrgRuleList;
    /**drg限定规则*/
    private List<DrgRuleModel> drgRuleList;
}
