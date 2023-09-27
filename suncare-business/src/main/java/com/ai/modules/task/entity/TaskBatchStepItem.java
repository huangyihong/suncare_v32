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
 * @Description: 批次步骤子页子项关联
 * @Author: jeecg-boot
 * @Date:   2020-02-18
 * @Version: V1.0
 */
@Data
@TableName("TASK_BATCH_STEP_ITEM")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TASK_BATCH_STEP_ITEM对象", description="批次步骤子页子项关联")
public class TaskBatchStepItem {

	/**ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "ID")
	private java.lang.String id;
	/**批次ID*/
	@Excel(name = "批次ID", width = 15)
    @ApiModelProperty(value = "批次ID")
	private java.lang.String batchId;
	/**步骤*/
	@Excel(name = "步骤", width = 15)
    @ApiModelProperty(value = "步骤")
	private java.lang.Integer step;
	/**子项关联*/
	@Excel(name = "子项关联", width = 15)
    @ApiModelProperty(value = "子项关联")
	private java.lang.String itemId;
	/**空未生成，wait等待执行，running正在生成，normal已生成，abnormal异常*/
	@Excel(name = "空未生成，wait等待执行，running正在生成，normal已生成，abnormal异常", width = 15)
    @ApiModelProperty(value = "空未生成，wait等待执行，running正在生成，normal已生成，abnormal异常")
	private java.lang.String status;
	/**错误信息等*/
	@Excel(name = "信息", width = 15)
	@ApiModelProperty(value = "信息")
	private java.lang.String msg;
	/**更新时间*/
	@Excel(name = "更新时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
	private java.util.Date updateTime;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
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

	@Excel(name = "数据源", width = 15)
	@ApiModelProperty(value = "数据源")
	private java.lang.String dataSource;
}
