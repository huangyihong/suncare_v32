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
 * @Description: 批次扩展字段映射表
 * @Author: jeecg-boot
 * @Date:   2021-06-03
 * @Version: V1.0
 */
@Data
@TableName("TASK_ACTION_BATCH_EXTMAP")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TASK_ACTION_BATCH_EXTMAP对象", description="批次扩展字段映射表")
public class TaskActionBatchExtmap {

	/**id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "id")
	private java.lang.String id;
	/**批次ID*/
	@Excel(name = "批次ID", width = 15)
    @ApiModelProperty(value = "批次ID")
	private java.lang.String batchId;
	/**不合规行为编码*/
	@Excel(name = "不合规行为编码", width = 15)
    @ApiModelProperty(value = "不合规行为编码")
	private java.lang.String actionId;
	/**扩展字段映射JSON*/
	@Excel(name = "扩展字段映射JSON", width = 15)
    @ApiModelProperty(value = "扩展字段映射JSON")
	private String groupFields;
	/**异步任务ID*/
	@Excel(name = "异步任务ID", width = 15)
    @ApiModelProperty(value = "异步任务ID")
	private java.lang.String logId;
	/**创建人姓名*/
	@Excel(name = "创建人姓名", width = 15)
    @ApiModelProperty(value = "创建人姓名")
	private java.lang.String createUserName;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUser;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
}
