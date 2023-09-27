package com.ai.modules.medical.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 药品合规规则
 * @Author: jeecg-boot
 * @Date:   2019-12-19
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_DRUG_RULE")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_DRUG_RULE对象", description="药品合规规则")
public class MedicalDrugRule {

	/**唯一编码*/
	@Excel(name = "唯一编码", width = 15)
    @ApiModelProperty(value = "唯一编码")
	@TableId("RULE_ID")
	private java.lang.String ruleId;
	/**药品编码(多值)*/
	@Excel(name = "药品编码(多值)", width = 15)
    @ApiModelProperty(value = "药品编码(多值)")
	private java.lang.String drugCode;

	@Excel(name = "药品来源类别(多值)", width = 15)
	@ApiModelProperty(value = "药品来源类别(多值)")
	private String drugTypes;
	/**限定范围*/
	@Excel(name = "限定范围", width = 15)
    @ApiModelProperty(value = "限定范围")
	@MedicalDict(dicCode = "LIMIT_SCOPE")
	private java.lang.String limitScope;
	/**性别(GB/T2261.1)*/
	@Excel(name = "性别", width = 15)
    @ApiModelProperty(value = "性别(GB/T2261.1)")
	@MedicalDict(dicCode = "GB/T2261.1")
	private java.lang.String sex;
	/**就医方式（BM_JZBZ00）*/
	@Excel(name = "就医方式", width = 15)
    @ApiModelProperty(value = "就医方式（BM_JZBZ00）")
	@MedicalDict(dicCode = "BM_JZBZ00")
	private java.lang.String jzlx;
	/**参保类型（WS364.1/CV07.10.003）*/
	@Excel(name = "参保类型", width = 15)
    @ApiModelProperty(value = "参保类型（WS364.1/CV07.10.003）")
//	@MedicalDict(dicCode = "WS364.1/CV07.10.003")
	private java.lang.String yblx;
	/**医院级别（YYJB）*/
	@Excel(name = "医院级别", width = 15)
    @ApiModelProperty(value = "医院级别（YYJB）")
	@MedicalDict(dicCode = "YYJB")
	private java.lang.String yyjb;
	@Excel(name = "科室", width = 15)
    @ApiModelProperty(value = "科室(HIS_YLJGZLKM)")
	private java.lang.String office;
	/**疗程用药剂量*/
	@Excel(name = "疗程用药剂量", width = 15)
    @ApiModelProperty(value = "疗程用药剂量")
	private java.lang.String courseDose;
	/**年度用药剂量*/
	@Excel(name = "年度用药剂量", width = 15)
    @ApiModelProperty(value = "年度用药剂量")
	private java.lang.String yearDose;
	/**治疗项目(kind=1)*/
	@Excel(name = "治疗项目", width = 15)
    @ApiModelProperty(value = "治疗项目(kind=1)")
	private java.lang.String treatProject;
	/**治疗方式(kind=2)*/
	@Excel(name = "治疗方式", width = 15)
    @ApiModelProperty(value = "治疗方式(kind=2)")
	private java.lang.String treatment;
	/**重复用药(kind=3)*/
	@Excel(name = "重复用药", width = 15)
    @ApiModelProperty(value = "重复用药(kind=3)")
	private java.lang.String repeatDrug;
	/**二线用药(kind=4)*/
	@Excel(name = "二线用药", width = 15)
    @ApiModelProperty(value = "二线用药(kind=4)")
	private java.lang.String twoLimitDrug;
	/**适应症(kind=5)*/
	@Excel(name = "适应症", width = 15)
    @ApiModelProperty(value = "适应症(kind=5)")
	private java.lang.String indication;
	/**提示信息*/
	@Excel(name = "提示信息", width = 15)
    @ApiModelProperty(value = "提示信息")
	private java.lang.String message;
	/**治疗用药(kind=7)*/
	@Excel(name = "治疗用药", width = 15)
    @ApiModelProperty(value = "治疗用药(kind=7)")
	private java.lang.String treatDrug;
	/**药品名称(多值)*/
	@Excel(name = "药品名称", width = 15)
    @ApiModelProperty(value = "药品名称(多值)")
	private java.lang.String drugNames;
	/**规则类型(1药品规则，2收费规则，3临床路径审核，4治疗规则)*/
	@Excel(name = "规则类型", width = 15)
    @ApiModelProperty(value = "规则类型(1药品规则，2收费规则，3临床路径审核，4治疗规则)")
	private java.lang.String ruleType;
	/**收费项目(多值)*/
	@Excel(name = "收费项目", width = 15)
    @ApiModelProperty(value = "收费项目(多值)")
	private java.lang.String chargeItems;
	/**频率*/
	@Excel(name = "频率", width = 15)
    @ApiModelProperty(value = "频率")
	private java.lang.String frequency;
	/**就诊周期（1:1次就诊 2:1日 3:1周 4:1月 5:1年 8:国家规定供暖期以外 9:国家规定暑期以外）*/
	@Excel(name = "就诊周期", width = 15)
    @ApiModelProperty(value = "就诊周期（1:1次就诊 2:1日 3:1周 4:1月 5:1年 8:国家规定供暖期以外 9:国家规定暑期以外）")
	@MedicalDict(dicCode = "FREQUENCY_PERIOD")
	private java.lang.String period;
	/**收费分类(多值)*/
	@Excel(name = "收费分类", width = 15)
    @ApiModelProperty(value = "收费分类(多值)")
	@MedicalDict(dicCode = "BM_CFLB00")
	private java.lang.String chargeTypes;
	/**住院天数下限*/
	@Excel(name = "住院天数下限", width = 15)
    @ApiModelProperty(value = "住院天数下限")
	private java.lang.String inhospitalMin;
	/**住院天数上限*/
	@Excel(name = "住院天数上限", width = 15)
    @ApiModelProperty(value = "住院天数上限")
	private java.lang.String inhospitalMax;
	/**入院前天数*/
	@Excel(name = "入院前天数", width = 15)
    @ApiModelProperty(value = "入院前天数")
	private java.lang.String beforeInhospital;
	/**临床项目路径*/
	@Excel(name = "临床项目路径", width = 15)
    @ApiModelProperty(value = "临床项目路径")
	private java.lang.String clinicProjects;
	/**疾病名称*/
	@Excel(name = "疾病名称", width = 15)
    @ApiModelProperty(value = "疾病名称")
	private java.lang.String diseaseNames;
	/**临床项目路径*/
	@Excel(name = "临床项目路径", width = 15)
    @ApiModelProperty(value = "临床项目路径")
	private java.lang.String beforeProjects;
	/**收费项目代码(多值)*/
	@Excel(name = "收费项目代码", width = 15)
    @ApiModelProperty(value = "收费项目代码(多值)")
	private java.lang.String chargeItemCodes;
	/**疾病代码*/
	@Excel(name = "疾病代码", width = 15)
    @ApiModelProperty(value = "疾病代码")
	private java.lang.String diseaseCodes;
	/**频率比较符*/
	@Excel(name = "频率比较符", width = 15)
    @ApiModelProperty(value = "频率比较符")
	private java.lang.String compare;

