package com.ai.modules.ybChargeSearch.vo;

import lombok.Data;

/**
 * 医疗机构
 */
@Data
public class OdsCheckorgListVo{
    private String orgid;
    //机构名称
    private String orgname;

    //机构属性
    private String owntype;

    //机构所在地
    private String localTag;

    //医院等级
    private String hosplevel;

    //最小基金支付金额
    private String maxAllfundPay;

    //符号
    private String fundValType;
}
