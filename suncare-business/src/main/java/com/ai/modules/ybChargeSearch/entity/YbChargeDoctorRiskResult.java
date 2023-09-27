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
 * @Description: 医生异常情况明细表
 * @Author: jeecg-boot
 * @Date:   2023-02-09
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_doctor_risk_result")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_doctor_risk_result对象", description="医生异常情况明细表")
public class YbChargeDoctorRiskResult {

	/**异常程度对应金额*/
	@Excel(name = "异常程度对应金额", width = 15)
    @ApiModelProperty(value = "异常程度对应金额")
	private java.math.BigDecimal abnormalMoney;
	/**异常阈值*/
	@Excel(name = "异常阈值", width = 15)
    @ApiModelProperty(value = "异常阈值")
	private java.math.BigDecimal abnormalStandard;
	/**分析指标对象的值*/
	@Excel(name = "分析指标对象的值", width = 15)
    @ApiModelProperty(value = "分析指标对象的值")
	private java.math.BigDecimal analyseValue;
	/**分析对象*/
	@Excel(name = "分析对象", width = 15)
    @ApiModelProperty(value = "分析对象")
	private java.lang.String compareObject;
	/**对比数据集人数*/
	@Excel(name = "对比数据集人数", width = 15)
    @ApiModelProperty(value = "对比数据集人数")
	private java.math.BigDecimal comparePatientAmount;
	/**对比参考值*/
	@Excel(name = "对比参考值", width = 15)
    @ApiModelProperty(value = "对比参考值")
	private java.math.BigDecimal compareValue;
	/**日*/
	@Excel(name = "日", width = 15)
    @ApiModelProperty(value = "日")
	private java.lang.String dday;
	/**科室映射后名称*/
	@Excel(name = "科室映射后名称", width = 15)
    @ApiModelProperty(value = "科室映射后名称")
	private java.lang.String deptname;
	/**科室原始名称*/
	@Excel(name = "科室原始名称", width = 15)
    @ApiModelProperty(value = "科室原始名称")
	private java.lang.String deptnameSrc;
	/**医生ID*/
	@Excel(name = "医生ID", width = 15)
    @ApiModelProperty(value = "医生ID")
	private java.lang.String doctorid;
	/**医生姓名*/
	@Excel(name = "医生姓名", width = 15)
    @ApiModelProperty(value = "医生姓名")
	private java.lang.String doctorname;
	/**数据来源*/
	@Excel(name = "数据来源", width = 15)
    @ApiModelProperty(value = "数据来源")
	private java.lang.String etlSource;
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**月*/
	@Excel(name = "月", width = 15)
    @ApiModelProperty(value = "月")
	private java.lang.String mmonth;
	/**医院名称*/
	@Excel(name = "医院名称", width = 15)
    @ApiModelProperty(value = "医院名称")
	private java.lang.String orgname;
	/**tagIndex*/
	@Excel(name = "tagIndex", width = 15)
    @ApiModelProperty(value = "tagIndex")
	private java.lang.String tagIndex;
	/**tagName*/
	@Excel(name = "tagName", width = 15)
    @ApiModelProperty(value = "tagName")
	private java.lang.String tagName;
	/**关联的任务表主键*/
	@Excel(name = "关联的任务表主键", width = 15)
    @ApiModelProperty(value = "关联的任务表主键")
	private java.lang.String taskId;
	/**年*/
	@Excel(name = "年", width = 15)
	@ApiModelProperty(value = "年")
	private java.lang.String yyear;

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
