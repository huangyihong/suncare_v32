package com.ai.modules.review.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.List;

@Data
@ApiModel(value = "MEDICAL_UNREASONABLE_ACTION对象", description = "违反不合理行为结果")
public class MedicalUnreasonableActionVo {
    @ApiModelProperty(value = "主键")
    private java.lang.String id;

    @ApiModelProperty(value = "就诊ID")
    private java.lang.String visitid;

    @ApiModelProperty(value = "yx患者编号 ")
    private java.lang.String clientid;

    @ApiModelProperty(value = "参保类别 ")
    private java.lang.String insurancetype;

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

    @ApiModelProperty(value = "医院级别")
    @MedicalDict(dicCode = "YYJB")
    private java.lang.String hosplevel;

    @ApiModelProperty(value = "医疗机构等级 ")
    @MedicalDict(dicCode = "YYDJ")
    private java.lang.String hospgrade;

    @ApiModelProperty(value = "就诊科室编码")
    private java.lang.String deptid;

    @ApiModelProperty(value = "就诊科室名称")
    private java.lang.String deptname;

    @ApiModelProperty(value = "就诊医师编码")
    private java.lang.String doctorid;

    @ApiModelProperty(value = "就诊医师姓名")
    private java.lang.String doctorname;

    @ApiModelProperty(value = "医疗费用总金额")
    private java.lang.String totalfee;

    @ApiModelProperty(value = "出院日期时间")
    private java.lang.String leavedate;

    @ApiModelProperty(value = "疾病编码")
    private java.lang.String diseasecode;

    @ApiModelProperty(value = "疾病名称 ")
    private java.lang.String diseasename;

    @ApiModelProperty(value = "病理诊断名称")
    private java.lang.String pathonogyDisease;

    @ApiModelProperty(value = "病理诊断疾病编码 ")
    private java.lang.String pathonogyDiseasecode;

    @ApiModelProperty(value = "his就诊id ")
    private java.lang.String hisVisitid;

    @ApiModelProperty(value = "虚拟就诊id ")
    private java.lang.String visitidDummy;

    @ApiModelProperty(value = "数据来源编码")
    private java.lang.String dataResouceId;

    @ApiModelProperty(value = "数据来源名称 ")
    private java.lang.String dataResouce;

    @ApiModelProperty(value = "etl来源编码 ")
    private java.lang.String etlSource;

    @ApiModelProperty(value = "etl来源名称")
    private java.lang.String etlSourceName;

    @ApiModelProperty(value = "etl时间")
    private java.lang.String etlTime;

    @ApiModelProperty(value = "模型ID")
    private String caseId;

    @ApiModelProperty(value = "模型名称")
    private String caseName;

    @ApiModelProperty(value = "数据生成时间")
    private java.lang.String genDataTime;

    @ApiModelProperty(value = "项目ID")
    private java.lang.String projectId;

    @ApiModelProperty(value = "项目名称 ")
    private java.lang.String projectName;

    @ApiModelProperty(value = "项目批次号")
    private java.lang.String batchId;

    @ApiModelProperty(value = "最小基金支出金额")
    private java.lang.String actionMoney;

    @ApiModelProperty(value = "模型得分")
    private java.lang.String caseScore;


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

    @ApiModelProperty(value = "业务类型")
    private java.lang.String busiType;

    @ApiModelProperty(value = "审查名称")
    private java.lang.String reviewName;
    @ApiModelProperty(value = "第一次审查人ID")
    private java.lang.String firReviewUserid;
    @ApiModelProperty(value = "第一次审查人姓名")
    private java.lang.String firReviewUsername;
    @ApiModelProperty(value = "第一次审查时间")
    private java.lang.String firReviewTime;
    /**
     * 第一次审查状态{init:待处理,white:白名单,blank:黑名单,grey:灰名单}
     */
    @ApiModelProperty(value = "第一次审查状态")
    private java.lang.String firReviewStatus;

    @ApiModelProperty(value = "第一次审查归类")
    private java.lang.String firReviewClassify;
    @ApiModelProperty(value = "第一次审查备注")
    private java.lang.String firReviewRemark;
    /**
     * 是否推送{1:是,0:否}
     */
    @ApiModelProperty(value = "是否推送")
    private java.lang.String pushStatus;
    @ApiModelProperty(value = "推送人")
    private java.lang.String pushUserid;
    @ApiModelProperty(value = "推送人")
    private java.lang.String pushUsername;
    @ApiModelProperty(value = "第二次审查人ID")
    private java.lang.String secReviewUserid;
    @ApiModelProperty(value = "第二次审查人姓名")
    private java.lang.String secReviewUsername;
    @ApiModelProperty(value = "第二次审查时间")
    private java.lang.String secReviewTime;
    /**
     * 第二次审查状态{wait:待审查,begin:审查中,exclude:排除,end:审查结束,reject:驳回,recover:收回}
     */
    @ApiModelProperty(value = "第二次审查状态")
    private java.lang.String secReviewStatus;

    @ApiModelProperty(value = "第二次审查归类")
    private java.lang.String secReviewClassify;

