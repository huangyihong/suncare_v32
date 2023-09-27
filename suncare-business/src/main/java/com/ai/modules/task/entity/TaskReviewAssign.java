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
 * @Description: 系统审核任务分配
 * @Author: jeecg-boot
 * @Date:   2020-05-19
 * @Version: V1.0
 */
@Data
@TableName("TASK_REVIEW_ASSIGN")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TASK_REVIEW_ASSIGN对象", description="系统审核任务分配")
public class TaskReviewAssign {

	/**分配ID*/
	@Excel(name = "分配ID", width = 15)
    @ApiModelProperty(value = "分配ID")
	@TableId("ASSIGN_ID")
	private java.lang.String assignId;
	/**任务名称*/
	@Excel(name = "任务名称", width = 15)
    @ApiModelProperty(value = "任务名称")
	private java.lang.String assignName;
	/**批次ID*/
	@Excel(name = "批次ID", width = 15)
    @ApiModelProperty(value = "批次ID")
	private java.lang.String batchId;
	/**步骤*/
	@Excel(name = "步骤", width = 15)
	@ApiModelProperty(value = "步骤")
	private java.lang.Integer step;
	/**组长*/
	@Excel(name = "组长", width = 15)
    @ApiModelProperty(value = "组长")
	private java.lang.String leader;
	/**组员*/
	@Excel(name = "组员", width = 15)
    @ApiModelProperty(value = "组员")
	private java.lang.String member;
	/**状态（01已完成）*/
	@Excel(name = "状态（01已完成）", width = 15)
    @ApiModelProperty(value = "状态（01已完成）")
	private java.lang.String status;
	/**结束时间*/
	@Excel(name = "结束时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "结束时间")
	private java.util.Date endTime;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
}