	/**门诊统筹*/
	@Excel(name = "门诊统筹", width = 15)
	@ApiModelProperty(value = "门诊统筹")
	@MedicalDict(dicCode = "YESNO")
	private java.lang.String outHospPlan;
	/**用药量限制*/
	@Excel(name = "用药量限制", width = 15)
	@ApiModelProperty(value = "用药量限制")
	private java.lang.String dosageLimit;
	/**用药量单位*/
	@Excel(name = "用药量单位", width = 15)
	@ApiModelProperty(value = "用药量单位")
	private java.lang.String dosageUnit;
	/**用药时限*/
	@Excel(name = "用药时限", width = 15)
	@ApiModelProperty(value = "用药时限")
	private java.lang.String takeTimeLimit;
	/**时间单位*/
	@Excel(name = "时间单位", width = 15)
	@ApiModelProperty(value = "时间单位")
	@MedicalDict(dicCode = "DRUG_TIME_UNIT")
	private java.lang.String timeUnit;
	/**年龄单位*/
	@Excel(name = "最大持续使用时间", width = 15)
	@ApiModelProperty(value = "最大持续使用时间")
	private java.lang.String maxKeepUseTime;
	/**最大持续时间单位*/
	@Excel(name = "最大持续时间单位", width = 15)
	@ApiModelProperty(value = "最大持续时间单位")
	@MedicalDict(dicCode = "DRUG_TIME_UNIT")
	private java.lang.String maxKeepTimeUnit;
	/**规则来源*/
	@Excel(name = "规则来源", width = 15)
	@ApiModelProperty(value = "规则来源")
	private java.lang.String ruleSource;
	/**卫生机构类别*/
	@Excel(name = "卫生机构类别", width = 15)
	@ApiModelProperty(value = "卫生机构类别")
	private java.lang.String healthOrgKind;
	/**合用不予支付药品*/
	@Excel(name = "合用不予支付药品", width = 15)
	@ApiModelProperty(value = "合用不予支付药品")
	private java.lang.String twoLimitDrug2;
	/**医嘱*/
	@Excel(name = "医嘱", width = 15)
	@ApiModelProperty(value = "医嘱")
	private java.lang.String docAdvice;

