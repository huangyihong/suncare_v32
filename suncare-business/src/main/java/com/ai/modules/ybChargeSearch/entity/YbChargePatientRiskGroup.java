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
 * @Description: 患者异常情况汇总表
 * @Author: jeecg-boot
 * @Date:   2023-01-11
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_patient_risk_group")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_patient_risk_group对象", description="患者异常情况汇总表")
public class YbChargePatientRiskGroup {

	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**关联的任务表主键*/
	@Excel(name = "关联的任务表主键", width = 15)
    @ApiModelProperty(value = "关联的任务表主键")
	private java.lang.String taskId;
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
	/**标签数量*/
	@Excel(name = "标签数量", width = 15)
    @ApiModelProperty(value = "标签数量")
	private java.lang.Integer tagCount;
	/**标签列表*/
	@Excel(name = "标签列表", width = 15)
    @ApiModelProperty(value = "标签列表")
	private java.lang.String tagName;
	/**参保类型*/
	@Excel(name = "参保类型", width = 15)
	@ApiModelProperty(value = "参保类型")
	private java.lang.String insurancetypename;
}
