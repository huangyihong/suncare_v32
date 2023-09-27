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
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 任务批次不合规行为汇总字段
 * @Author: jeecg-boot
 * @Date:   2020-12-08
 * @Version: V1.0
 */
@Data
@TableName("TASK_BATCH_ACTION_FIELD_CONFIG")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TASK_BATCH_ACTION_FIELD_CONFIG对象", description="任务批次不合规行为汇总字段")
public class TaskBatchActionFieldConfig {
    
	/**主键ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键ID")
	private java.lang.String id;
	/**批次ID*/
	@Excel(name = "批次ID", width = 15)
    @ApiModelProperty(value = "批次ID")
	private java.lang.String batchId;
	/**不合理行为名称*/
	@Excel(name = "不合理行为名称", width = 15)
    @ApiModelProperty(value = "不合理行为名称")
	private java.lang.String actionName;
	/**汇总字段*/
	@Excel(name = "汇总字段", width = 15)
    @ApiModelProperty(value = "汇总字段")
	private java.lang.String summaryField;
	/**状态*/
	@Excel(name = "状态", width = 15)
    @ApiModelProperty(value = "状态")
	private java.lang.String status;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String remark;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**开始时间*/
	@Excel(name = "开始时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "开始时间")
	private java.util.Date startTime;
	/**完成时间*/
	@Excel(name = "完成时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "完成时间")
	private java.util.Date endTime;
}
