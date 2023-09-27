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
 * @Description: 批次任务运行日志
 * @Author: jeecg-boot
 * @Date:   2021-04-27
 * @Version: V1.0
 */
@Data
@TableName("TASK_BATCH_BREAK_RULE_LOG")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TASK_BATCH_BREAK_RULE_LOG对象", description="批次任务运行日志")
public class TaskBatchBreakRuleLog {
    
	/**ID*/
	@Excel(name = "ID", width = 15)
    @ApiModelProperty(value = "ID")
	private java.lang.String logId;
	/**批次ID*/
	@Excel(name = "批次ID", width = 15)
    @ApiModelProperty(value = "批次ID")
	private java.lang.String batchId;
	/**对象类型{drug:药品合规,charge:收费合规}*/
	@Excel(name = "对象类型{drug:药品合规,charge:收费合规}", width = 15)
    @ApiModelProperty(value = "对象类型{drug:药品合规,charge:收费合规}")
	private java.lang.String itemType;
	/**对象ID*/
	@Excel(name = "对象ID", width = 15)
    @ApiModelProperty(value = "对象ID")
	private java.lang.String itemId;
	/**对象名称*/
	@Excel(name = "对象名称", width = 15)
    @ApiModelProperty(value = "对象名称")
	private java.lang.String itemName;
	/**状态{wait:待处理,running:处理中,normal:完成,abnormal:异常}*/
	@Excel(name = "状态{wait:待处理,running:处理中,normal:完成,abnormal:异常}", width = 15)
    @ApiModelProperty(value = "状态{wait:待处理,running:处理中,normal:完成,abnormal:异常}")
	private java.lang.String status;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**开始时间*/
	@Excel(name = "开始时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "开始时间")
	private java.util.Date startTime;
	/**结束时间*/
	@Excel(name = "结束时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "结束时间")
	private java.util.Date endTime;
	/**信息*/
	@Excel(name = "信息", width = 15)
    @ApiModelProperty(value = "信息")
	private java.lang.String message;
	/**规则json*/
	@Excel(name = "规则json", width = 15)
    @ApiModelProperty(value = "规则json")
	private java.lang.String ruleJson;
	/**规则条件json*/
	@Excel(name = "规则条件json", width = 15)
    @ApiModelProperty(value = "规则条件json")
	private java.lang.String whereJson;
	/**对象子类型*/
	@Excel(name = "对象子类型", width = 15)
    @ApiModelProperty(value = "对象子类型")
	private java.lang.String itemStype;
}
