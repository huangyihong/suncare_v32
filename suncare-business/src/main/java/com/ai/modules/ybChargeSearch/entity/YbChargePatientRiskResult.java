package com.ai.modules.ybChargeSearch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 患者异常情况明细表
 * @Author: jeecg-boot
 * @Date:   2023-01-11
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_patient_risk_result")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_patient_risk_result对象", description="患者异常情况明细表")
public class YbChargePatientRiskResult {

	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**关联的任务表主键*/
	@Excel(name = "关联的任务表主键", width = 15)
    @ApiModelProperty(value = "关联的任务表主键")
	private java.lang.String taskId;
	/**tagIndex*/
	@Excel(name = "tagIndex", width = 15)
    @ApiModelProperty(value = "tagIndex")
	private java.lang.String tagIndex;
	/**tagName*/
	@Excel(name = "tagName", width = 15)
    @ApiModelProperty(value = "tagName")
	private java.lang.String tagName;
	/**年*/
	@Excel(name = "年", width = 15)
    @ApiModelProperty(value = "年")
	private java.lang.String yyear;
	/**患者ID*/
	@Excel(name = "患者ID", width = 15)
    @ApiModelProperty(value = "患者ID")
	private java.lang.String clientid;
	/**患者姓名*/
	@Excel(name = "患者姓名", width = 15)
    @ApiModelProperty(value = "患者姓名")
	private java.lang.String clientname;
	/**年龄*/
	@Excel(name = "年龄", width = 15)
    @ApiModelProperty(value = "年龄")
	private java.lang.String yearage;
	/**性别*/
	@Excel(name = "性别", width = 15)
    @ApiModelProperty(value = "性别")
	private java.lang.String sex;
	/**医院列表*/
	@Excel(name = "医院列表", width = 15)
    @ApiModelProperty(value = "医院列表")
	private java.lang.String orgList;
	/**诊断列表*/
	@Excel(name = "诊断列表", width = 15)
	@ApiModelProperty(value = "诊断列表")
	private java.lang.String diagNameSrc;
	/**映射后诊断列表*/
	@Excel(name = "映射后诊断列表", width = 15)
    @ApiModelProperty(value = "映射后诊断列表")
	private java.lang.String diagNameList;
	/**总费用*/
	@Excel(name = "总费用", width = 15)
    @ApiModelProperty(value = "总费用")
	private java.math.BigDecimal totalfeeSum;
	/**报销总费用*/
	@Excel(name = "报销总费用", width = 15)
    @ApiModelProperty(value = "报销总费用")
	private java.math.BigDecimal fundpaySum;
	/**数据来源*/
	@Excel(name = "数据来源", width = 15)
    @ApiModelProperty(value = "数据来源")
	private java.lang.String etlSource;
	/**分析对象*/
	@Excel(name = "分析对象", width = 15)
    @ApiModelProperty(value = "分析对象")
	private java.lang.String compareObject;
	/**分析指标对象的值*/
	@Excel(name = "分析指标对象的值", width = 15)
    @ApiModelProperty(value = "分析指标对象的值")
	private java.math.BigDecimal analyseValue;
	/**对比参考值*/
	@Excel(name = "对比参考值", width = 15)
    @ApiModelProperty(value = "对比参考值")
	private java.math.BigDecimal compareValue;
	/**异常阈值*/
	@Excel(name = "异常阈值", width = 15)
    @ApiModelProperty(value = "异常阈值")
	private java.math.BigDecimal abnormalStandard;
	/**对比数据集人数*/
	@Excel(name = "对比数据集人数", width = 15)
    @ApiModelProperty(value = "对比数据集人数")
	private java.math.BigDecimal comparePatientAmount;
	/**异常程度对应金额*/
	@Excel(name = "异常程度对应金额", width = 15)
    @ApiModelProperty(value = "异常程度对应金额")
	private java.math.BigDecimal abnormalMoney;
	/**参保类型*/
	@Excel(name = "参保类型", width = 15)
	@ApiModelProperty(value = "参保类型")
	private java.lang.String insurancetypename;

	/**原主键id*/
	@Excel(name = "原主键id", width = 15)
	@ApiModelProperty(value = "原主键id")
	private java.lang.String srcId;

	/**标注内容*/
	@Excel(name = "标注内容", width = 15)
	@ApiModelProperty(value = "标注内容")
	private java.lang.String labelName;

	/**标注人*/
	@Excel(name = "标注人", width = 15)
	@ApiModelProperty(value = "标注人")
	private java.lang.String labelUser;

	/**标注时间*/
	@Excel(name = "标注时间", width = 15)
	@ApiModelProperty(value = "标注时间")
	private java.lang.String labelTime;
}