	/**修改人用户名*/
	@Excel(name = "修改人用户名", width = 15)
	@ApiModelProperty(value = "修改人用户名")
	private java.lang.String updateUser;
	/**修改人姓名*/
	@Excel(name = "修改人姓名", width = 15)
	@ApiModelProperty(value = "修改人姓名")
	private java.lang.String updateUsername;
	/**修改时间*/
	@Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "修改时间")
	private java.util.Date updateTime;
	/**创建人用户名*/
	@Excel(name = "创建人用户名", width = 15)
	@ApiModelProperty(value = "创建人用户名")
	private java.lang.String createUser;
	/**创建人姓名*/
	@Excel(name = "创建人姓名", width = 15)
	@ApiModelProperty(value = "创建人姓名")
	private java.lang.String createUsername;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;

	/**合规项目组*/
	@Excel(name = "合规项目组", width = 15)
	@ApiModelProperty(value = "合规项目组")
	private java.lang.String fitGroupCodes;

	/**互斥项目组*/
	@Excel(name = "互斥项目组", width = 15)
	@ApiModelProperty(value = "互斥项目组")
	private java.lang.String unfitGroupCodes;

	/**一日互斥项目组*/
	@Excel(name = "一日互斥项目组", width = 15)
	@ApiModelProperty(value = "一日互斥项目组")
	private java.lang.String unfitGroupCodesDay;

	/**政策依据*/
	@Excel(name = "政策依据", width = 15)
	@ApiModelProperty(value = "政策依据")
	private java.lang.String ruleBasis;

    @Excel(name = "禁忌症", width = 15)
    @ApiModelProperty(value = "禁忌症")
    private java.lang.String unIndication;

    @Excel(name = "不能报销", width = 15)
    @ApiModelProperty(value = "不能报销")
	@MedicalDict(dicCode = "YESNO")
    private java.lang.String unExpense;

    @Excel(name = "医疗机构", width = 15)
    @ApiModelProperty(value = "医疗机构")
    private java.lang.String org;

    @Excel(name = "给药途径", width = 15)
    @ApiModelProperty(value = "给药途径")
    private java.lang.String drugUsage;

    @Excel(name = "不能收费", width = 15)
    @ApiModelProperty(value = "不能收费")
	@MedicalDict(dicCode = "YESNO")
    private java.lang.String unCharge;

	@Excel(name = "支付时长就诊周期", width = 15)
	@ApiModelProperty(value = "支付时长就诊周期")
	@MedicalDict(dicCode = "VISIT_PERIOD")
	private java.lang.String payDurationPeriod;

	@Excel(name = "支付时长", width = 15)
	@ApiModelProperty(value = "支付时长")
	private java.lang.Integer payDuration;

	@Excel(name = "支付时长单位", width = 15)
	@ApiModelProperty(value = "支付时长单位")
	@MedicalDict(dicCode = "AGE_UNIT")
	private java.lang.String payDurationUnit;

	@Excel(name = "数量就诊周期", width = 15)
	@ApiModelProperty(value = "数量就诊周期")
	@MedicalDict(dicCode = "VISIT_PERIOD")
	private java.lang.String countPeriod;

	@Excel(name = "数量", width = 15)
	@ApiModelProperty(value = "数量")
	private java.lang.Integer count;

	@Excel(name = "年龄", width = 15)
	@ApiModelProperty(value = "年龄")
	@MedicalDict(dicCode = "AGE_RANGE")
	private java.lang.String age;

	@Excel(name = "年龄下限", width = 15)
	@ApiModelProperty(value = "年龄下限")
	private java.lang.Integer ageLow;

	@Excel(name = "年龄上限", width = 15)
	@ApiModelProperty(value = "年龄上限")
	private java.lang.Integer ageHigh;

	@Excel(name = "年龄下限比较符", width = 15)
	@ApiModelProperty(value = "年龄下限比较符")
	private java.lang.String ageLowCompare;

	@Excel(name = "年龄上限比较符", width = 15)
	@ApiModelProperty(value = "年龄上限比较符")
	private java.lang.String ageHighCompare;

	@Excel(name = "年龄单位", width = 15)
	@ApiModelProperty(value = "年龄单位")
	@MedicalDict(dicCode = "AGE_UNIT")
	private java.lang.String ageUnit;

	@Excel(name = "频率疾病组频率", width = 15)
	@ApiModelProperty(value = "频率疾病组频率")
	private java.lang.String diseasegroupFreq;

	@Excel(name = "频率疾病组编码", width = 15)
	@ApiModelProperty(value = "频率疾病组编码")
	private java.lang.String diseasegroupCodes;

	/**频率*/
	@Excel(name = "频率2", width = 15)
    @ApiModelProperty(value = "频率2")
	private java.lang.String twoFrequency;
	/**就诊周期（1:1次就诊 2:1日 3:1周 4:1月 5:1年 8:国家规定供暖期以外 9:国家规定暑期以外）*/
	@Excel(name = "就诊周期2", width = 15)
    @ApiModelProperty(value = "就诊周期2")
	@MedicalDict(dicCode = "FREQUENCY_PERIOD")
	private java.lang.String twoPeriod;
	/**频率比较符*/
	@Excel(name = "频率比较符2", width = 15)
    @ApiModelProperty(value = "频率比较符2")
	private java.lang.String twoCompare;

	/**开始时间*/
	@Excel(name = "开始时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "开始时间")
	private Date startTime;
	/**开始时间*/
	@Excel(name = "结束时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "结束时间")
	private Date endTime;
	/**不合理行为ID*/
	@Excel(name = "不合理行为ID", width = 15)
	@ApiModelProperty(value = "不合理行为ID")
	@MedicalDict(dicCode = "ACTION_LIST")
	private java.lang.String actionId;
	@Excel(name = "不合理行为名称", width = 15)
	@ApiModelProperty(value = "不合理行为名称")
	private java.lang.String actionName;
	/**不合理行为类型*/
	@Excel(name = "不合理行为类型", width = 15)
	@ApiModelProperty(value = "不合理行为类型")
	@MedicalDict(dicCode = "ACTION_TYPE")
	private java.lang.String actionType;

	@Excel(name = "项目类型 ITEM项目、GROUP项目组", width = 15)
	@ApiModelProperty(value = "项目类型 ITEM项目、GROUP项目组")
	private java.lang.String testResultItemType;

	@Excel(name = "项目编码", width = 15)
	@ApiModelProperty(value = "项目编码")
	private java.lang.String testResultItemCode;
	@Excel(name = "项目名称", width = 15)
	@ApiModelProperty(value = "项目名称")
	private java.lang.String testResultItemName;

	@Excel(name = "值类型 1定量 2定性", width = 15)
	@ApiModelProperty(value = "值类型 1定量 2定性")
	private java.lang.String testResultValueType;

	@Excel(name = "值 定量为数学范围表达式", width = 15)
	@ApiModelProperty(value = "值 定量为数学范围表达式")
	private java.lang.String testResultValue;

	@Excel(name = "定量值单位", width = 15)
	@ApiModelProperty(value = "定量值单位")
	private java.lang.String testResultUnit;


}
