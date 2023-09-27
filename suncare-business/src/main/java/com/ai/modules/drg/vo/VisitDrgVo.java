package com.ai.modules.drg.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;


@Data
@ApiModel(value="VisitDrgVo", description="入组病历信息")
public class VisitDrgVo {

    //结算日期,出院主要诊断,主要手术和操作名称,住院总费用,医保结算ID,医保结算金额(元)

    private String id;
    //就诊ID
    private String visitid;

    //医保住院登记号
    private String caseId;

    //医疗机构名称
    private String orgname;

    //医疗机构编码
    private String orgid;

    //患者姓名
    private String clientname;

    private String clientid;

    //医保个人编号
    private String insurancecardNo;

    //性别
    private String sex;

    //出生时间
    private String birthday;

    //参保类型
    private String insurancetype;

    //入院日期
    private String admitdate;

    //出院日期
    private String leavedate;

    //出院主要诊断
    private String diagConclusion;

    //住院总费用
    private java.math.BigDecimal totalfee;

    //DRG目录编码
    private String drg;

    //DRG目录名称
    private String drgName;

    //ADRG目录编码
    private String adrg;

    //ADRG目录名称
    private String adrgName;

    //mdc目录编码
    private String mdc;

    //mdc目录名称
    private String mdcName;

    /**满足MDC诊断编码*/
    private String mdcDiagCode;
    /**满足MDC诊断名称*/
    private String mdcDiagName;
    /**满足ADRG手术编码*/
    private String adrgSurgeryCode;
    /**满足ADRG手术名称*/
    private String adrgSurgeryName;
    /**DRG主诊断编码*/
    private String drgDiagCode;
    /**DRG主诊断名称*/
    private String drgDiagName;
    /**DRG主手术编码*/
    private String drgSurgeryCode;
    /**DRG主手术名称*/
    private String drgSurgeryName;

}
