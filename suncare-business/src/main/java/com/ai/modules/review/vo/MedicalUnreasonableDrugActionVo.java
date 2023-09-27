package com.ai.modules.review.vo;

import lombok.Data;

/**
 * @Auther: zhangpeng
 * @Date: 2020/6/9 15
 * @Description:
 */

@Data
public class MedicalUnreasonableDrugActionVo {

    // 项目编码
    private String itemcode;

    // 项目名称
    private String itemname;

    // 规格
    private String specificaion;

    // 结算日期
    private String chargedate;

    // 规则名称
    private String ruleName;

    // 提示信息
    private String ruleDesc;

    // 规则类型 1 药品 2收费
    private String ruleType;

    private String visitid;

    private String batchId;


}
