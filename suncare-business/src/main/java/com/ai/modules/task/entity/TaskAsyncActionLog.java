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
 * @Description: 异步操作日志
 * @Author: jeecg-boot
 * @Date:   2020-12-07
 * @Version: V1.0
 */
@Data
@TableName("TASK_ASYNC_ACTION_LOG")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TASK_ASYNC_ACTION_LOG对象", description="异步操作日志")
public class TaskAsyncActionLog {

	/**日志ID*/
	@Excel(name = "日志ID", width = 15)
    @ApiModelProperty(value = "日志ID")
	@TableId("LOG_ID")
	private java.lang.String logId;
	/**操作标题*/
	@Excel(name = "操作标题", width = 15)
	@ApiModelProperty(value = "操作标题")
	private java.lang.String actionTitle;
	/**操作类型*/
	@Excel(name = "操作类型", width = 15)
    @ApiModelProperty(value = "操作类型")
    @MedicalDict(dicCode = "ASYNC_ACTION_TYPE")
	private java.lang.String actionType;
	/**操作对象*/
	@Excel(name = "操作对象", width = 15)
	@ApiModelProperty(value = "操作对象")
	private String actionObject;
	/**操作语句*/
	@Excel(name = "操作语句", width = 15)
    @ApiModelProperty(value = "操作语句")
	private String actionParam;
	/**操作平台 (SOLR,ORACLE)*/
	@Excel(name = "操作平台 (SOLR,ORACLE)", width = 15)
    @ApiModelProperty(value = "操作平台 (SOLR,ORACLE)")
	private java.lang.String actionPlatform;
	/**操作页面路径*/
	@Excel(name = "操作页面路径参数", width = 15)
	@ApiModelProperty(value = "操作页面路径参数")
	private java.lang.String actionPathParam;
	/**操作配置*/
	// 沉淀分组存分组配置，模板存模板配置
	@Excel(name = "操作配置", width = 15)
	@ApiModelProperty(value = "操作配置")
	private String actionConfig;
	/**操作记录数*/
	@Excel(name = "操作记录数", width = 15)
    @ApiModelProperty(value = "操作记录数")
	private java.lang.Integer recordCount;
    /**操作记录数*/
    @Excel(name = "剩余记录数", width = 15)
    @ApiModelProperty(value = "剩余记录数")
    private java.lang.Integer leftCount;
	/**状态*/
	@Excel(name = "状态", width = 15)
	@ApiModelProperty(value = "状态")
    @MedicalDict(dicCode = "CUSTOM_RUN_STATUS")
	private java.lang.String status;
	/**提示信息*/
	@Excel(name = "提示信息", width = 15)
	@ApiModelProperty(value = "提示信息")
	private java.lang.String msg;
	/**开始时间*/
	@Excel(name = "开始时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "开始时间")
	private java.util.Date startTime;
	/**结束时间*/
	@Excel(name = "结束时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "结束时间")
	private java.util.Date endTime;
	/**线程或进程ID*/
	@Excel(name = "线程或进程ID", width = 15)
    @ApiModelProperty(value = "线程或进程ID")
	private java.lang.String threadId;
	/**数据源*/
	@Excel(name = "数据源", width = 15)
	@ApiModelProperty(value = "数据源")
	private java.lang.String dataSource;
	/**创建人姓名*/
	@Excel(name = "创建人姓名", width = 15)
    @ApiModelProperty(value = "创建人姓名")
	private java.lang.String createUsername;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUser;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
}
