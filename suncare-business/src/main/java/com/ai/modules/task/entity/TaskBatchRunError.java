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
 * @Description: 任务日志
 * @Author: jeecg-boot
 * @Date:   2020-08-03
 * @Version: V1.0
 */
@Data
@TableName("TASK_BATCH_RUN_ERROR")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TASK_BATCH_RUN_ERROR对象", description="任务日志")
public class TaskBatchRunError {
    
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
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**异常信息*/
	@Excel(name = "异常信息", width = 15)
    @ApiModelProperty(value = "异常信息")
	private java.lang.String message;
}
