package com.ai.modules.review.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class ReviewNewPushVo{
    @ApiModelProperty(value = "主键")
    private java.lang.String id;

    @ApiModelProperty(value = "就诊ID")
    private java.lang.String visitid;

    @ApiModelProperty(value = "yx患者编号 ")
    private java.lang.String clientid;

    @ApiModelProperty(value = "患者姓名")
    private java.lang.String clientname;

    @ApiModelProperty(value = "性别代码")
    private java.lang.String sexCode;

    @ApiModelProperty(value = "性别名称")
    private java.lang.String sex;

    @ApiModelProperty(value = "出生日期 ")
    private java.lang.String birthday;

    @ApiModelProperty(value = "年龄（岁）")
    private java.lang.Double yearage;

    @ApiModelProperty(value = "就诊类型代码 ")
    private java.lang.String visittypeId;

    @ApiModelProperty(value = "就诊类型名称")
    private java.lang.String visittype;

    @ApiModelProperty(value = "就诊日期时间")
    private java.lang.String visitdate;

    @ApiModelProperty(value = "就诊标志")
    private java.lang.String visitSign;

    @ApiModelProperty(value = "就诊医疗机构编码")
    private java.lang.String orgid;

    @ApiModelProperty(value = "就诊医疗机构名称 ")
    private java.lang.String orgname;

    @ApiModelProperty(value = "医疗费用总金额")
    private java.lang.Double totalfee;

    @ApiModelProperty(value = "出院日期时间")
    private java.lang.String leavedate;

    @ApiModelProperty(value = "疾病编码")
    private java.lang.String diseasecode;

    @ApiModelProperty(value = "疾病名称 ")
    private java.lang.String diseasename;

    @ApiModelProperty(value = "his就诊id ")
    private java.lang.String hisVisitid;

    @ApiModelProperty(value = "模型ID")
    private java.lang.String caseId;

    @ApiModelProperty(value = "模型名称")
    private java.lang.String caseName;

    @ApiModelProperty(value = "项目ID")
    private java.lang.String projectId;

    @ApiModelProperty(value = "项目名称 ")
    private java.lang.String projectName;

    @ApiModelProperty(value = "项目批次号")
    private java.lang.String batchId;

    @ApiModelProperty(value = "病例违规金额")
    private java.lang.Double actionMoney;

    @ApiModelProperty(value = "不合规行为类型ID")
    private java.lang.String actionTypeId;

    @ApiModelProperty(value = "不合规行为类型名称")
    private java.lang.String actionTypeName;

    @ApiModelProperty(value = "不合规行为ID")
    private java.lang.String actionId;

    @ApiModelProperty(value = "不合规行为名称 ")
    private java.lang.String actionName;

    @ApiModelProperty(value = "不合规行为释义")
    private java.lang.String actionDesc;

    /**是否推送{1:是,0:否}*/
    @ApiModelProperty(value = "是否推送")
    private java.lang.String pushStatus;

}
