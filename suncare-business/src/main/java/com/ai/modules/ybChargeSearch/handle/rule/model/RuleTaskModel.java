package com.ai.modules.ybChargeSearch.handle.rule.model;

import lombok.Data;

/**
 * @author : zhangly
 * @date : 2023/2/17 15:11
 */
@Data
public class RuleTaskModel {
    /**规则限制内容*/
    private RuleLimitModel ruleLimitModel;
    /**规则名称*/
    private String ruleName;
    /**医院名称*/
    private String orgname;
    /**医院编码*/
    private String orgid;
    /**开始时间*/
    private String startDate;
    /**结束时间*/
    private String endDate;
    /**就诊类型*/
    private String visittype;
    /**患者姓名*/
    private String clientname;
    /**就诊号*/
    private String visitid;
    /**出院日期*/
    private String leavedate;
    /**数据来源*/
    private String etlSource;
    /**数据来源层级*/
    private String dataStaticsLevel;
    /**病案号*/
    private String caseid;
    /**数据库类型*/
    private String dbType;
    /**患者身份证号*/
    private String idNo;
}