    @ApiModelProperty(value = "第二次审查备注")
    private java.lang.String secReviewRemark;
    /**
     * {1:是,0:否}
     **/
    @ApiModelProperty(value = "第二次是否推送客户")
    private java.lang.String secPushStatus;

    @ApiModelProperty(value = "第二次推送人ID")
    private java.lang.String secPushUserid;

    @ApiModelProperty(value = "第二次推送人")
    private java.lang.String secPushUsername;
    /**
     * {1:是,0:否}
     **/
    @ApiModelProperty(value = "是否主要违规行为")
    private java.lang.String mainFlag;

    @ApiModelProperty(value = "客户审查人ID")
    private java.lang.String cusReviewUserid;

    @ApiModelProperty(value = "客户审查人姓名")
    private java.lang.String cusReviewUsername;

    @ApiModelProperty(value = "客户审查时间")
    private java.lang.String cusReviewTime;

    @ApiModelProperty(value = "客户审查状态")
    private java.lang.String cusReviewStatus;

    @ApiModelProperty(value = "客户审查备注")
    private java.lang.String cusReviewRemark;

    @ApiModelProperty(value = "人工审核归属临床路径ID")
    private String clinicalId;
    @ApiModelProperty(value = "人工审核归属临床路径名称")
    private String clinicalName;

    @ApiModelProperty(value = "项目数量")
    private String itemQty;
    @ApiModelProperty(value = "违规范围")
    private List<String> ruleScope;
    @ApiModelProperty(value = "违规范围名称")
    private List<String> ruleScopeName;
    @ApiModelProperty(value = "冲突项目编码")
    private List<String> mutexItemCode;
    @ApiModelProperty(value = "冲突项目名称")
    private List<String> mutexItemName;

    @ApiModelProperty(value = "政策依据")
    private String ruleBasis;

    @ApiModelProperty(value = "最大基金支出金额")
    private String maxActionMoney;

    @ApiModelProperty(value = "项目编码_src")
    private String itemcodeSrc;
    @ApiModelProperty(value = "项目名称_src")
    private String itemnameSrc;
    @ApiModelProperty(value = "医院收费项目编码")
    private String hisItemcode;
    @ApiModelProperty(value = "医院收费项目名称")
    private String hisItemname;
    @ApiModelProperty(value = "医院收费项目编码（原始）")
    private String hisItemcodeSrc;
    @ApiModelProperty(value = "医院收费项目名称（原始）")
    private String hisItemnameSrc;



    @ApiModelProperty(value = "周期ID")
    private String issueId;
    @ApiModelProperty(value = "周期名称")
    private String issueName;
    @ApiModelProperty(value = "项目客户ID")
    private String xmkhId;
    @ApiModelProperty(value = "项目客户名称")
    private String xmkhName;
    @ApiModelProperty(value = "任务批次名称")
    private String taskBatchName;
    @ApiModelProperty(value = "0.待处理,1.已处理")
    private String handleStatus;

    /**住院天数*/
    @Excel(name = "住院天数", width = 15)
    @ApiModelProperty(value = "住院天数")
    private java.lang.String zyDays;
    /**住院天数计算值*/
    @Excel(name = "住院天数计算值", width = 15)
    @ApiModelProperty(value = "住院天数计算值")
    private java.lang.String zyDaysCalculate;

    @ApiModelProperty(value = "项目编码")
    private String itemcode;
    @ApiModelProperty(value = "项目名称")
    private String itemname;


    @ApiModelProperty(value = "合规ID")
    private String ruleId;
    @ApiModelProperty(value = "合规名称")
    private String ruleName;

    @ApiModelProperty(value = "最小违规金额")
    private String minMoney;
    @ApiModelProperty(value = "最大违规金额")
    private String maxMoney;

    @ApiModelProperty(value = "结伴组ID")
    private String togetherid;

    @ApiModelProperty(value = "结伴人数")
    private Double groupPatientQty;

    @ApiModelProperty(value = "结伴次数")
    private Double groupCnt;

    @ApiModelProperty(value = "扩展字段1")
    private String ext1;
    @ApiModelProperty(value = "扩展字段2")
    private String ext2;
    @ApiModelProperty(value = "扩展字段3")
    private String ext3;
    @ApiModelProperty(value = "扩展字段4")
    private String ext4;
    @ApiModelProperty(value = "扩展字段5")
    private String ext5;
    @ApiModelProperty(value = "扩展字段6")
    private String ext6;
    @ApiModelProperty(value = "扩展字段7")
    private String ext7;
    @ApiModelProperty(value = "扩展字段8")
    private String ext8;
    @ApiModelProperty(value = "扩展字段9")
    private String ext9;
    @ApiModelProperty(value = "扩展字段10")
    private String ext10;


    @ApiModelProperty(value = "AI识别的概率")
    private String probility;
    @ApiModelProperty(value = "AI识别的黑灰白结果")
    private String predictLabel;

    @ApiModelProperty(value = "规则级别")
    private String ruleGrade;
    @ApiModelProperty(value = "级别备注")
    private String ruleGradeRemark;

}
