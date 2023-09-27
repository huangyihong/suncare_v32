package com.ai.modules.ybChargeSearch.entity;

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
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 医生住院期间收治病人明细数据
 * @Author: jeecg-boot
 * @Date:   2023-06-15
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_doctor_admit_patient_insick")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_doctor_admit_patient_insick对象", description="医生住院期间收治病人明细数据")
public class YbChargeDoctorAdmitPatientInsick {

	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**关联的任务表主键*/
	@Excel(name = "关联的任务表主键", width = 15)
    @ApiModelProperty(value = "关联的任务表主键")
	private java.lang.String taskId;
	/**标签名字*/
	@Excel(name = "标签名字", width = 15)
    @ApiModelProperty(value = "标签名字")
	private java.lang.String tagName;
	/**就诊id*/
	@Excel(name = "就诊id", width = 15)
    @ApiModelProperty(value = "就诊id")
	private java.lang.String visitid;
	/**机构名称*/
	@Excel(name = "机构名称", width = 15)
    @ApiModelProperty(value = "机构名称")
	private java.lang.String orgname;
	/**患者id*/
	@Excel(name = "患者id", width = 15)
    @ApiModelProperty(value = "患者id")
	private java.lang.String clientid;
	/**身份证号*/
	@Excel(name = "身份证号", width = 15)
    @ApiModelProperty(value = "身份证号")
	private java.lang.String idNo;
	/**患者姓名*/
	@Excel(name = "患者姓名", width = 15)
    @ApiModelProperty(value = "患者姓名")
	private java.lang.String name;
	/**患者性别*/
	@Excel(name = "患者性别", width = 15)
    @ApiModelProperty(value = "患者性别")
	private java.lang.String sex;
	/**患者年龄*/
	@Excel(name = "患者年龄", width = 15)
    @ApiModelProperty(value = "患者年龄")
	private java.lang.String yearage;
	/**就诊类型*/
	@Excel(name = "就诊类型", width = 15)
    @ApiModelProperty(value = "就诊类型")
	private java.lang.String visittype;
	/**患者入院时间*/
	@Excel(name = "患者入院时间", width = 15)
    @ApiModelProperty(value = "患者入院时间")
	private java.lang.String visitdate;
	/**患者出院时间*/
	@Excel(name = "患者出院时间", width = 15)
    @ApiModelProperty(value = "患者出院时间")
	private java.lang.String leavedate;
	/**患者住院天数*/
	@Excel(name = "患者住院天数", width = 15)
    @ApiModelProperty(value = "患者住院天数")
	private java.lang.Integer zyDays;
	/**患者就诊科室*/
	@Excel(name = "患者就诊科室", width = 15)
    @ApiModelProperty(value = "患者就诊科室")
	private java.lang.String deptname;
	/**患者就诊医生姓名*/
	@Excel(name = "患者就诊医生姓名", width = 15)
    @ApiModelProperty(value = "患者就诊医生姓名")
	private java.lang.String doctorname;
	/**患者主诊断疾病*/
	@Excel(name = "患者主诊断疾病", width = 15)
    @ApiModelProperty(value = "患者主诊断疾病")
	private java.lang.String disPrimary;
	/**患者次诊断疾病*/
	@Excel(name = "患者次诊断疾病", width = 15)
    @ApiModelProperty(value = "患者次诊断疾病")
	private java.lang.String disSecondary;
	/**患者总费用*/
	@Excel(name = "患者总费用", width = 15)
    @ApiModelProperty(value = "患者总费用")
	private java.math.BigDecimal totalfee;
	/**患者基金支付金额*/
	@Excel(name = "患者基金支付金额", width = 15)
    @ApiModelProperty(value = "患者基金支付金额")
	private java.math.BigDecimal fundpay;
	/**患者工作单位*/
	@Excel(name = "患者工作单位", width = 15)
    @ApiModelProperty(value = "患者工作单位")
	private java.lang.String workplacename;
	/**医生就诊id*/
	@Excel(name = "医生就诊id", width = 15)
    @ApiModelProperty(value = "医生就诊id")
	private java.lang.String docVisitid;
	/**机构名称*/
	@Excel(name = "机构名称", width = 15)
    @ApiModelProperty(value = "机构名称")
	private java.lang.String docOrgname;
	/**医生名称*/
	@Excel(name = "医生名称", width = 15)
    @ApiModelProperty(value = "医生名称")
	private java.lang.String docClientname;
	/**医生入院日期*/
	@Excel(name = "医生入院日期", width = 15)
    @ApiModelProperty(value = "医生入院日期")
	private java.lang.String docVisitdate;
	/**医生出院日期*/
	@Excel(name = "医生出院日期", width = 15)
    @ApiModelProperty(value = "医生出院日期")
	private java.lang.String docLeavedate;
	/**医生工作单位*/
	@Excel(name = "医生工作单位", width = 15)
    @ApiModelProperty(value = "医生工作单位")
	private java.lang.String docWorkplacename;
	/**医生就诊总金额*/
	@Excel(name = "医生就诊总金额", width = 15)
    @ApiModelProperty(value = "医生就诊总金额")
	private java.math.BigDecimal docTotalfee;
	/**医生基金支付金额*/
	@Excel(name = "医生基金支付金额", width = 15)
    @ApiModelProperty(value = "医生基金支付金额")
	private java.math.BigDecimal docFundpay;
	/**医生主诊断疾病*/
	@Excel(name = "医生主诊断疾病", width = 15)
    @ApiModelProperty(value = "医生主诊断疾病")
	private java.lang.String docDisPrimary;
	/**医生次诊断疾病*/
	@Excel(name = "医生次诊断疾病", width = 15)
    @ApiModelProperty(value = "医生次诊断疾病")
	private java.lang.String docDisSecondary;
}
