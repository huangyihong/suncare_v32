package com.ai.modules.review.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 风控结果表
 * @Author: jeecg-boot
 * @Date:   2022-12-24
 * @Version: V1.0
 */
@Data
@TableName("medical_unreasonable_action")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="medical_unreasonable_action对象", description="风控结果表")
public class MedicalUnreasonableAction {

	/**不合规行为释义*/
	@Excel(name = "不合规行为释义", width = 15)
    @ApiModelProperty(value = "不合规行为释义")
	private java.lang.String actionDesc;
	/**不合规行为ID*/
	@Excel(name = "不合规行为ID", width = 15)
    @ApiModelProperty(value = "不合规行为ID")
	private java.lang.String actionId;
	/**最小基金支出金额*/
	@Excel(name = "最小基金支出金额", width = 15)
    @ApiModelProperty(value = "最小基金支出金额")
	private java.math.BigDecimal actionMoney;
	/**不合规行为名称*/
	@Excel(name = "不合规行为名称", width = 15)
    @ApiModelProperty(value = "不合规行为名称")
	private java.lang.String actionName;
	/**不合规行为类型ID*/
	@Excel(name = "不合规行为类型ID", width = 15)
    @ApiModelProperty(value = "不合规行为类型ID")
	private java.lang.String actionTypeId;
	/**不合规行为类型名称*/
	@Excel(name = "不合规行为类型名称", width = 15)
    @ApiModelProperty(value = "不合规行为类型名称")
	private java.lang.String actionTypeName;
	/**项目频次/数量*/
	@Excel(name = "项目频次/数量", width = 15)
    @ApiModelProperty(value = "项目频次/数量")
	private java.lang.Integer aiItemCnt;
	/**超出频次/数量*/
	@Excel(name = "超出频次/数量", width = 15)
    @ApiModelProperty(value = "超出频次/数量")
	private java.lang.Integer aiOutCnt;
	/**项目批次号*/
	@Excel(name = "项目批次号", width = 15)
    @ApiModelProperty(value = "项目批次号")
	private java.lang.String batchId;
	/**出生日期*/
	@Excel(name = "出生日期", width = 15)
    @ApiModelProperty(value = "出生日期")
	private java.lang.String birthday;
	/**违规内容(关联项目内容)*/
	@Excel(name = "违规内容(关联项目内容)", width = 15)
    @ApiModelProperty(value = "违规内容(关联项目内容)")
	private java.lang.String breakRuleContent;
	/**违规说明*/
	@Excel(name = "违规说明", width = 15)
    @ApiModelProperty(value = "违规说明")
	private java.lang.String breakState;
	/**不合规行为级别*/
	@Excel(name = "不合规行为级别", width = 15)
    @ApiModelProperty(value = "不合规行为级别")
	private java.lang.String busiType;
	/**模型ID，临床路径ID，药品ID，收费项目ID等*/
	@Excel(name = "模型ID，临床路径ID，药品ID，收费项目ID等", width = 15)
    @ApiModelProperty(value = "模型ID，临床路径ID，药品ID，收费项目ID等")
	private java.lang.String caseId;
	/**模型名称，临床路径名称，药品名称，收费项目名称等*/
	@Excel(name = "模型名称，临床路径名称，药品名称，收费项目名称等", width = 15)
    @ApiModelProperty(value = "模型名称，临床路径名称，药品名称，收费项目名称等")
	private java.lang.String caseName;
	/**模型得分*/
	@Excel(name = "模型得分", width = 15)
    @ApiModelProperty(value = "模型得分")
	private java.math.BigDecimal caseScore;
	/**收费类别名称*/
	@Excel(name = "收费类别名称", width = 15)
    @ApiModelProperty(value = "收费类别名称")
	private java.lang.String chargeclass;
	/**收费类别编码*/
	@Excel(name = "收费类别编码", width = 15)
    @ApiModelProperty(value = "收费类别编码")
	private java.lang.String chargeclassId;
	/**收费日期*/
	@Excel(name = "收费日期", width = 15)
    @ApiModelProperty(value = "收费日期")
	private java.lang.String chargedate;
	/**yx患者编号*/
	@Excel(name = "yx患者编号", width = 15)
    @ApiModelProperty(value = "yx患者编号")
	private java.lang.String clientid;
	/**患者姓名*/
	@Excel(name = "患者姓名", width = 15)
    @ApiModelProperty(value = "患者姓名")
	private java.lang.String clientname;
	/**临床路径药品范围外金额（预留）*/
	@Excel(name = "临床路径药品范围外金额（预留）", width = 15)
    @ApiModelProperty(value = "临床路径药品范围外金额（预留）")
	private java.math.BigDecimal clinicalDrugBeyondMoney;
	/**临床路径药品范围外金额占比（预留）*/
	@Excel(name = "临床路径药品范围外金额占比（预留）", width = 15)
    @ApiModelProperty(value = "临床路径药品范围外金额占比（预留）")
	private java.math.BigDecimal clinicalDrugBeyondMoneyRatio;
	/**临床路径药品范围内金额*/
	@Excel(name = "临床路径药品范围内金额", width = 15)
    @ApiModelProperty(value = "临床路径药品范围内金额")
	private java.math.BigDecimal clinicalDrugMoney;
	/**临床路径药品范围内金额占比*/
	@Excel(name = "临床路径药品范围内金额占比", width = 15)
    @ApiModelProperty(value = "临床路径药品范围内金额占比")
	private java.math.BigDecimal clinicalDrugMoneyRatio;
	/**满足的临床路径准入条件组ID*/
	@Excel(name = "满足的临床路径准入条件组ID", width = 15)
    @ApiModelProperty(value = "满足的临床路径准入条件组ID")
	private java.lang.String clinicalGroupIds;
	/**满足的临床路径准入条件组名称*/
	@Excel(name = "满足的临床路径准入条件组名称", width = 15)
    @ApiModelProperty(value = "满足的临床路径准入条件组名称")
	private java.lang.String clinicalGroupNames;
	/**临床路径项目范围外金额（预留）*/
	@Excel(name = "临床路径项目范围外金额（预留）", width = 15)
    @ApiModelProperty(value = "临床路径项目范围外金额（预留）")
	private java.math.BigDecimal clinicalTreatBeyondMoney;
	/**临床路径项目范围外金额占比（预留）*/
	@Excel(name = "临床路径项目范围外金额占比（预留）", width = 15)
    @ApiModelProperty(value = "临床路径项目范围外金额占比（预留）")
	private java.math.BigDecimal clinicalTreatBeyondMoneyRatio;
	/**临床路径项目范围内金额*/
	@Excel(name = "临床路径项目范围内金额", width = 15)
    @ApiModelProperty(value = "临床路径项目范围内金额")
	private java.math.BigDecimal clinicalTreatMoney;
	/**临床路径项目范围内金额占比*/
	@Excel(name = "临床路径项目范围内金额占比", width = 15)
    @ApiModelProperty(value = "临床路径项目范围内金额占比")
	private java.math.BigDecimal clinicalTreatMoneyRatio;
	/**客户审查备注*/
	@Excel(name = "客户审查备注", width = 15)
    @ApiModelProperty(value = "客户审查备注")
	private java.lang.String cusReviewRemark;
	/**客户审查状态{init:未处理,wait:待审查,begin:审查中,exclude:排除,sure:确认,reject:驳回}*/
	@Excel(name = "客户审查状态{init:未处理,wait:待审查,begin:审查中,exclude:排除,sure:确认,reject:驳回}", width = 15)
    @ApiModelProperty(value = "客户审查状态{init:未处理,wait:待审查,begin:审查中,exclude:排除,sure:确认,reject:驳回}")
	private java.lang.String cusReviewStatus;
	/**客户审查时间*/
	@Excel(name = "客户审查时间", width = 15)
    @ApiModelProperty(value = "客户审查时间")
	private java.lang.String cusReviewTime;
	/**客户审查人ID*/
	@Excel(name = "客户审查人ID", width = 15)
    @ApiModelProperty(value = "客户审查人ID")
	private java.lang.String cusReviewUserid;
	/**客户审查人姓名*/
	@Excel(name = "客户审查人姓名", width = 15)
    @ApiModelProperty(value = "客户审查人姓名")
	private java.lang.String cusReviewUsername;
	/**数据来源名称*/
	@Excel(name = "数据来源名称", width = 15)
    @ApiModelProperty(value = "数据来源名称")
	private java.lang.String dataResouce;
	/**数据来源编码*/
	@Excel(name = "数据来源编码", width = 15)
    @ApiModelProperty(value = "数据来源编码")
	private java.lang.String dataResouceId;
	/**年龄（天）*/
	@Excel(name = "年龄（天）", width = 15)
    @ApiModelProperty(value = "年龄（天）")
	private java.lang.Integer dayage;
	/**就诊科室编码*/
	@Excel(name = "就诊科室编码", width = 15)
    @ApiModelProperty(value = "就诊科室编码")
	private java.lang.String deptid;
	/**就诊科室编码_src*/
	@Excel(name = "就诊科室编码_src", width = 15)
    @ApiModelProperty(value = "就诊科室编码_src")
	private java.lang.String deptidSrc;
	/**就诊科室名称*/
	@Excel(name = "就诊科室名称", width = 15)
    @ApiModelProperty(value = "就诊科室名称")
	private java.lang.String deptname;
	/**就诊科室名称_src*/
	@Excel(name = "就诊科室名称_src", width = 15)
    @ApiModelProperty(value = "就诊科室名称_src")
	private java.lang.String deptnameSrc;
	/**疾病编码*/
	@Excel(name = "疾病编码", width = 15)
    @ApiModelProperty(value = "疾病编码")
	private java.lang.String diseasecode;
	/**疾病编码（原始）*/
	@Excel(name = "疾病编码（原始）", width = 15)
    @ApiModelProperty(value = "疾病编码（原始）")
	private java.lang.String diseasecodeSrc;
	/**疾病名称*/
	@Excel(name = "疾病名称", width = 15)
    @ApiModelProperty(value = "疾病名称")
	private java.lang.String diseasename;
	/**疾病名称（原始）*/
	@Excel(name = "疾病名称（原始）", width = 15)
    @ApiModelProperty(value = "疾病名称（原始）")
	private java.lang.String diseasenameSrc;
	/**就诊医师编码*/
	@Excel(name = "就诊医师编码", width = 15)
    @ApiModelProperty(value = "就诊医师编码")
	private java.lang.String doctorid;
	/**就诊医师姓名*/
	@Excel(name = "就诊医师姓名", width = 15)
    @ApiModelProperty(value = "就诊医师姓名")
	private java.lang.String doctorname;
	/**etl来源编码*/
	@Excel(name = "etl来源编码", width = 15)
    @ApiModelProperty(value = "etl来源编码")
	private java.lang.String etlSource;
	/**etl来源名称*/
	@Excel(name = "etl来源名称", width = 15)
    @ApiModelProperty(value = "etl来源名称")
	private java.lang.String etlSourceName;
	/**etl时间*/
	@Excel(name = "etl时间", width = 15)
    @ApiModelProperty(value = "etl时间")
	private java.lang.String etlTime;
	/**汇总字段预留*/
	@Excel(name = "汇总字段预留", width = 15)
    @ApiModelProperty(value = "汇总字段预留")
	private java.lang.String ext1;
	/**汇总字段预留*/
	@Excel(name = "汇总字段预留", width = 15)
    @ApiModelProperty(value = "汇总字段预留")
	private java.lang.String ext10;
	/**汇总字段预留*/
	@Excel(name = "汇总字段预留", width = 15)
    @ApiModelProperty(value = "汇总字段预留")
	private java.lang.String ext2;
	/**汇总字段预留*/
	@Excel(name = "汇总字段预留", width = 15)
    @ApiModelProperty(value = "汇总字段预留")
	private java.lang.String ext3;
	/**汇总字段预留*/
	@Excel(name = "汇总字段预留", width = 15)
    @ApiModelProperty(value = "汇总字段预留")
	private java.lang.String ext4;
	/**汇总字段预留*/
	@Excel(name = "汇总字段预留", width = 15)
    @ApiModelProperty(value = "汇总字段预留")
	private java.lang.String ext5;
	/**汇总字段预留*/
	@Excel(name = "汇总字段预留", width = 15)
    @ApiModelProperty(value = "汇总字段预留")
	private java.lang.String ext6;
	/**汇总字段预留*/
	@Excel(name = "汇总字段预留", width = 15)
    @ApiModelProperty(value = "汇总字段预留")
	private java.lang.String ext7;
	/**汇总字段预留*/
	@Excel(name = "汇总字段预留", width = 15)
    @ApiModelProperty(value = "汇总字段预留")
	private java.lang.String ext8;
	/**汇总字段预留*/
	@Excel(name = "汇总字段预留", width = 15)
    @ApiModelProperty(value = "汇总字段预留")
	private java.lang.String ext9;
	/**初审归类*/
	@Excel(name = "初审归类", width = 15)
    @ApiModelProperty(value = "初审归类")
	private java.lang.String firReviewClassify;
	/**初审备注*/
	@Excel(name = "初审备注", width = 15)
    @ApiModelProperty(value = "初审备注")
	private java.lang.String firReviewRemark;
	/**初审状态{init:待处理,white:白名单,blank:黑名单,grey:灰名单}*/
	@Excel(name = "初审状态{init:待处理,white:白名单,blank:黑名单,grey:灰名单}", width = 15)
    @ApiModelProperty(value = "初审状态{init:待处理,white:白名单,blank:黑名单,grey:灰名单}")
	private java.lang.String firReviewStatus;
	/**初审时间*/
	@Excel(name = "初审时间", width = 15)
    @ApiModelProperty(value = "初审时间")
	private java.lang.String firReviewTime;
	/**初审人ID*/
	@Excel(name = "初审人ID", width = 15)
    @ApiModelProperty(value = "初审人ID")
	private java.lang.String firReviewUserid;
	/**初审人姓名*/
	@Excel(name = "初审人姓名", width = 15)
    @ApiModelProperty(value = "初审人姓名")
	private java.lang.String firReviewUsername;
	/**医保结算方式代码*/
	@Excel(name = "医保结算方式代码", width = 15)
    @ApiModelProperty(value = "医保结算方式代码")
	private java.lang.String funSettlewayId;
	/**医保结算方式名称*/
	@Excel(name = "医保结算方式名称", width = 15)
    @ApiModelProperty(value = "医保结算方式名称")
	private java.lang.String funSettlewayName;
	/**医保基金支出金额*/
	@Excel(name = "医保基金支出金额", width = 15)
    @ApiModelProperty(value = "医保基金支出金额")
	private java.math.BigDecimal fundCover;
	/**本次基金支付金额*/
	@Excel(name = "本次基金支付金额", width = 15)
    @ApiModelProperty(value = "本次基金支付金额")
	private java.math.BigDecimal fundpay;
	/**数据生成时间*/
	@Excel(name = "数据生成时间", width = 15)
    @ApiModelProperty(value = "数据生成时间")
	private java.lang.String genDataTime;
	/**结伴次数*/
	@Excel(name = "结伴次数", width = 15)
    @ApiModelProperty(value = "结伴次数")
	private java.lang.Integer groupCnt;
	/**结伴人数*/
	@Excel(name = "结伴人数", width = 15)
    @ApiModelProperty(value = "结伴人数")
	private java.lang.Integer groupPatientQty;
	/**处理状态0.待处理,1.已处理*/
	@Excel(name = "处理状态0.待处理,1.已处理", width = 15)
    @ApiModelProperty(value = "处理状态0.待处理,1.已处理")
	private java.lang.String handleStatus;
	/**医院收费项目编码*/
	@Excel(name = "医院收费项目编码", width = 15)
    @ApiModelProperty(value = "医院收费项目编码")
	private java.lang.String hisItemcode;
	/**医院收费项目编码（原始）*/
	@Excel(name = "医院收费项目编码（原始）", width = 15)
    @ApiModelProperty(value = "医院收费项目编码（原始）")
	private java.lang.String hisItemcodeSrc;
	/**医院收费项目名称*/
	@Excel(name = "医院收费项目名称", width = 15)
    @ApiModelProperty(value = "医院收费项目名称")
	private java.lang.String hisItemname;
	/**医院收费项目名称（原始）*/
	@Excel(name = "医院收费项目名称（原始）", width = 15)
    @ApiModelProperty(value = "医院收费项目名称（原始）")
	private java.lang.String hisItemnameSrc;
	/**his就诊id*/
	@Excel(name = "his就诊id", width = 15)
    @ApiModelProperty(value = "his就诊id")
	private java.lang.String hisVisitid;
	/**his就诊id_src*/
	@Excel(name = "his就诊id_src", width = 15)
    @ApiModelProperty(value = "his就诊id_src")
	private java.lang.String hisVisitidSrc;
	/**医疗机构等级*/
	@Excel(name = "医疗机构等级", width = 15)
    @ApiModelProperty(value = "医疗机构等级")
	@MedicalDict(dicCode = "YYDJ")
	private java.lang.String hospgrade;
	/**医院级别*/
	@Excel(name = "医院级别", width = 15)
    @ApiModelProperty(value = "医院级别")
	@MedicalDict(dicCode = "YYJB")
	private java.lang.String hosplevel;
	/**主键*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键")
	private java.lang.String id;
	/**身份证件号码*/
	@Excel(name = "身份证件号码", width = 15)
    @ApiModelProperty(value = "身份证件号码")
	private java.lang.String idNo;
	/**参保类别*/
	@Excel(name = "参保类别", width = 15)
    @ApiModelProperty(value = "参保类别")
	private java.lang.String insurancetype;
	/**周期ID*/
	@Excel(name = "周期ID", width = 15)
    @ApiModelProperty(value = "周期ID")
	private java.lang.String issueId;
	/**周期名称*/
	@Excel(name = "周期名称", width = 15)
    @ApiModelProperty(value = "周期名称")
	private java.lang.String issueName;
	/**项目费用*/
	@Excel(name = "项目费用", width = 15)
    @ApiModelProperty(value = "项目费用")
	private java.math.BigDecimal itemAmt;
	/**关联ID*/
	@Excel(name = "关联ID", width = 15)
    @ApiModelProperty(value = "关联ID")
	private java.lang.String itemId;
	/**项目数量*/
	@Excel(name = "项目数量", width = 15)
    @ApiModelProperty(value = "项目数量")
	private java.lang.Integer itemQty;
	/**项目编码*/
	@Excel(name = "项目编码", width = 15)
    @ApiModelProperty(value = "项目编码")
	private java.lang.String itemcode;
	/**项目编码_src*/
	@Excel(name = "项目编码_src", width = 15)
    @ApiModelProperty(value = "项目编码_src")
	private java.lang.String itemcodeSrc;
	/**项目名称*/
	@Excel(name = "项目名称", width = 15)
    @ApiModelProperty(value = "项目名称")
	private java.lang.String itemname;
	/**项目名称_src*/
	@Excel(name = "项目名称_src", width = 15)
    @ApiModelProperty(value = "项目名称_src")
	private java.lang.String itemnameSrc;
	/**项目最高单价*/
	@Excel(name = "项目最高单价", width = 15)
    @ApiModelProperty(value = "项目最高单价")
	private java.math.BigDecimal itempriceMax;
	/**出院日期*/
	@Excel(name = "出院日期", width = 15)
    @ApiModelProperty(value = "出院日期")
	private java.lang.String leavedate;
	/**是否主要违规行为{1:是,0:否}*/
	@Excel(name = "是否主要违规行为{1:是,0:否}", width = 15)
    @ApiModelProperty(value = "是否主要违规行为{1:是,0:否}")
	private java.lang.String mainFlag;
	/**最大基金支出金额*/
	@Excel(name = "最大基金支出金额", width = 15)
    @ApiModelProperty(value = "最大基金支出金额")
	private java.math.BigDecimal maxActionMoney;
	/**最大违规金额*/
	@Excel(name = "最大违规金额", width = 15)
    @ApiModelProperty(value = "最大违规金额")
	private java.math.BigDecimal maxMoney;
	/**病案号DWB_MASTER_INFO.CASE_ID*/
	@Excel(name = "病案号DWB_MASTER_INFO.CASE_ID", width = 15)
    @ApiModelProperty(value = "病案号DWB_MASTER_INFO.CASE_ID")
	private java.lang.String medicalNo;
	/**最小违规金额*/
	@Excel(name = "最小违规金额", width = 15)
    @ApiModelProperty(value = "最小违规金额")
	private java.math.BigDecimal minMoney;
	/**年龄（月）*/
	@Excel(name = "年龄（月）", width = 15)
    @ApiModelProperty(value = "年龄（月）")
	private java.lang.Integer monthage;
	/**冲突项目编码*/
	@Excel(name = "冲突项目编码", width = 15)
    @ApiModelProperty(value = "冲突项目编码")
	private java.lang.String mutexItemCode;
	/**冲突项目名称*/
	@Excel(name = "冲突项目名称", width = 15)
    @ApiModelProperty(value = "冲突项目名称")
	private java.lang.String mutexItemName;
	/**就诊医疗机构编码*/
	@Excel(name = "就诊医疗机构编码", width = 15)
    @ApiModelProperty(value = "就诊医疗机构编码")
	private java.lang.String orgid;
	/**就诊医疗机构编码_src*/
	@Excel(name = "就诊医疗机构编码_src", width = 15)
    @ApiModelProperty(value = "就诊医疗机构编码_src")
	private java.lang.String orgidSrc;
	/**就诊医疗机构名称*/
	@Excel(name = "就诊医疗机构名称", width = 15)
    @ApiModelProperty(value = "就诊医疗机构名称")
	private java.lang.String orgname;
	/**就诊医疗机构名称_src*/
	@Excel(name = "就诊医疗机构名称_src", width = 15)
    @ApiModelProperty(value = "就诊医疗机构名称_src")
	private java.lang.String orgnameSrc;
	/**医疗机构类型*/
	@Excel(name = "医疗机构类型", width = 15)
    @ApiModelProperty(value = "医疗机构类型")
	private java.lang.String orgtype;
	/**医疗机构类型编码*/
	@Excel(name = "医疗机构类型编码", width = 15)
    @ApiModelProperty(value = "医疗机构类型编码")
	private java.lang.String orgtypeCode;
	/**病理诊断名称*/
	@Excel(name = "病理诊断名称", width = 15)
    @ApiModelProperty(value = "病理诊断名称")
	private java.lang.String pathonogyDisease;
	/**病理诊断疾病编码*/
	@Excel(name = "病理诊断疾病编码", width = 15)
    @ApiModelProperty(value = "病理诊断疾病编码")
	private java.lang.String pathonogyDiseasecode;
	/**AI识别的黑灰白结果 white:白名单,blank:黑名单,grey:灰名单*/
	@Excel(name = "AI识别的黑灰白结果 white:白名单,blank:黑名单,grey:灰名单", width = 15)
    @ApiModelProperty(value = "AI识别的黑灰白结果 white:白名单,blank:黑名单,grey:灰名单")
	private java.lang.String predictLabel;
	/**AI识别的概率*/
	@Excel(name = "AI识别的概率", width = 15)
    @ApiModelProperty(value = "AI识别的概率")
	private java.math.BigDecimal probility;
	/**项目地*/
	@Excel(name = "项目地", width = 15)
    @ApiModelProperty(value = "项目地")
	private java.lang.String project;
	/**项目ID*/
	@Excel(name = "项目ID", width = 15)
    @ApiModelProperty(value = "项目ID")
	private java.lang.String projectId;
	/**项目名称*/
	@Excel(name = "项目名称", width = 15)
    @ApiModelProperty(value = "项目名称")
	private java.lang.String projectName;
	/**满足条件的疾病信息*/
	@Excel(name = "满足条件的疾病信息", width = 15)
    @ApiModelProperty(value = "满足条件的疾病信息")
	private java.lang.String proofDiag;
	/**满足条件的药品信息*/
	@Excel(name = "满足条件的药品信息", width = 15)
    @ApiModelProperty(value = "满足条件的药品信息")
	private java.lang.String proofDrug;
	/**满足条件的检查项目信息*/
	@Excel(name = "满足条件的检查项目信息", width = 15)
    @ApiModelProperty(value = "满足条件的检查项目信息")
	private java.lang.String proofTreat;
	/**初审是否推送{1:是,0:否}*/
	@Excel(name = "初审是否推送{1:是,0:否}", width = 15)
    @ApiModelProperty(value = "初审是否推送{1:是,0:否}")
	private java.lang.String pushStatus;
	/**推送时间*/
	@Excel(name = "推送时间", width = 15)
    @ApiModelProperty(value = "推送时间")
	private java.lang.String pushTime;
	/**推送人ID*/
	@Excel(name = "推送人ID", width = 15)
    @ApiModelProperty(value = "推送人ID")
	private java.lang.String pushUserid;
	/**推送人*/
	@Excel(name = "推送人", width = 15)
    @ApiModelProperty(value = "推送人")
	private java.lang.String pushUsername;
	/**审核名称*/
	@Excel(name = "审核名称", width = 15)
    @ApiModelProperty(value = "审核名称")
	private java.lang.String reviewName;
	/**规则依据*/
	@Excel(name = "规则依据", width = 15)
    @ApiModelProperty(value = "规则依据")
	private java.lang.String ruleBasis;
	/**合规名称*/
	@Excel(name = "合规名称", width = 15)
    @ApiModelProperty(value = "合规名称")
	private java.lang.String ruleFname;
	/**规则级别*/
	@Excel(name = "规则级别", width = 15)
    @ApiModelProperty(value = "规则级别")
	private java.lang.String ruleGrade;
	/**规则级别备注*/
	@Excel(name = "规则级别备注", width = 15)
    @ApiModelProperty(value = "规则级别备注")
	private java.lang.String ruleGradeRemark;
	/**合规ID*/
	@Excel(name = "合规ID", width = 15)
    @ApiModelProperty(value = "合规ID")
	private java.lang.String ruleId;
	/**不合规行为级别*/
	@Excel(name = "不合规行为级别", width = 15)
    @ApiModelProperty(value = "不合规行为级别")
	private java.lang.String ruleLevel;
	/**规则限定类型*/
	@Excel(name = "规则限定类型", width = 15)
    @ApiModelProperty(value = "规则限定类型")
	private java.lang.String ruleLimit;
	/**违规范围*/
	@Excel(name = "违规范围", width = 15)
    @ApiModelProperty(value = "违规范围")
	private java.lang.String ruleScope;
	/**违规范围名称*/
	@Excel(name = "违规范围名称", width = 15)
    @ApiModelProperty(value = "违规范围名称")
	private java.lang.String ruleScopeName;
	/**第二次是否推送客户{1:是,0:否}*/
	@Excel(name = "第二次是否推送客户{1:是,0:否}", width = 15)
    @ApiModelProperty(value = "第二次是否推送客户{1:是,0:否}")
	private java.lang.String secPushStatus;
	/**第二次推送人*/
	@Excel(name = "第二次推送人", width = 15)
    @ApiModelProperty(value = "第二次推送人")
	private java.lang.String secPushTime;
	/**第二次推送人ID*/
	@Excel(name = "第二次推送人ID", width = 15)
    @ApiModelProperty(value = "第二次推送人ID")
	private java.lang.String secPushUserid;
	/**第二次推送人*/
	@Excel(name = "第二次推送人", width = 15)
    @ApiModelProperty(value = "第二次推送人")
	private java.lang.String secPushUsername;
	/**第二次审核备注*/
	@Excel(name = "第二次审核备注", width = 15)
    @ApiModelProperty(value = "第二次审核备注")
	private java.lang.String secReviewClassify;
	/**第二次审核备注*/
	@Excel(name = "第二次审核备注", width = 15)
    @ApiModelProperty(value = "第二次审核备注")
	private java.lang.String secReviewRemark;
	/**第二次审核状态{init:未处理,wait:待审查,begin:审查中,exclude:排除,end:审查结束,reject:驳回,recover:收回}*/
	@Excel(name = "第二次审核状态{init:未处理,wait:待审查,begin:审查中,exclude:排除,end:审查结束,reject:驳回,recover:收回}", width = 15)
    @ApiModelProperty(value = "第二次审核状态{init:未处理,wait:待审查,begin:审查中,exclude:排除,end:审查结束,reject:驳回,recover:收回}")
	private java.lang.String secReviewStatus;
	/**第二次审核时间*/
	@Excel(name = "第二次审核时间", width = 15)
    @ApiModelProperty(value = "第二次审核时间")
	private java.lang.String secReviewTime;
	/**第二次审核人ID*/
	@Excel(name = "第二次审核人ID", width = 15)
    @ApiModelProperty(value = "第二次审核人ID")
	private java.lang.String secReviewUserid;
	/**第二次审核人姓名*/
	@Excel(name = "第二次审核人姓名", width = 15)
    @ApiModelProperty(value = "第二次审核人姓名")
	private java.lang.String secReviewUsername;
	/**项目最低自付比例*/
	@Excel(name = "项目最低自付比例", width = 15)
    @ApiModelProperty(value = "项目最低自付比例")
	private java.math.BigDecimal selfpayPropMin;
	/**性别名称*/
	@Excel(name = "性别名称", width = 15)
    @ApiModelProperty(value = "性别名称")
	private java.lang.String sex;
	/**性别代码*/
	@Excel(name = "性别代码", width = 15)
    @ApiModelProperty(value = "性别代码")
	private java.lang.String sexCode;
	/**性别代码_src*/
	@Excel(name = "性别代码_src", width = 15)
    @ApiModelProperty(value = "性别代码_src")
	private java.lang.String sexCodeSrc;
	/**性别名称_src*/
	@Excel(name = "性别名称_src", width = 15)
    @ApiModelProperty(value = "性别名称_src")
	private java.lang.String sexSrc;
	/**汇总字段*/
	@Excel(name = "汇总字段", width = 15)
    @ApiModelProperty(value = "汇总字段")
	private java.lang.String summaryField;
	/**汇总字段值*/
	@Excel(name = "汇总字段值", width = 15)
    @ApiModelProperty(value = "汇总字段值")
	private java.lang.String summaryFieldValue;
	/**任务批次名称*/
	@Excel(name = "任务批次名称", width = 15)
    @ApiModelProperty(value = "任务批次名称")
	private java.lang.String taskBatchName;
	/**结伴组ID*/
	@Excel(name = "结伴组ID", width = 15)
    @ApiModelProperty(value = "结伴组ID")
	private java.lang.String togetherid;
	/**结伴组名称*/
	@Excel(name = "结伴组名称", width = 15)
    @ApiModelProperty(value = "结伴组名称")
	private java.lang.String togethername;
	/**医疗费用总金额*/
	@Excel(name = "医疗费用总金额", width = 15)
    @ApiModelProperty(value = "医疗费用总金额")
	private java.math.BigDecimal totalfee;
	/**就诊标志*/
	@Excel(name = "就诊标志", width = 15)
    @ApiModelProperty(value = "就诊标志")
	private java.lang.String visitSign;
	/**就诊日期时间*/
	@Excel(name = "就诊日期时间", width = 15)
    @ApiModelProperty(value = "就诊日期时间")
	private java.lang.String visitdate;
	/**就诊id*/
	@Excel(name = "就诊id", width = 15)
    @ApiModelProperty(value = "就诊id")
	private java.lang.String visitid;
	/**his和医保/农合关联的visitid*/
	@Excel(name = "his和医保/农合关联的visitid", width = 15)
    @ApiModelProperty(value = "his和医保/农合关联的visitid")
	private java.lang.String visitidConnect;
	/**虚拟就诊id*/
	@Excel(name = "虚拟就诊id", width = 15)
    @ApiModelProperty(value = "虚拟就诊id")
	private java.lang.String visitidDummy;
	/**就诊类型名称*/
	@Excel(name = "就诊类型名称", width = 15)
    @ApiModelProperty(value = "就诊类型名称")
	private java.lang.String visittype;
	/**就诊类型代码*/
	@Excel(name = "就诊类型代码", width = 15)
    @ApiModelProperty(value = "就诊类型代码")
	private java.lang.String visittypeId;
	/**就诊类型代码_src*/
	@Excel(name = "就诊类型代码_src", width = 15)
    @ApiModelProperty(value = "就诊类型代码_src")
	private java.lang.String visittypeIdSrc;
	/**就诊类型名称_src*/
	@Excel(name = "就诊类型名称_src", width = 15)
    @ApiModelProperty(value = "就诊类型名称_src")
	private java.lang.String visittypeSrc;
	/**项目客户ID*/
	@Excel(name = "项目客户ID", width = 15)
    @ApiModelProperty(value = "项目客户ID")
	private java.lang.String xmkhId;
	/**项目客户名称*/
	@Excel(name = "项目客户名称", width = 15)
    @ApiModelProperty(value = "项目客户名称")
	private java.lang.String xmkhName;
	/**医保/农合就诊id*/
	@Excel(name = "医保/农合就诊id", width = 15)
    @ApiModelProperty(value = "医保/农合就诊id")
	private java.lang.String ybVisitid;
	/**年龄（岁）*/
	@Excel(name = "年龄（岁）", width = 15)
    @ApiModelProperty(value = "年龄（岁）")
	private java.lang.Integer yearage;
	/**实际住院天数*/
	@Excel(name = "实际住院天数", width = 15)
    @ApiModelProperty(value = "实际住院天数")
	private java.lang.Integer zyDays;
	/**计算的住院天数*/
	@Excel(name = "计算的住院天数", width = 15)
    @ApiModelProperty(value = "计算的住院天数")
	private java.lang.Integer zyDaysCalculate;
}
