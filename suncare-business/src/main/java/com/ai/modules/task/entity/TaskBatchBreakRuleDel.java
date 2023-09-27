package com.ai.modules.task.entity;

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
 * @Description: 违规模型详情
 * @Author: jeecg-boot
 * @Date:   2020-01-17
 * @Version: V1.0
 */
@Data
@TableName("TASK_BATCH_BREAK_RULE_DEL")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TASK_BATCH_BREAK_RULE_DEL对象", description="违规模型详情")
public class TaskBatchBreakRuleDel {

	/**id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "id")
	private java.lang.String id;
	/**批次ID*/
	@Excel(name = "批次ID", width = 15)
    @ApiModelProperty(value = "批次ID")
	private java.lang.String batchId;
	/**业务组ID*/
	@Excel(name = "业务组ID", width = 15)
    @ApiModelProperty(value = "业务组ID")
	private java.lang.String busiId;
	/**业务组名称*/
	@Excel(name = "业务组名称", width = 15)
    @ApiModelProperty(value = "业务组名称")
	private java.lang.String busiName;
	/**违规模型ID*/
	@Excel(name = "违规规则ID", width = 15)
    @ApiModelProperty(value = "违规模型ID")
	private java.lang.String caseId;
	/**违规模型名称*/
	@Excel(name = "违规模型名称", width = 15)
    @ApiModelProperty(value = "违规模型名称")
	private java.lang.String caseName;
	/**规则类型*/
	@Excel(name = "规则类型", width = 15)
	@ApiModelProperty(value = "规则类型")
//    @MedicalDict(dicCode = "RULE_TYPE")
	private java.lang.String ruleType;
	/**违规主体数*/
	@Excel(name = "违规主体数", width = 15)
    @ApiModelProperty(value = "违规主体数")
	private java.lang.Integer objectNum;
	/**违规记录数*/
	@Excel(name = "违规记录数", width = 15)
    @ApiModelProperty(value = "违规记录数")
	private java.lang.Integer recordNum;
	/**总金额*/
	@Excel(name = "总金额", width = 15)
    @ApiModelProperty(value = "违规总金额")
	private java.math.BigDecimal totalAcount;
	/**空未生成，wait等待执行，running正在生成，normal已生成，abnormal异常*/
	@Excel(name = "运行状态", width = 15)
    @ApiModelProperty(value = "空未生成，wait等待执行，running正在生成，normal已生成，abnormal异常")
	@MedicalDict(dicCode = "RUN_STATUS")
	private java.lang.String status;
	/**异常信息*/
	@Excel(name = "异常信息", width = 15)
    @ApiModelProperty(value = "异常信息")
	private java.lang.String errorMsg;
	/**执行开始时间*/
	@Excel(name = "执行开始时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "执行开始时间")
	private java.util.Date startTime;
	/**执行结束时间*/
	@Excel(name = "执行结束时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "执行结束时间")
	private java.util.Date endTime;

	/**审核人ID*/
	@Excel(name = "审核人ID", width = 15)
	@ApiModelProperty(value = "审核人ID")
	private java.lang.String reviewUserid;
	/**审核人名称*/
	@Excel(name = "审核人名称", width = 15)
	@ApiModelProperty(value = "审核人名称")
	private java.lang.String reviewUsername;

	/**审核完成时间*/
	@Excel(name = "审核完成时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "审核完成时间")
	private java.util.Date reviewTime;

	/**审核完成时间*/
	@Excel(name = "审核状态", width = 15)
	@ApiModelProperty(value = "审核状态")
	private String reviewStatus;

	/**审核通过记录数*/
	@Excel(name = "审核通过记录数", width = 15)
	@ApiModelProperty(value = "审核通过记录数")
	private String reviewAcount;
	
	/**违规总金额*/
	@Excel(name = "违规总金额", width = 15)
    @ApiModelProperty(value = "违规总金额")
	private java.math.BigDecimal actionMoney;
}
